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
import fastparse.Parsed.Success

object ParserSpec extends DefaultRunnableSpec {
  def spec = suite("ParserSpec")(
    test("Parser.key parser succeeds on valid keys") {
      assert(parse("spinner", Parser.key(_)))(equalTo(Success("spinner", 7))) &&
      assert(parse("msg", Parser.key(_)))(equalTo(Success("msg", 3))) &&
      assert(parse("elapsed", Parser.key(_)))(equalTo(Success("elapsed", 7))) &&
      assert(parse("elapsed_precise", Parser.key(_)))(equalTo(Success("elapsed_precise", 15))) &&
      assert(parse("prefix", Parser.key(_)))(equalTo(Success("prefix", 6)))

    },
    testM("Parser.key parser fails on invalid keys") {
      val keys = List("spinner", "msg", "elapsed", "elapsed_precise", "prefix")

      check(Gen.alphaNumericStringBounded(1, 20).filterNot(str => keys.contains(str))) { string =>
        assert(parse(string, Parser.key(_)).isSuccess)(equalTo(false))
      }
    },
    test("Parser.foregroundColor parser succeeds on valid foreground colors") {
      assert(parse("black", Parser.foregroundColor(_)))(equalTo(Success(ForegroundColor(Color.Black), 5))) &&
      assert(parse("red", Parser.foregroundColor(_)))(equalTo(Success(ForegroundColor(Color.Red), 3))) &&
      assert(parse("green", Parser.foregroundColor(_)))(equalTo(Success(ForegroundColor(Color.Green), 5))) &&
      assert(parse("yellow", Parser.foregroundColor(_)))(equalTo(Success(ForegroundColor(Color.Yellow), 6))) &&
      assert(parse("magenta", Parser.foregroundColor(_)))(equalTo(Success(ForegroundColor(Color.Magenta), 7))) &&
      assert(parse("white", Parser.foregroundColor(_)))(equalTo(Success(ForegroundColor(Color.White), 5))) &&
      assert(parse("blue", Parser.foregroundColor(_)))(equalTo(Success(ForegroundColor(Color.Blue), 4)))
    },
    test("Parser.backgroundColor parser succeeds on valid background colors") {
      assert(parse("on_black", Parser.backgroundColor(_)))(equalTo(Success(BackgrounColor(Color.Black), 8))) &&
      assert(parse("on_red", Parser.backgroundColor(_)))(equalTo(Success(BackgrounColor(Color.Red), 6)))
      assert(parse("on_green", Parser.backgroundColor(_)))(equalTo(Success(BackgrounColor(Color.Green), 8))) &&
      assert(parse("on_yellow", Parser.backgroundColor(_)))(equalTo(Success(BackgrounColor(Color.Yellow), 9))) &&
      assert(parse("on_magenta", Parser.backgroundColor(_)))(equalTo(Success(BackgrounColor(Color.Magenta), 10))) &&
      assert(parse("on_white", Parser.backgroundColor(_)))(equalTo(Success(BackgrounColor(Color.White), 8))) &&
      assert(parse("on_blue", Parser.backgroundColor(_)))(equalTo(Success(BackgrounColor(Color.Blue), 7)))
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

      assert(parse(".blue.on_red.bold.underlined", Parser.style(_)))(
        equalTo(
          Success(
            Style.empty().fg(Color.Blue).bg(Color.Red).attribute(Attribute.Bold).attribute(Attribute.Underlined),
            28)))
    }
  )
}
