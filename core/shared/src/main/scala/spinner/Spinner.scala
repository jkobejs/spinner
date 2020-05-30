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

import zio.ZIO
import zio.ZRef
import zio.Ref
import zio.Schedule
import zio._
import zio.duration.Duration
import java.util.concurrent.TimeUnit
import spinner.template.Template
import spinner.template.TemplateElement
// import spinner.template.Key
// import spinner.template.Alignment
// import spinner.template.Style
// import spinner.template.Color
// import spinner.template.Attribute

case class Spinner private (private val style: SpinnerStyle, private val innerState: InnerState) {
  def spin[R, E, A](effect: ZIO[R, E, A]): ZIO[R with clock.Clock with console.Console, E, A] = {

    for {
      ref    <- ZRef.make(innerState)
      fiber  <- spinEffect(ref).forever.fork
      result <- effect
      _      <- fiber.interruptFork
    } yield result
  }

  def spinWithState[R, E, A](f: State => ZIO[R, E, A]): ZIO[R with clock.Clock with console.Console, E, A] = {
    for {
      ref    <- ZRef.make(innerState)
      fiber  <- spinEffect(ref).forever.fork
      result <- f(State(ref))
      _      <- fiber.interruptFork
    } yield result
  }

  private def spinEffect(ref: Ref[InnerState]) = {
    (for {
      innerState <- ref.get
      _          <- console.putStr(s"\u001B[1K\u001B[1000D${render(innerState, 0, "prefix")}")
      nextIndex = (innerState.index + 1) % (style.spinnerStrings.length)
      _ <- ref.set(innerState.copy(index = nextIndex))
    } yield ()).repeat(Schedule.spaced(Duration(100, TimeUnit.MILLISECONDS)))
  }

  private def render(inState: InnerState, currentTime: Long, prefix: String): String =
    style.template.elements.map {
      case TemplateElement.Val(value) => value
      case variable: TemplateElement.Var => variable.render(inState, style.spinnerStrings, currentTime, prefix)
    }.mkString

}

object Spinner {
  def default(): Spinner = Spinner(SpinnerStyle.default, InnerState(0, "Spinning...", 0))
}

case class InnerState private (
  index: Int,
  message: String,
  startedAt: Long
)

case class State private (private val inner: Ref[InnerState]) {
  def updateMessage(message: String) = inner.update(_.copy(message = message))
}

case class SpinnerStyle private (
  spinnerStrings: List[String],
  template: Template
)

object SpinnerStyle {
  import spinner.template.interpolator._

  def default(): SpinnerStyle =
    SpinnerStyle(
      spinnerStrings = "⠁⠁⠉⠙⠚⠒⠂⠂⠒⠲⠴⠤⠄⠄⠤⠠⠠⠤⠦⠖⠒⠐⠐⠒⠓⠋⠉⠈⠈ ".toCharArray().map(_.toString).toList,
      // template = Template(Nil)
      template = template"Spinn{er: {spinner:.blue} Mess}age: {msg:.yellow.underlined} Elapsed {elapsed:.green.dim}"
      // template = Template(
      //   Seq(
      //     TemplateElement.Val("Spinner: "),
      //     TemplateElement.Var(
      //       key = Key.Spinner,
      //       align = Alignment.Left,
      //       width = 20,
      //       style = Style.empty().fg(Color.Blue)
      //     ),
      //     TemplateElement.Val(" Message: "),
      //     TemplateElement.Var(
      //       key = Key.Message,
      //       align = Alignment.Left,
      //       width = 20,
      //       style = Style
      //         .empty()
      //         .fg(Color.Cyan)
      //         .attribute(Attribute.Underlined)
      //         .attribute(Attribute.Italic)
      //       // .attribute(Attribute.Bold)
      //     ),
      //     TemplateElement.Val(" Elapsed: "),
      //     TemplateElement.Var(
      //       key = Key.Elapsed,
      //       align = Alignment.Left,
      //       width = 20,
      //       style = Style.empty().fg(Color.Red).attribute(Attribute.Dim)
      //     )
      //   ))
    )
}
