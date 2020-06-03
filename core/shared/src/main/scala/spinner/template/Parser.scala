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

import spinner.ansi.{Attribute, BackgroundColor, ForegroundColor}
import fastparse._, NoWhitespace._

object Parser {
  def spinner[_: P]: P[Key] = P("spinner").map(_ => Key.Spinner)
  def msg[_: P]: P[Key] = P("msg").map(_ => Key.Message)
  def elapsed_precise[_: P]: P[Key] = P("elapsed_precise").map(_ => Key.ElapsedPrecise)
  def elapsed[_: P]: P[Key] = P("elapsed").map(_ => Key.Elapsed)
  def prefix[_: P]: P[Key] = P("prefix").map(_ => Key.Prefix)
  def key[_: P]: P[Key] = P(spinner | msg | elapsed_precise | elapsed | prefix)

  // Foreground Color
  def black[_: P]: P[ForegroundColor] = P("black").map(_ => ForegroundColor.Black)
  def red[_: P]: P[ForegroundColor] = P("red").map(_ => ForegroundColor.Red)
  def green[_: P]: P[ForegroundColor] = P("green").map(_ => ForegroundColor.Green)
  def yellow[_: P]: P[ForegroundColor] = P("yellow").map(_ => ForegroundColor.Yellow)
  def magenta[_: P]: P[ForegroundColor] = P("magenta").map(_ => ForegroundColor.Magenta)
  def white[_: P]: P[ForegroundColor] = P("white").map(_ => ForegroundColor.White)
  def blue[_: P]: P[ForegroundColor] = P("blue").map(_ => ForegroundColor.Blue)

  def foregroundColor[_: P]: P[ForegroundColor] =
    P(black | red | green | yellow | blue | magenta | white)

// Background Color
  def onBlack[_: P]: P[BackgroundColor] = P("on_black").map(_ => BackgroundColor.Black)
  def onRed[_: P]: P[BackgroundColor] = P("on_red").map(_ => BackgroundColor.Red)
  def onGreen[_: P]: P[BackgroundColor] = P("on_green").map(_ => BackgroundColor.Green)
  def onYellow[_: P]: P[BackgroundColor] = P("on_yellow").map(_ => BackgroundColor.Yellow)
  def onMagenta[_: P]: P[BackgroundColor] = P("on_magenta").map(_ => BackgroundColor.Magenta)
  def onWhite[_: P]: P[BackgroundColor] = P("on_white").map(_ => BackgroundColor.White)
  def onBlue[_: P]: P[BackgroundColor] = P("on_blue").map(_ => BackgroundColor.Blue)

  def backgroundColor[_: P]: P[BackgroundColor] =
    P(onBlack | onRed | onGreen | onYellow | onBlue | onMagenta | onWhite)

  // Alignment
  def left[_: P]: P[Alignment] = P("<").map(_ => Alignment.Left)
  def center[_: P]: P[Alignment] = P("^").map(_ => Alignment.Center)
  def right[_: P]: P[Alignment] = P(">").map(_ => Alignment.Right)
  def alignment[_: P]: P[Alignment] = P((left | center | right))

  // Width
  def width[_: P] = P(CharIn("0-9").rep(1).!.map(_.toInt))

  // Attribute
  def bold[_: P]: P[Attribute] = P("bold").map(_ => Attribute.Bold)
  def dim[_: P]: P[Attribute] = P("dim").map(_ => Attribute.Dim)
  def underlined[_: P]: P[Attribute] = P("underlined").map(_ => Attribute.Underlined)
  def slowBlink[_: P]: P[Attribute] = P("slow_blink").map(_ => Attribute.SlowBlink)
  def rapidBlink[_: P]: P[Attribute] = P("rapid_blink").map(_ => Attribute.RapidBlink)
  def reverse[_: P]: P[Attribute] = P("reverse").map(_ => Attribute.Reverse)
  def hidden[_: P]: P[Attribute] = P("hidden").map(_ => Attribute.Hidden)
  def attribute[_: P]: P[Attribute] = P(bold | dim | underlined | slowBlink | rapidBlink | reverse | hidden)

  // Style
  def style[_: P] =
    P(("." ~ foregroundColor).? ~ ("." ~ backgroundColor).? ~ ("." ~ attribute).rep).map {
      case (fgOpt, bgOpt, attrs) => Style(fg = fgOpt, bg = bgOpt, attributes = attrs)
    }

  // Variable
  def variable[_: P]: P[TemplateElement] = P("{" ~ key ~ ":" ~ alignment.? ~ width.? ~ style.? ~ "}").map {
    case (key, alignmentOpt, widthOpt, styleOpt) =>
      TemplateElement.Var(
        key = key,
        align = alignmentOpt.getOrElse(Alignment.Left),
        width = widthOpt.getOrElse(20),
        style = styleOpt.getOrElse(Style.empty())
      )
  }

  // def stringChars(c: Char) = c != '{' && c != '}'
  def stringChars(c: Char) = c == c

  // def value[_: P]: P[TemplateElement] = P(CharsWhile(stringChars).!).map(TemplateElement.Val(_))
  def value[_: P]: P[TemplateElement] =
    P(AnyChar.!).filter(str => !parse(str, variable(_)).isSuccess).map(TemplateElement.Val(_))

  // Template
  def template[_: P]: P[Template] =
    P(Start ~ (variable | value).rep ~ End).map(Template(_))
}
