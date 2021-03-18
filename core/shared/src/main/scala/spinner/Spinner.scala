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
import spinner.RenderState
import spinner.SpinnerStyle
import spinner.ansi.Navigation
import spinner.template.Template
import scala.collection.mutable
import zio.clock.Clock
import zio.console.Console
import spinner.ansi.Clear

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

  /** Print a log line above the progress bar.
    *
    * If the progress bar was added to a [[MultiProgress]], the log line will be
    * printed above all other progress bars.
    *
    * @param message
    */
  def println(message: String): URIO[Clock with Console, Unit] =
    for {
      s <- state.get
      lines = message.split("\n")
      _ <- s.draw(lines)
    } yield ()

  /**
    * Returns if progress bar is done.
    *
    * @return
    */
  def isFinished(): UIO[Boolean] =
    state.get.map(_.finished)

  /** Spawns a background fiber to tick the progress bar.
    *
    * When this is enabled a background thread will regularly tick the
    * progress back in the given interval (milliseconds).  This is
    * useful to advance progress bars that are very slow by themselves.
    *
    * When steady ticks are enabled calling `.tick()` on a progress
    * bar does not do anything.
    *
    * @param miliseconds
    */
  def enableSteadyTick(miliseconds: Long) =
    for {
      // TODO: Check if it is already running
      fiber <- (for {
        _ <- clock.sleep(Duration(miliseconds, TimeUnit.MILLISECONDS))
        _ <- updateAndDraw(s => s.copy(tick = s.tick + 1))
      } yield ()).doWhileM(_ => isFinished().map(f => !f)).fork
      _ <- state.update(_.copy(tickFiber = Some(fiber)))
      _ <- tick()
    } yield ()

  private def updateAndDraw(update: ProgressState => ProgressState): URIO[Clock with Console, Unit] =
    for {
      currentTime  <- clock.currentTime(TimeUnit.MILLISECONDS)
      updatedState <- state.updateAndGet(update.andThen(_.copy(currentTime = currentTime)))
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
          currentTime = currentTime,
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

  val defaultSpinner: Builder =
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
  currentTime: Long,
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

  def elapsed(): Long =
    currentTime - started

  def eta(): Long =
    (elapsed() * ((1 - fraction()) / fraction())).toLong

  /**
    * Calculates index of current progress bar character that is used for fine grained bars.
    *
    * We want to support fine bars so lets explain what we want to do.
    * Imagine that we are in ideal screnario where we have:
    * - progress chars "█▇▆▅▄▃▂▁  " (size 10, completed character is '█', background character is ' ')
    * - progress bar width 10
    * - progress bar len 80
    * - and that we are incrementing position by 1 until we reach the end
    *
    * Since width of bar is 10 and length of bar is 80 first completed character will be drawn when current position is 8,
    * next one will be drawn when current position is 16 and so on until we reach 80.
    * What we can observe is that we need 8 position increments to draw completed character so we can use those 8 increments
    * to draw more fine grained drawing.
    * Lets show how our progress bar will progress from position 40 to 49:
    *  40 |█████     |
    *  41 |█████▁    |
    *  42 |█████▂    |
    *  43 |█████▃    |
    *  44 |█████▄    |
    *  45 |█████▅    |
    *  46 |█████▆    |
    *  47 |█████▇    |
    *  48 |██████    |
    *  49 |██████▁   |
    *
    * Now imagine that we have not so ideal screnario:
    * - progress chars "█▇▆▅▄▃▂▁  " (size 10, completed character is '█', background character is ' ')
    * - progress bar width 10
    * - progress bar len 40
    * - and that we are incrementing position by 1 until we reach the end
    *
    * This configuration means that completed character will be drawn when position is multiple of four and that we have 4 seps
    * for fine grained drawing of completed character
    * Lets show how our progress bar will progress from position 20 to 45:
    *  20 |█████     |
    *  21 |█████▂    |
    *  22 |█████▄    |
    *  23 |█████▆    |
    *  24 |██████    |
    *  21 |██████▂   |
    *
    * @return Current progress bar characte
    */
  def currentProgressCharIndex(): Int = {
    val currentProgressCharsSize = Math.max(0, style.progressChars.size - 2)

    val filledProgress = (fraction() * width.getOrElse(10))

    // Value that says how much of current progress char is completed so that we can pick right character
    val currentCharFilledPercentage = filledProgress - filledProgress.intValue()

    // Since we are picking from the end of array of current character we need to substract from progress char size
    currentProgressCharsSize - (currentCharFilledPercentage * currentProgressCharsSize).toInt
  }

  // TODO: Take care of overflow
  private[spinner] def incTick() = if (steadyTick == 0 || tick == 0) copy(tick = tick + 1) else this

  // TODO: Take care of overflow
  private[spinner] def advance(delta: Long) = copy(pos = (pos + delta))

  private[spinner] def draw(orphanLines: Array[String] = Array.empty[String]): URIO[Console, Unit] =
    for {
      _ <- drawTarget.draw(
        ProgressDrawState(
          lines = orphanLines.:+(
            (style.template.render(
              this
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
  lines: Array[String],
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
  import spinner.DrawTarget._

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
