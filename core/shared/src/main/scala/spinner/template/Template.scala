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

import spinner.RenderState
import spinner.ansi._
import spinner.ProgressState

final case class Template(
  elements: Seq[TemplateElement]
) {

  def render(renderState: ProgressState): String = {
    elements.map {
      case TemplateElement.Val(value) => value
      case variable: TemplateElement.Var =>
        variable.key match {
          case Key.Spinner => variable.render(renderState.currentTickString())
          case Key.Message => variable.render(renderState.message)
          case Key.ElapsedPrecise =>
            variable.render(Formatting.preciseTime(renderState.elapsed()))
          case Key.Elapsed =>
            variable.render(Formatting.humanReadableTime(renderState.elapsed()))
          case Key.Prefix => variable.render(renderState.prefix)
          case Key.Bar =>
            val width = variable.width.getOrElse(20)
            val fraction = renderState.fraction()
            val completed = (width * fraction).toInt

            // val drawCurrent = (fraction > 0.0 && completed < width)

            val currentChar = renderState.style.progressChars(renderState.currentProgressCharIndex()).toString()

            // val currentChar = if (drawCurrent) {
            //   val currentCharsSize = Math.max(renderState.style.progressChars.size - 2, 0)

            //   val currentCharPosition =
            //     if (currentCharsSize > 0) {
            //       currentCharsSize - (fraction * width * currentCharsSize).toInt % currentCharsSize
            //     } else {
            //       1
            //     }
            //   renderState.style.progressChars(currentCharPosition).toString
            // } else ""

            val remaining = width - completed - currentChar.size
            val message = renderState.style.progressChars.head.toString * completed + currentChar + renderState.style.progressChars.last.toString * remaining
            variable.render(message)
          case Key.Position => variable.render(renderState.pos.toString)
          case Key.Length => variable.render(renderState.len.toString)
          case Key.Eta =>
            val eta = renderState.eta()
            variable.render(Formatting.humanReadableTime(eta))
          case Key.EtaPrecise =>
            if (renderState.elapsed() > 1000) {
              val eta = renderState.eta()
              variable.render(Formatting.preciseTime(eta))
            } else {
              variable.render("")
            }
          case Key.Bytes =>
            variable.render(Formatting.humanReadableSizeSI(renderState.pos.toLong))
          case Key.TotalBytes =>
            variable.render(Formatting.humanReadableSizeSI(renderState.len.toLong))

        }
    }.mkString(Clear.ToBeginningOfLine.toAnsiCode + Navigation.Left(1000).toAnsiCode, "", "")
  }
}

sealed trait TemplateElement

object TemplateElement {
  final case class Val(`val`: String) extends TemplateElement

  final case class Var(
    key: Key,
    align: Alignment = Alignment.Left,
    width: Option[Int] = None,
    style: Style = Style.empty()
  ) extends TemplateElement {

    def render(message: String): String = {
      val alignedMessage = alignMessage(message)
      val allAttributes =
        (style.fg.map(_.toFansi).toList ++ style.bg.map(_.toFansi).toList ++ style.attributes.map(_.toFansi))
          .map(attr => (attr, 0, alignedMessage.size))

      fansi
        .Str(alignMessage(message))
        .overlayAll(
          allAttributes
        )
        .toString()
    }

    private def alignMessage(message: String): String = {
      val wdth = width.getOrElse(message.size)
      val truncated = message.take(wdth)
      val diff = wdth - truncated.size

      align match {
        case Alignment.Left => truncated + (" " * diff)
        case Alignment.Center =>
          if (diff % 2 == 0) {
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
  final case object Bar extends Key
  final case object Position extends Key
  final case object Length extends Key
  final case object Eta extends Key
  final case object EtaPrecise extends Key
  final case object Bytes extends Key
  final case object TotalBytes extends Key
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
