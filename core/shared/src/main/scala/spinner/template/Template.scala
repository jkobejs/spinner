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
import java.util.concurrent.TimeUnit

final case class Template(
  elements: Seq[TemplateElement]
) {
  def render(renderState: RenderState): String = {
    elements.map {
      case TemplateElement.Val(value) => value
      case variable: TemplateElement.Var =>
        variable.key match {
          case Key.Spinner => variable.render(renderState.spinner)
          case Key.Message => variable.render(renderState.message)
          case Key.ElapsedPrecise =>
            val millis = renderState.elapsed
            variable.render(formatPrecise(millis))
          case Key.Elapsed =>
            variable.render(formatHumanReadable(renderState.elapsed))
          case Key.Prefix => variable.render(renderState.prefix)
          case Key.Bar =>
            val currentPosition = ((renderState.position / renderState.len.toDouble) * variable.width)
            val n = Math.max(0, renderState.progressChars.length - 2)
            val currentProgressCharPosition = if (n == 0) 1 else ((n - (n * currentPosition).toInt % n) - 1)
            val message = renderState.progressChars.head.toString * currentPosition.toInt + renderState
              .progressChars(currentProgressCharPosition)
              .toString + (renderState.progressChars.last
              .toString()) * (variable.width - currentPosition.toInt)
            variable.render(message)
          case Key.Position => variable.render(renderState.position.toString)
          case Key.Length => variable.render(renderState.len.toString)
          case Key.Eta =>
            val eta = calculateEta(renderState)
            variable.render(formatHumanReadable(eta))
          case Key.EtaPrecise =>
            if (renderState.elapsed > 1000) {
              val eta = calculateEta(renderState)
              variable.render(formatPrecise(eta))
            } else {
              variable.render("")
            }

        }
    }.mkString(Clear.ToBeginningOfLine.toAnsiCode + Navigation.Left(1000).toAnsiCode, "", "")
  }

  private def calculateEta(renderState: RenderState): Long = {
    val percentage = renderState.position / renderState.len.toDouble
    val secondsPerPercent = renderState.elapsed / (percentage * 100)
    (secondsPerPercent * ((1 - percentage) * 100)).toLong
  }

  private def formatHumanReadable(timestamp: Long): String = {
    val hours = TimeUnit.MILLISECONDS.toHours(timestamp)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.HOURS.toMinutes(
      TimeUnit.MILLISECONDS.toHours(timestamp))
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MINUTES.toSeconds(
      TimeUnit.MILLISECONDS.toMinutes(timestamp))
    Seq(if (hours > 0) hours + "h" else "", if (minutes > 0) minutes + "m" else "", seconds + "s")
      .filter(_.nonEmpty)
      .mkString(" ")
  }

  private def formatPrecise(timestamp: Long): String =
    "%02d:%02d:%02d".format(
      TimeUnit.MILLISECONDS.toHours(timestamp),
      TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timestamp)),
      TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MINUTES.toSeconds(
        TimeUnit.MILLISECONDS.toMinutes(timestamp))
    )
}

sealed trait TemplateElement

object TemplateElement {
  final case class Val(`val`: String) extends TemplateElement

  final case class Var(
    key: Key,
    align: Alignment = Alignment.Left,
    width: Int = 20,
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
      val truncated = message.take(width)
      val diff = width - truncated.size

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
