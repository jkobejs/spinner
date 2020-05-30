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

import fastparse._, NoWhitespace._

object Parser {
  def spinner[_: P]: P[Key] = P("spinner").map(_ => Key.Spinner)
  def msg[_: P]: P[Key] = P("msg").map(_ => Key.Message)
  def elapsed_precise[_: P]: P[Key] = P("elapsed_precise").map(_ => Key.ElapsedPrecise)
  def elapsed[_: P]: P[Key] = P("elapsed").map(_ => Key.Elapsed)
  def prefix[_: P]: P[Key] = P("prefix").map(_ => Key.Prefix)
  def key[_: P]: P[Key] = P(spinner | msg | elapsed_precise | elapsed | prefix)

  // Color
  def black[_: P]: P[Color] = P("black").map(_ => Color.Black)
  def red[_: P]: P[Color] = P("red").map(_ => Color.Red)
  def green[_: P]: P[Color] = P("green").map(_ => Color.Green)
  def yellow[_: P]: P[Color] = P("yellow").map(_ => Color.Yellow)
  def magenta[_: P]: P[Color] = P("magenta").map(_ => Color.Magenta)
  def white[_: P]: P[Color] = P("white").map(_ => Color.White)
  def blue[_: P]: P[Color] = P("blue").map(_ => Color.Blue)

  def foregroundColor[_: P]: P[ForegroundColor] =
    P(black | red | green | yellow | blue | magenta | white).map(ForegroundColor(_))

  def backgroundColor[_: P]: P[BackgrounColor] =
    P("on_" ~ (black | red | green | yellow | blue | magenta | white)).map(BackgrounColor(_))

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
  def blink[_: P]: P[Attribute] = P("blink").map(_ => Attribute.Blink)
  def reverse[_: P]: P[Attribute] = P("reverse").map(_ => Attribute.Reverse)
  def hidden[_: P]: P[Attribute] = P("hidden").map(_ => Attribute.Hidden)
  def attribute[_: P]: P[Attribute] = P(bold | dim | underlined | blink | reverse | hidden)

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
