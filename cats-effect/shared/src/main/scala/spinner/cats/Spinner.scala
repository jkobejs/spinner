/*
 * Copyright (c) 2020 the spinner contributors.
 * See the project homepage at: https://jkobejs.github.io/spinner/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spinner.cats

import spinner.template._
import spinner.RenderState
import spinner.SpinnerStyle
import cats.effect._
import cats.implicits._
import java.util.concurrent.TimeUnit
import cats.effect.concurrent.Ref
import scala.concurrent.duration._

final class Spinner private (
  private val style: SpinnerStyle,
  private val innerState: InnerState,
  private val prefix: String
) {
  def spin[F[_], A](effect: F[A])(implicit F: Concurrent[F], timer: Timer[F]): F[A] =
    for {
      ref         <- Ref.of(innerState)
      currentTime <- timer.clock.realTime(TimeUnit.MILLISECONDS)
      fiber       <- F.start(spinnEffect(ref, currentTime))
      result      <- effect
      _           <- fiber.cancel
    } yield result

  def spinWithState[F[_], A](f: State[F] => F[A])(implicit F: Concurrent[F], timer: Timer[F]): F[A] =
    for {
      ref         <- Ref.of(innerState)
      currentTime <- timer.clock.realTime(TimeUnit.MILLISECONDS)
      fiber       <- F.start(spinnEffect(ref, currentTime))
      result      <- f(new State(ref))
      _           <- fiber.cancel
    } yield result

  private def spinnEffect[F[_]](ref: Ref[F, InnerState], startedAt: Long)(
    implicit F: Sync[F],
    timer: Timer[F]): F[Unit] = {
    (for {
      innerState  <- ref.get
      currentTime <- timer.clock.realTime(TimeUnit.MILLISECONDS)
      _ <- F.delay(
        print(
          style.template.render(
            RenderState(
              elapsed = currentTime - startedAt,
              spinner = style.spinner(innerState.index),
              message = innerState.message,
              prefix = prefix,
              progressChars = Array(),
              position = 0,
              len = 0
            ))))
      nextIndex = (innerState.index + 1) % (style.spinner.length)
      _ <- ref.set(innerState.copy(index = nextIndex))
      _ <- timer.sleep(100.milli)
    } yield ()).flatMap(_ => spinnEffect(ref, startedAt))
  }
}

object Spinner {
  def default(): Spinner = new Spinner(SpinnerStyle.default, InnerState(0, ""), "")

  def apply(spinner: String, template: Template, message: String = "", prefix: String = ""): Spinner =
    new Spinner(
      SpinnerStyle(spinner.toCharArray().map(_.toString).toSeq, template, progressChars = Array('#', '#', '-')),
      InnerState(0, message),
      prefix)

  def apply(spinner: Seq[String], template: Template, message: String, prefix: String): Spinner =
    new Spinner(
      SpinnerStyle(spinner, template, progressChars = Array('#', '#', '-')),
      InnerState(0, message),
      prefix: String)

}

final class State[F[_]] private[cats] (private val inner: Ref[F, InnerState]) {
  def updateMessage(message: String) = inner.update(_.copy(message = message))
}

private final case class InnerState(
  index: Int,
  message: String
)
