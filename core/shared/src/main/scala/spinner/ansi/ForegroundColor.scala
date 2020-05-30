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

sealed trait ForegroundColor {
  def toAnsiCode: String
}

object ForegroundColor {
  final case object Black extends ForegroundColor {
    override def toAnsiCode: String = Console.BLACK
  }
  final case object BrightBlack extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[30;1m"
  }
  final case object Red extends ForegroundColor {
    override def toAnsiCode: String = Console.RED
  }
  final case object BrightRed extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[31;1m"
  }
  final case object Green extends ForegroundColor {
    override def toAnsiCode: String = Console.GREEN
  }
  final case object BrightGreen extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[32;1m"
  }
  final case object Yellow extends ForegroundColor {
    override def toAnsiCode: String = Console.YELLOW
  }
  final case object BrightYellow extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[33;1m"
  }
  final case object Blue extends ForegroundColor {
    override def toAnsiCode: String = Console.BLUE
  }
  final case object BrightBlue extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[34;1m"
  }
  final case object Magenta extends ForegroundColor {
    override def toAnsiCode: String = Console.MAGENTA
  }
  final case object BrightMagenta extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[35;1m"
  }
  final case object Cyan extends ForegroundColor {
    override def toAnsiCode: String = Console.CYAN
  }
  final case object BrightCyan extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[36;1m"
  }
  final case object White extends ForegroundColor {
    override def toAnsiCode: String = Console.WHITE
  }
  final case object BrightWhite extends ForegroundColor {
    override def toAnsiCode: String = "\u001b[37;1m"
  }
}
