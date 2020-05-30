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

case class Template(
  elements: Seq[TemplateElement]
)

sealed trait TemplateElement

object TemplateElement {
  case class Val(`val`: String) extends TemplateElement
  case class Var(
    key: Key,
    align: Alignment,
    width: Int,
    style: Style
  ) extends TemplateElement {
    def render(innerState: InnerState, spinnerStrings: List[String], currentTime: Long, prefix: String): String = {
      key match {
        case Key.Spinner => applyStyle(spinnerStrings(innerState.index))
        case Key.Message => applyStyle(innerState.message)
        case Key.ElapsedPrecise => applyStyle(((currentTime - innerState.startedAt) / 1000.0).toString)
        case Key.Elapsed => applyStyle(((currentTime - innerState.startedAt) / 1000.0).toString)
        case Key.Prefix => applyStyle(prefix)
      }
    }

    private def applyStyle(message: String): String =
      s"${Console.RESET}${style.attributes.map(_.toAnsi).mkString}${style.fg.map(_.color.toAnsiFg).getOrElse("")}$message${Console.RESET}"
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
  bg: Option[BackgrounColor],
  attributes: Seq[Attribute]
) {
  def fg(color: Color): Style = this.copy(fg = Some(ForegroundColor(color)))
  def bg(color: Color): Style = this.copy(bg = Some(BackgrounColor(color)))
  def attribute(attribute: Attribute): Style = this.copy(attributes = attributes.:+(attribute))

  def black(): Style = fg(Color.Black)
  def red(): Style = fg(Color.Red)
  def green(): Style = fg(Color.Green)
  def yellow(): Style = fg(Color.Yellow)
}

sealed trait Alignment

object Alignment {
  final case object Left extends Alignment
  final case object Center extends Alignment
  final case object Right extends Alignment

  def parse(alignment: String): Alignment =
    alignment match {
      case "<" => Alignment.Left
      case "^" => Alignment.Center
      case ">" => Alignment.Right
      case _ => Alignment.Left
    }
}

object Style {
  def empty(): Style = Style(fg = None, bg = None, attributes = Nil)
}

sealed trait Color {
  def toAnsiFg: String
  def toAnsiBg: String
}

object Color {
  final case object Black extends Color {
    override def toAnsiFg: String = Console.BLACK
    override def toAnsiBg: String = Console.BLACK_B
  }
  final case object Red extends Color {
    override def toAnsiFg: String = Console.RED
    override def toAnsiBg: String = Console.RED_B
  }
  final case object Green extends Color {
    override def toAnsiFg: String = Console.GREEN
    override def toAnsiBg: String = Console.GREEN_B
  }
  final case object Yellow extends Color {
    override def toAnsiFg: String = Console.YELLOW
    override def toAnsiBg: String = Console.YELLOW_B
  }
  final case object Blue extends Color {
    override def toAnsiFg: String = Console.BLUE
    override def toAnsiBg: String = Console.BLUE_B
  }
  final case object Magenta extends Color {
    override def toAnsiFg: String = Console.MAGENTA
    override def toAnsiBg: String = Console.MAGENTA_B
  }
  final case object Cyan extends Color {
    override def toAnsiFg: String = Console.CYAN
    override def toAnsiBg: String = Console.CYAN_B
  }
  final case object White extends Color {
    override def toAnsiFg: String = Console.WHITE
    override def toAnsiBg: String = Console.WHITE_B
  }
}

case class ForegroundColor(color: Color)
case class BackgrounColor(color: Color)

sealed trait Attribute {
  def toAnsi: String
}

object Attribute {
  final case object Bold extends Attribute {
    override def toAnsi: String = Console.BOLD
  }
  final case object Dim extends Attribute {
    override def toAnsi: String = "\u001b[2m"
  }
  final case object Italic extends Attribute {
    override def toAnsi: String = "\u001b[3m"
  }
  final case object Underlined extends Attribute {
    override def toAnsi: String = Console.UNDERLINED
  }
  final case object Blink extends Attribute {
    override def toAnsi: String = Console.BLINK
  }
  final case object Reverse extends Attribute {
    override def toAnsi: String = Console.REVERSED
  }
  final case object Hidden extends Attribute {
    override def toAnsi: String = Console.INVISIBLE
  }
}
