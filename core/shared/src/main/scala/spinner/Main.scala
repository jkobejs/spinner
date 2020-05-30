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

package spinner

import zio.console._
import zio.IO
import zio.Schedule
import zio.duration.Duration
import java.util.concurrent.TimeUnit

object Main extends zio.App {

  def run(args: List[String]) =
    myAppLogic.exitCode

  val myAppLogic =
    for {
      _ <- putStrLn("Starting spinner!")
      _ <- Spinner.default().spin(IO.none.repeat(Schedule.duration(Duration(3, TimeUnit.SECONDS))))
      _ <- putStrLn("")
      _ <- Spinner.default().spinWithState { state =>
        for {
          _ <- state.updateMessage("First effect")
          _ <- IO.none.repeat(Schedule.duration(Duration(3, TimeUnit.SECONDS)))
          _ <- state.updateMessage("Second effect")
          _ <- IO.none.repeat(Schedule.duration(Duration(2, TimeUnit.SECONDS)))
          _ <- state.updateMessage("Thi effect")
          _ <- IO.none.repeat(Schedule.duration(Duration(1, TimeUnit.SECONDS)))
        } yield ()
      }

      _ <- putStrLn(s"Done")
    } yield ()
}
