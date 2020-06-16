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

package spinner.ansi

sealed trait BackgroundColor {
  def toAnsiCode: String
  def toFansi: fansi.Attrs
}

object BackgroundColor {
  final case object Black extends BackgroundColor {
    override def toAnsiCode: String = Console.BLACK_B
    override def toFansi: fansi.Attrs = fansi.Back.Black
  }
  final case object Red extends BackgroundColor {
    override def toAnsiCode: String = Console.RED_B
    override def toFansi: fansi.Attrs = fansi.Back.Red
  }
  final case object BrightRed extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[41;1m"
    override def toFansi: fansi.Attrs = fansi.Back.LightRed
  }
  final case object Green extends BackgroundColor {
    override def toAnsiCode: String = Console.GREEN_B
    override def toFansi: fansi.Attrs = fansi.Back.Green
  }
  final case object BrightGreen extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[42;1m"
    override def toFansi: fansi.Attrs = fansi.Back.LightGreen
  }
  final case object Yellow extends BackgroundColor {
    override def toAnsiCode: String = Console.YELLOW_B
    override def toFansi: fansi.Attrs = fansi.Back.Yellow
  }
  final case object BrightYellow extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[43;1m"
    override def toFansi: fansi.Attrs = fansi.Back.LightYellow
  }
  final case object Blue extends BackgroundColor {
    override def toAnsiCode: String = Console.BLUE_B
    override def toFansi: fansi.Attrs = fansi.Back.Blue
  }
  final case object BrightBlue extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[44;1m"
    override def toFansi: fansi.Attrs = fansi.Back.LightBlue
  }
  final case object Magenta extends BackgroundColor {
    override def toAnsiCode: String = Console.MAGENTA_B
    override def toFansi: fansi.Attrs = fansi.Back.Magenta
  }
  final case object BrightMagenta extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[45;1m"
    override def toFansi: fansi.Attrs = fansi.Back.LightMagenta
  }
  final case object Cyan extends BackgroundColor {
    override def toAnsiCode: String = Console.CYAN_B
    override def toFansi: fansi.Attrs = fansi.Back.Cyan
  }
  final case object BrightCyan extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[46;1m"
    override def toFansi: fansi.Attrs = fansi.Back.LightCyan
  }
  final case object White extends BackgroundColor {
    override def toAnsiCode: String = Console.WHITE_B
    override def toFansi: fansi.Attrs = fansi.Back.White

  }
  final case object DarkGray extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[37;1m"
    override def toFansi: fansi.Attr = fansi.Back.DarkGray
  }
  final case object LightGray extends BackgroundColor {
    override def toAnsiCode: String = "\u001b[37;1m"
    override def toFansi: fansi.Attr = fansi.Back.LightGray
  }
}
