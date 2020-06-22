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
import scala.collection.mutable
import zio.clock.Clock
import zio.console.Console
import spinner.ansi.Clear

final class State private[zio] (private val inner: Ref[InnerState]) {
  def updateMessage(message: String) = inner.update(_.copy(message = message))
  def updatePosition(position: Int) = inner.update(_.copy(position = position))
  def update(message: String, position: Int) = inner.update(_.copy(message = message, position = position))
  def queue: Queue[Unit] = ???
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

case class ProgresBar private (
  state: Ref[ProgressState]
) {

  /**
    * Manually ticks the spinner or progress bar.
    *
    * @return
    */
  def tick(): URIO[Clock with Console, Unit] =
    updateAndDraw(s => s.incTick())

  /**
    * Advances position of a progress bar by delta.
    *
    * @param delta
    */
  def advance(delta: Long): URIO[Clock with Console, Unit] =
    updateAndDraw(s => s.advance(delta).incTick())

  /**
    * Sets position to given value.
    *
    * @param position
    */
  def setPosition(position: Long): URIO[Clock with Console, Unit] =
    updateAndDraw(_.copy(pos = position).incTick())

  /**
    * Sets prefix to given value.
    *
    * For the prefix to be visible, `{prefix}` placeholder
    * must be present in the template.
    *
    * @param prefix
    */
  def setPrefix(prefix: String): URIO[Clock with Console, Unit] =
    updateAndDraw(_.copy(prefix = prefix).incTick())

  /**
    * Sets message to given value.
    *
    * For the message to be visible, `{msg}` placeholder
    * must be present in the template.
    *
    * @param message
    */
  def setMessage(message: String): URIO[Clock with Console, Unit] =
    updateAndDraw(_.copy(message = message))

  /**
    * Finishes the progress bar and leaves the current message.
    *
    * For the message to be visible, `{msg}` placeholder
    * must be present in the template
    */
  def finish(): URIO[Clock with Console, Unit] =
    updateAndDraw(s => s.copy(pos = s.len, finished = true))

  /**
    * Finishes the progress bar and sets a message.
    *
    * @param message
    * @return
    */
  def finishWithMessage(message: String): URIO[Clock with Console, Unit] =
    updateAndDraw(s => s.copy(pos = s.len, message = message, finished = true))

  def println(message: String): URIO[Clock with Console, Unit] =
    for {
      s <- state.get
      lines = Seq(message)
      _ <- s.draw(lines)
    } yield ()

  def isFinished(): UIO[Boolean] =
    state.get.map(_.finished)

  def enableSteadyTick(miliseconds: Long) =
    for {
      fiber <- (for {
        _ <- clock.sleep(Duration(miliseconds, TimeUnit.MILLISECONDS))
        _ <- updateAndDraw(s => s.copy(tick = s.tick + 1))
      } yield ()).doWhileM(_ => isFinished().map(f => !f)).fork
      _ <- state.update(_.copy(tickFiber = Some(fiber)))
      _ <- tick()
    } yield ()

  private def updateAndDraw(update: ProgressState => ProgressState): URIO[Clock with Console, Unit] =
    for {
      updatedState <- state.updateAndGet(update)
      _            <- updatedState.draw()
    } yield ()

}

object ProgresBar {
  case class Builder private (
    style: SpinnerStyle,
    len: Long,
    message: String,
    drawTarget: DrawTarget
  ) {
    def withMessage(message: String) = copy(message = message)
    def withStyle(style: SpinnerStyle) = copy(style = style)
    def withDrawTarget(drawTarget: DrawTarget) = copy(drawTarget = drawTarget)
    def withLen(len: Long) = copy(len = len)

    def build(): URIO[Clock, ProgresBar] =
      for {
        currentTime <- clock.currentTime(TimeUnit.MILLISECONDS)
        state = ProgressState(
          style = this.style,
          pos = 0,
          len = this.len,
          tick = 0,
          started = currentTime,
          drawTarget = this.drawTarget,
          width = None,
          message = this.message,
          prefix = "",
          estimate = 0,
          steadyTick = 0,
          finished = false,
          tickFiber = None
        )
        stateRef <- Ref.make(state)
      } yield ProgresBar(stateRef)
  }

  def defaultBar(len: Long): Builder =
    Builder(
      style = SpinnerStyle.default,
      len = len,
      message = "",
      drawTarget = DrawTarget.Single(None)
    )

  def defaultSpinner: Builder =
    Builder(
      style = SpinnerStyle.defaultSpinner.build(),
      len = 0,
      message = "",
      drawTarget = DrawTarget.Single(None)
    )
}

case class ProgressState(
  style: SpinnerStyle,
  pos: Long,
  len: Long,
  tick: Int,
  started: Long,
  drawTarget: DrawTarget,
  width: Option[Int],
  message: String,
  prefix: String,
  estimate: Long,
  steadyTick: Long,
  finished: Boolean,
  tickFiber: Option[Fiber.Runtime[Nothing, Unit]]
) {
  def currentTickString(): String =
    if (finished)
      style.spinner.lastOption.getOrElse("")
    else style.spinner(tick % (style.spinner.size - 1))

  def fraction(): Double =
    ((pos, len) match {
      case (_, 0) => 1.0
      case (0, _) => 0.0
      case (pos, len) => pos / len.toDouble
    }).max(0.0).min(1.0)

  // TODO: Take care of overflow
  private[spinner] def incTick() = if (steadyTick == 0 || tick == 0) copy(tick = tick + 1) else this

  // TODO: Take care of overflow
  private[spinner] def advance(delta: Long) = copy(pos = (pos + delta))

  private[spinner] def draw(orphanLines: Seq[String] = Nil): URIO[Clock with Console, Unit] =
    for {
      currentTime <- clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- drawTarget.draw(
        ProgressDrawState(
          lines = orphanLines ++ Seq(
            style.template.render(RenderState(
              elapsed = currentTime - started,
              prefix = prefix,
              spinner = currentTickString(),
              message = message,
              progressChars = style.progressChars,
              position = pos.toInt,
              len = len.toInt
            ))),
          orphanLines = orphanLines.size,
          finished = false,
          forceDraw = false,
          moveCursor = false,
          timestamp = 0
        )
      )
    } yield ()
}

case class MultiBar(
  states: Ref[mutable.ArrayBuffer[ProgressDrawState]],
  ordering: Seq[Int],
  stateQueue: Queue[(Int, ProgressDrawState)]
) {
  def join() =
    (for {
      idWithState <- stateQueue.take
      _           <- states.modify(arrayBuffer => (arrayBuffer.insert(idWithState._1, idWithState._2), arrayBuffer))
    } yield ()).doWhileM(_ => states.get.map(_.exists(state => !state.finished)))
}

object MultiBar {
  def create() =
    for {
      queue  <- Queue.bounded[(Int, ProgressDrawState)](32)
      states <- Ref.make(mutable.ArrayBuffer.empty[ProgressDrawState])
    } yield MultiBar(states = states, ordering = Nil, stateQueue = queue)
}

case class ProgressDrawState(
  /// The lines to print (can contain ANSI codes)
  lines: Seq[String],
  /// The number of lines that shouldn't be reaped by the next tick.
  orphanLines: Int,
  /// True if the bar no longer needs drawing.
  finished: Boolean,
  /// True if drawing should be forced.
  forceDraw: Boolean,
  /// True if we should move the cursor up when possible instead of clearing lines.
  moveCursor: Boolean,
  /// Time when the draw state was created.
  timestamp: Long
) {
  def moveCursorUp() = {
    if (moveCursor)
      console.putStr(Navigation.Up(lines.size - orphanLines).toAnsiCode)
    else UIO.unit
  }

  def clearOutput() = {
    val numOfLines = lines.size - orphanLines
    console.putStr(
      Navigation
        .Up(numOfLines)
        .toAnsiCode + (Clear.EntireLine.toAnsiCode + Navigation.Down(1).toAnsiCode) * numOfLines +
        Navigation.Up(numOfLines).toAnsiCode)
  }

  def write() = {
    console.putStrLn(lines.mkString("\n"))
  }
}

sealed trait DrawTarget {
  import spinner.zio.DrawTarget._

  def draw(state: ProgressDrawState): URIO[Console, Unit] = {
    this match {
      case Single(_) =>
        (if (state.moveCursor)
           state.moveCursorUp()
         else
           state.clearOutput) *> state.write()
      case Multi(index, sendQueue) =>
        sendQueue.offer((index, state)) *> UIO.unit
    }
  }
}

object DrawTarget {
  case class Single(lastState: Option[ProgressDrawState]) extends DrawTarget
  case class Multi(index: Int, sendQueue: Queue[(Int, ProgressDrawState)]) extends DrawTarget
}
