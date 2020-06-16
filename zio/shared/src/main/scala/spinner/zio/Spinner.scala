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

package spinner.zio

import zio._
import zio.duration.Duration
import java.util.concurrent.TimeUnit
import spinner.RenderState
import spinner.SpinnerStyle
import spinner.ansi.Navigation
import spinner.template.Template

final class State private[zio] (private val inner: Ref[InnerState]) {
  def updateMessage(message: String) = inner.update(_.copy(message = message))
  def updatePosition(position: Int) = inner.update(_.copy(position = position))
  def update(message: String, position: Int) = inner.update(_.copy(message = message, position = position))
}

private final case class InnerState(
  index: Int = 0,
  message: String = "",
  done: Boolean = false,
  elapsed: Long = 0,
  style: SpinnerStyle = SpinnerStyle.default,
  prefix: String = "",
  position: Int = 0,
  len: Int = 0
)

final class ProgressBar[-R, +E, +A] private (
  val style: SpinnerStyle,
  val handler: State => ZIO[R, E, A],
  val message: String,
  val prefix: String,
  val len: Int,
  val doneMessage: Option[String] = None,
  val onErrorMessage: Option[String] = None
)

object ProgressBar {
  def builder[R, E, A](handler: State => ZIO[R, E, A]) = Builder(handler = handler)
  def builder[R, E, A](effect: ZIO[R, E, A]) = Builder(handler = _ => effect)

  final case class Builder[-R, +E, +A] private[ProgressBar] (
    style: SpinnerStyle = SpinnerStyle.default,
    handler: State => ZIO[R, E, A],
    message: String = "",
    prefix: String = "",
    len: Int = 1
  ) {
    def withMessage(message: String) = copy(message = message)
    def withPrefix(prefix: String) = copy(prefix = prefix)
    def withSpinner(spinner: String) = copy(style.copy(spinner = spinner.toCharArray().map(_.toString).toSeq))
    def withSpinner(spinner: List[String]) = copy(style = style.copy(spinner = spinner))
    def withTemplate(template: Template) = copy(style = style.copy(template = template))
    def withProgressChars(chars: String) = {
      copy(style = style.copy(progressChars = chars.toCharArray()))
    }
    def withLen(len: Int) = copy(len = len)
    def build() = new ProgressBar(style = style, handler = handler, message = message, prefix = prefix, len = len)
  }
}

final class MultiProgressBar[-R, +E, +A] private (
  header: Option[String],
  progressBars: Seq[ProgressBar[R, E, A]]
) {
  def start() = {
    for {
      _           <- printHeader()
      _           <- createSpace()
      refs        <- createStateRefs()
      currentTime <- getCurrentTime()
      fiber       <- startRenderLoop(refs, currentTime)
      result      <- handleEffects(refs, currentTime)
      _           <- fiber.join
    } yield result
  }

  private def printHeader() =
    header.map(h => console.putStrLn(h)).getOrElse(ZIO.unit)

  private def createStateRefs(): UIO[Seq[Ref[InnerState]]] = {
    ZIO.foreach(progressBars) { pb =>
      ZRef.make(
        InnerState(
          message = pb.message,
          style = pb.style,
          prefix = pb.prefix,
          position = 0,
          len = pb.len
        )
      )
    }
  }
  private def handleEffects(refs: Seq[Ref[InnerState]], currentTime: Long) =
    ZIO.foreachPar(refs.zip(progressBars.map(_.handler))) {
      case (ref, effectHandler) =>
        runEffect(ref, effectHandler, currentTime)
    }

  private def getCurrentTime() =
    clock.currentTime(TimeUnit.MILLISECONDS)

  private def runEffect[R1 <: R, E1 >: E, A1 >: A](
    ref: Ref[InnerState],
    effectHandler: State => ZIO[R1, E1, A1],
    startedAt: Long) =
    for {
      result      <- effectHandler(new State(ref))
      currentTime <- getCurrentTime()
      _           <- ref.update(_.copy(done = true, elapsed = currentTime - startedAt))
    } yield result

  private def startRenderLoop(refs: Seq[Ref[InnerState]], startedAt: Long) = {
    renderAll(refs, startedAt)
      .repeat(Schedule.doUntilM { _: Unit =>
        ZIO.foreach(refs)(_.get).map(_.forall(_.done))
      }.addDelay(_ => Duration(40, TimeUnit.MILLISECONDS)))
      .fork
  }

  private def renderAll(refs: Seq[Ref[InnerState]], startedAt: Long) = {
    (for {
      _           <- moveToTop(refs.size)
      currentTime <- clock.currentTime(TimeUnit.MILLISECONDS)
      _           <- ZIO.foreach(refs)(ref => renderLine(ref, currentTime - startedAt))
    } yield ())
  }

  private def renderLine(ref: Ref[InnerState], elapsed: Long) =
    for {
      innerState <- ref.get
      style = innerState.style
      _ <- console.putStrLn(
        style.template.render(RenderState(
          elapsed = if (innerState.done) innerState.elapsed else elapsed,
          spinner = if (innerState.done) style.spinner.last else style.spinner(innerState.index),
          message = innerState.message,
          prefix = innerState.prefix,
          progressChars = style.progressChars,
          position = innerState.position,
          len = innerState.len
        )))
      nextIndex = (innerState.index + 1) % (style.spinner.length)
      _ <- if (innerState.done) ZIO.unit else ref.set(innerState.copy(index = nextIndex, elapsed = elapsed))
    } yield ()

  private def createSpace() =
    console.putStr("\n" * progressBars.size)

  private def moveToTop(steps: Int) =
    console.putStr(Navigation.Up(steps).toAnsiCode)
}

object MultiProgressBar {
  def builder[R, E, A]() = Builder[R, E, A](None, Nil)

  final case class Builder[-R, +E, A] private[MultiProgressBar] (
    header: Option[String],
    progressBars: Seq[ProgressBar[R, E, A]]
  ) {
    def progressBar[R1 <: R, E1 >: E, A1 >: A](progresBar: ProgressBar[R1, E1, A1]) =
      copy(progressBars = progressBars.:+(progresBar))

    def progressBars[R1 <: R, E1 >: E, A1 >: A](progresBar: ProgressBar[R1, E1, A1], bars: ProgressBar[R1, E1, A1]*) =
      copy(progressBars = progressBars.:+(progresBar) ++ bars)

    def progressBars[R1 <: R, E1 >: E, A1 >: A](bars: ProgressBar[R1, E1, A1]*) =
      copy(progressBars = progressBars ++ bars)

    def withHeader(header: String) = copy(header = Some(header))

    private def build() = new MultiProgressBar(header, progressBars)

    def start() = build().start()
  }
}
