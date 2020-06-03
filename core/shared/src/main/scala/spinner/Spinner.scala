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

package spinner

import zio._
import zio.duration.Duration
import java.util.concurrent.TimeUnit
import spinner.template._

case class Spinner private (
  private val style: SpinnerStyle,
  private val innerState: InnerState,
  private val prefix: String) {
  def spin[R, E, A](effect: ZIO[R, E, A]): ZIO[R with clock.Clock with console.Console, E, A] = {

    for {
      ref         <- ZRef.make(innerState)
      currentTime <- clock.currentTime(TimeUnit.MILLISECONDS)
      fiber       <- spinEffect(ref, currentTime).forever.fork
      result      <- effect
      _           <- fiber.interruptFork
    } yield result
  }

  def spinWithState[R, E, A](f: State => ZIO[R, E, A]): ZIO[R with clock.Clock with console.Console, E, A] = {
    for {
      ref         <- ZRef.make(innerState)
      currentTime <- clock.currentTime(TimeUnit.MILLISECONDS)
      fiber       <- spinEffect(ref, currentTime).forever.fork
      result      <- f(State(ref))
      _           <- fiber.interruptFork
    } yield result
  }

  private def spinEffect(ref: Ref[InnerState], startedAt: Long) = {
    (for {
      innerState  <- ref.get
      currentTime <- clock.currentTime(TimeUnit.MILLISECONDS)
      _           <- console.putStr(style.template.render(innerState, style.spinner, currentTime - startedAt, prefix))
      nextIndex = (innerState.index + 1) % (style.spinner.length)
      _ <- ref.set(innerState.copy(index = nextIndex))
    } yield ()).repeat(Schedule.spaced(Duration(100, TimeUnit.MILLISECONDS)))
  }
}

object Spinner {
  def default(): Spinner = Spinner(SpinnerStyle.default, InnerState(0, ""), "")

  def apply(spinner: String, template: Template, message: String = "", prefix: String = ""): Spinner =
    Spinner(SpinnerStyle(spinner.toCharArray().map(_.toString).toSeq, template), InnerState(0, message), prefix)

  def apply(spinner: Seq[String], template: Template, message: String, prefix: String): Spinner =
    Spinner(SpinnerStyle(spinner, template), InnerState(0, message), prefix: String)

}

case class InnerState private (
  index: Int,
  message: String
)

case class State private (private val inner: Ref[InnerState]) {
  def updateMessage(message: String) = inner.update(_.copy(message = message))
}

case class SpinnerStyle private (
  spinner: Seq[String],
  template: Template
)

object SpinnerStyle {
  def default(): SpinnerStyle =
    SpinnerStyle(
      spinner = "⠁⠁⠉⠙⠚⠒⠂⠂⠒⠲⠴⠤⠄⠄⠤⠠⠠⠤⠦⠖⠒⠐⠐⠒⠓⠋⠉⠈⠈ ".toCharArray().map(_.toString).toSeq,
      template = Template(
        Seq(
          TemplateElement.Var(Key.Prefix),
          TemplateElement.Var(Key.Spinner),
          TemplateElement.Val(" "),
          TemplateElement.Var(Key.Message),
          TemplateElement.Val(" Elapsed: "),
          TemplateElement.Var(Key.Elapsed)
        ))
    )
}
