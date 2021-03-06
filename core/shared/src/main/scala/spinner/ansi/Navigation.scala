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

sealed trait Navigation {
  def toAnsiCode: String
}

object Navigation {
  final case class Up(steps: Int) extends Navigation {
    override def toAnsiCode: String = s"\u001b[${steps}A"
  }
  final case class Down(steps: Int) extends Navigation {
    override def toAnsiCode: String = s"\u001b[${steps}B"
  }
  final case class Right(steps: Int) extends Navigation {
    override def toAnsiCode: String = s"\u001b[${steps}C"
  }
  final case class Left(steps: Int) extends Navigation {
    override def toAnsiCode: String = s"\u001b[${steps}D"
  }
}
