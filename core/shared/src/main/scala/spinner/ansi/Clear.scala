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

sealed trait Clear {
  def toAnsiCode: String
}

object Clear {
  final case object UntilEndOfScreen extends Clear {
    override def toAnsiCode: String = "\u001b[0J"
  }
  final case object ToBeginningOfScreen extends Clear {
    override def toAnsiCode: String = "\u001b[1J"
  }
  final case object EntireScreen extends Clear {
    override def toAnsiCode: String = "\u001b[2J"
  }

  final case object UntilEndOfLine extends Clear {
    override def toAnsiCode: String = "\u001b[0K"
  }
  final case object ToBeginningOfLine extends Clear {
    override def toAnsiCode: String = "\u001b[1K"
  }
  final case object EntireLine extends Clear {
    override def toAnsiCode: String = "\u001b[2K"
  }
}
