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

trait Attribute {
  def toAnsiCode: String
}

object Attribute {
  final case object Reset extends Attribute {
    override def toAnsiCode: String = Console.RESET
  }
  final case object Bold extends Attribute {
    override def toAnsiCode: String = Console.BOLD
  }
  final case object Dim extends Attribute {
    override def toAnsiCode: String = "\u001b[2m"
  }
  final case object Italic extends Attribute {
    override def toAnsiCode: String = "\u001b[3m"
  }
  final case object Underlined extends Attribute {
    override def toAnsiCode: String = Console.UNDERLINED
  }
  final case object SlowBlink extends Attribute {
    override def toAnsiCode: String = Console.BLINK
  }
  final case object RapidBlink extends Attribute {
    override def toAnsiCode: String = "\u001b[6m"
  }
  final case object Reverse extends Attribute {
    override def toAnsiCode: String = Console.REVERSED
  }
  final case object Hidden extends Attribute {
    override def toAnsiCode: String = Console.INVISIBLE
  }
}
