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
  def bar[_: P]: P[Key] = P("bar").map(_ => Key.Bar)
  def length[_: P]: P[Key] = P("len").map(_ => Key.Length)
  def position[_: P]: P[Key] = P("pos").map(_ => Key.Position)
  def eta[_: P]: P[Key] = P("eta").map(_ => Key.Eta)
  def etaPrecise[_: P]: P[Key] = P("eta_precise").map(_ => Key.EtaPrecise)
  def bytes[_: P]: P[Key] = P("bytes").map(_ => Key.Bytes)
  def totalBytes[_: P]: P[Key] = P("total_bytes").map(_ => Key.TotalBytes)
  def key[_: P]: P[Key] =
    P(
      spinner | msg | elapsed_precise | elapsed | prefix | bar | length
        | position | etaPrecise | eta | totalBytes | bytes)

  // Foreground Color
  def black[_: P]: P[ForegroundColor] = P("black").map(_ => ForegroundColor.Black)
  def red[_: P]: P[ForegroundColor] = P("red").map(_ => ForegroundColor.Red)
  def green[_: P]: P[ForegroundColor] = P("green").map(_ => ForegroundColor.Green)
  def yellow[_: P]: P[ForegroundColor] = P("yellow").map(_ => ForegroundColor.Yellow)
  def magenta[_: P]: P[ForegroundColor] = P("magenta").map(_ => ForegroundColor.Magenta)
  def white[_: P]: P[ForegroundColor] = P("white").map(_ => ForegroundColor.White)
  def blue[_: P]: P[ForegroundColor] = P("blue").map(_ => ForegroundColor.Blue)
  def cyan[_: P]: P[ForegroundColor] = P("cyan").map(_ => ForegroundColor.Cyan)
  def brightRed[_: P]: P[ForegroundColor] = P("bright_red").map(_ => ForegroundColor.BrightRed)
  def brightGreen[_: P]: P[ForegroundColor] = P("bright_green").map(_ => ForegroundColor.BrightGreen)
  def brightYellow[_: P]: P[ForegroundColor] = P("bright_yellow").map(_ => ForegroundColor.BrightYellow)
  def brightMagenta[_: P]: P[ForegroundColor] = P("bright_magenta").map(_ => ForegroundColor.BrightMagenta)
  def brightBlue[_: P]: P[ForegroundColor] = P("bright_blue").map(_ => ForegroundColor.BrightBlue)
  def brightCyan[_: P]: P[ForegroundColor] = P("bright_cyan").map(_ => ForegroundColor.BrightCyan)
  def darkGray[_: P]: P[ForegroundColor] = P("dark_gray").map(_ => ForegroundColor.DarkGray)
  def lightGray[_: P]: P[ForegroundColor] = P("light_gray").map(_ => ForegroundColor.LightGray)

  def foregroundColor[_: P]: P[ForegroundColor] =
    P(
      black | red | green | yellow | blue | magenta | white | cyan
        | brightRed | brightGreen | brightYellow | brightMagenta | brightBlue | brightCyan
        | darkGray | lightGray
    )

// Background Color
  def onBlack[_: P]: P[BackgroundColor] = P("on_black").map(_ => BackgroundColor.Black)
  def onRed[_: P]: P[BackgroundColor] = P("on_red").map(_ => BackgroundColor.Red)
  def onGreen[_: P]: P[BackgroundColor] = P("on_green").map(_ => BackgroundColor.Green)
  def onYellow[_: P]: P[BackgroundColor] = P("on_yellow").map(_ => BackgroundColor.Yellow)
  def onMagenta[_: P]: P[BackgroundColor] = P("on_magenta").map(_ => BackgroundColor.Magenta)
  def onWhite[_: P]: P[BackgroundColor] = P("on_white").map(_ => BackgroundColor.White)
  def onBlue[_: P]: P[BackgroundColor] = P("on_blue").map(_ => BackgroundColor.Blue)
  def onCyan[_: P]: P[BackgroundColor] = P("on_cyan").map(_ => BackgroundColor.Cyan)

  def onBrightRed[_: P]: P[BackgroundColor] = P("on_bright_red").map(_ => BackgroundColor.BrightRed)
  def onBrightGreen[_: P]: P[BackgroundColor] = P("on_bright_green").map(_ => BackgroundColor.BrightGreen)
  def onBrightYellow[_: P]: P[BackgroundColor] = P("on_bright_yellow").map(_ => BackgroundColor.BrightYellow)
  def onBrightMagenta[_: P]: P[BackgroundColor] = P("on_bright_magenta").map(_ => BackgroundColor.BrightMagenta)
  def onBrightBlue[_: P]: P[BackgroundColor] = P("on_bright_blue").map(_ => BackgroundColor.BrightBlue)
  def onBrightCyan[_: P]: P[BackgroundColor] = P("on_bright_cyan").map(_ => BackgroundColor.BrightCyan)
  def onDarkGray[_: P]: P[BackgroundColor] = P("on_dark_gray").map(_ => BackgroundColor.DarkGray)
  def onLightGray[_: P]: P[BackgroundColor] = P("on_light_gray").map(_ => BackgroundColor.LightGray)

  def backgroundColor[_: P]: P[BackgroundColor] =
    P(
      onBlack | onRed | onGreen | onYellow | onBlue | onMagenta | onWhite | onCyan
        | onBrightRed | onBrightGreen | onBrightYellow | onBrightBlue | onBrightCyan
        | onDarkGray | onLightGray
    )

  // Alignment
  def left[_: P]: P[Alignment] = P("<").map(_ => Alignment.Left)
  def center[_: P]: P[Alignment] = P("^").map(_ => Alignment.Center)
  def right[_: P]: P[Alignment] = P(">").map(_ => Alignment.Right)
  def alignment[_: P]: P[Alignment] = P((left | center | right))

  // Width
  def width[_: P] = P(CharIn("0-9").rep(1).!.map(_.toInt))

  // Attribute
  def bold[_: P]: P[Attribute] = P("bold").map(_ => Attribute.Bold)
  def underlined[_: P]: P[Attribute] = P("underlined").map(_ => Attribute.Underlined)
  def reverse[_: P]: P[Attribute] = P("reverse").map(_ => Attribute.Reverse)
  // def dim[_: P]: P[Attribute] = P("dim").map(_ => Attribute.Dim)
  // def slowBlink[_: P]: P[Attribute] = P("slow_blink").map(_ => Attribute.SlowBlink)
  // def rapidBlink[_: P]: P[Attribute] = P("rapid_blink").map(_ => Attribute.RapidBlink)
  // def hidden[_: P]: P[Attribute] = P("hidden").map(_ => Attribute.Hidden)
  def attribute[_: P]: P[Attribute] =
    P(
      bold | underlined | reverse
      // |  slowBlink | rapidBlink | hidden | dim
    )

  // Style
  def style[_: P] =
    P(("." ~ foregroundColor).? ~ ("." ~ backgroundColor).? ~ ("." ~ attribute).rep).map {
      case (fgOpt, bgOpt, attrs) => Style(fg = fgOpt, bg = bgOpt, attributes = attrs)
    }

  // Variable
  def variable[_: P]: P[TemplateElement] = P("{" ~ key ~ (":" ~ alignment.? ~ width.? ~ style.?).? ~ "}").map {
    case (key, Some((alignmentOpt, widthOpt, styleOpt))) =>
      TemplateElement.Var(
        key = key,
        align = alignmentOpt.getOrElse(Alignment.Left),
        width = widthOpt,
        style = styleOpt.getOrElse(Style.empty())
      )
    case (key, _) =>
      TemplateElement.Var(
        key = key,
        align = Alignment.Left,
        width = None,
        style = Style.empty()
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
