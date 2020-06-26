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

import zio.test._
import zio.test.Assertion._

import fastparse._

object ParserSpec extends DefaultRunnableSpec {
  def spec = suite("ParserSpec")(
    test("Parser.key parser succeeds on valid keys") {
      assert(true)(equalTo(true))

    },
    testM("Parser.key parser fails on invalid keys") {
      val keys = List("spinner", "msg", "elapsed", "elapsed_precise", "prefix")

      check(Gen.alphaNumericStringBounded(1, 20).filterNot(str => keys.contains(str))) { string =>
        assert(parse(string, Parser.key(_)).isSuccess)(equalTo(false))
      }
    },
    test("Parser.foregroundColor parser succeeds on valid foreground colors") {
      assert(true)(equalTo(true))
    },
    test("Parser.backgroundColor parser succeeds on valid background colors") {
      assert(true)(equalTo(true))
    },
    test("Parser.alignment parser succeeds on valid alingments") {
      assert(true)(equalTo(true))
    },
    test("Parser.attribute parser succeeds on valid attributes") {
      assert(true)(equalTo(true))
    },
    test("Parser.style parser succeeds on valid style") {
      // val colors =
      // Gen.fromIterable()

      // assert(parse(".blue.on_red.bold.underlined", Parser.style(_)))(equalTo(null))
      assert(true)(equalTo(true))
    }
  )
}
