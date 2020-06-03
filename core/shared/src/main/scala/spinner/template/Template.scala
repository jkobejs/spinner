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

package spinner.template

import spinner.InnerState
import spinner.ansi._

case class Template(
  elements: Seq[TemplateElement]
) {
  def render(innerState: InnerState, spinnerStrings: Seq[String], elapsed: Long, prefix: String): String = {
    elements.map {
      case TemplateElement.Val(value) => value
      case variable: TemplateElement.Var =>
        variable.key match {
          case Key.Spinner => variable.render(spinnerStrings(innerState.index))
          case Key.Message => variable.render(innerState.message)
          case Key.ElapsedPrecise => variable.render((elapsed / 1000.0).toString + "s")
          case Key.Elapsed => variable.render((elapsed / 1000.0).toString + "s")
          case Key.Prefix => variable.render(prefix)
        }
    }.mkString(Clear.ToBeginningOfLine.toAnsiCode + Navigation.Left(1000).toAnsiCode, "", "")
  }
}

sealed trait TemplateElement

object TemplateElement {
  case class Val(`val`: String) extends TemplateElement

  case class Var(
    key: Key,
    align: Alignment = Alignment.Left,
    width: Int = 20,
    style: Style = Style.empty()
  ) extends TemplateElement {

    def render(message: String): String = {
      val l = List(
        Attribute.Reset.toAnsiCode,
        style.fgAnsiCode,
        style.bgAnsiCode,
        style.attributes.map(_.toAnsiCode).mkString,
        if (key == Key.Message) alignMessage(message) else message,
        Attribute.Reset.toAnsiCode
      )

      l.mkString
    }

    private def alignMessage(message: String): String = {
      val truncated = message.take(width)
      val diff = width - truncated.size

      align match {
        case Alignment.Left => truncated + (" " * diff)
        case Alignment.Center =>
          if (diff % 2 == 0)  {
            val padding = diff / 2
            (" " * padding) + truncated + (" " * padding)
          } else {
            val leftPadding = diff / 2
            val rightPadding = leftPadding + 1

            (" " * leftPadding) + truncated + (" " * rightPadding)
          }
        case Alignment.Right => (" " * diff) + truncated
      }
    }
  }
}

sealed trait Key
object Key {
  final case object Spinner extends Key
  final case object Message extends Key
  final case object ElapsedPrecise extends Key
  final case object Elapsed extends Key
  final case object Prefix extends Key
}

case class Style(
  fg: Option[ForegroundColor],
  bg: Option[BackgroundColor],
  attributes: Seq[Attribute]
) {
  def fgAnsiCode = fg.map(_.toAnsiCode).getOrElse("")
  def bgAnsiCode = bg.map(_.toAnsiCode).getOrElse("")
}

sealed trait Alignment

object Alignment {
  final case object Left extends Alignment
  final case object Center extends Alignment
  final case object Right extends Alignment
}

object Style {
  def empty(): Style = Style(fg = None, bg = None, attributes = Nil)
}
