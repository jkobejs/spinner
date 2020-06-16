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

import fansi.Attr

sealed trait ForegroundColor {
  def toAnsiCode: String
  def toFansi: fansi.Attr
}

object ForegroundColor {
  final case object Black extends ForegroundColor {
    override def toAnsiCode: String = Console.BLACK
    override def toFansi: Attr = fansi.Color.Black
  }
  final case object Red extends ForegroundColor {
    override def toAnsiCode: String = Console.RED
    override def toFansi: Attr = fansi.Color.Red
  }
  final case object BrightRed extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[31;1m"
    override def toFansi: Attr = fansi.Color.LightRed
  }
  final case object Green extends ForegroundColor {
    override def toAnsiCode: String = Console.GREEN
    override def toFansi: Attr = fansi.Color.Green
  }
  final case object BrightGreen extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[32;1m"
    override def toFansi: Attr = fansi.Color.LightGreen
  }
  final case object Yellow extends ForegroundColor {
    override def toAnsiCode: String = Console.YELLOW
    override def toFansi: Attr = fansi.Color.Yellow
  }
  final case object BrightYellow extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[33;1m"
    override def toFansi: Attr = fansi.Color.LightYellow
  }
  final case object Blue extends ForegroundColor {
    override def toAnsiCode: String = Console.BLUE
    override def toFansi: Attr = fansi.Color.Blue
  }
  final case object BrightBlue extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[34;1m"
    override def toFansi: Attr = fansi.Color.LightBlue
  }
  final case object Magenta extends ForegroundColor {
    override def toAnsiCode: String = Console.MAGENTA
    override def toFansi: Attr = fansi.Color.Magenta
  }
  final case object BrightMagenta extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[35;1m"
    override def toFansi: Attr = fansi.Color.LightMagenta
  }
  final case object Cyan extends ForegroundColor {
    override def toAnsiCode: String = Console.CYAN
    override def toFansi: Attr = fansi.Color.Cyan
  }
  final case object BrightCyan extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[36;1m"
    override def toFansi: Attr = fansi.Color.LightCyan
  }
  final case object White extends ForegroundColor {
    override def toAnsiCode: String = Console.WHITE
    override def toFansi: Attr = fansi.Color.White
  }
  final case object DarkGray extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[37;1m"
    override def toFansi: Attr = fansi.Color.DarkGray
  }
  final case object LightGray extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[37;1m"
    override def toFansi: Attr = fansi.Color.LightGray
  }
}
