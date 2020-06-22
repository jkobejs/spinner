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

package single

import zio.duration.Duration
import java.util.concurrent.TimeUnit
import spinner.zio._
import zio._

object Main extends zio.App {

  def run(args: List[String]) =
    myAppLogic.exitCode

  val myAppLogic = {
    for {
      pb <- ProgresBar.create(1024)
      _  <- ZIO.foreach(0 until 1024)(_ => pb.advance(1) *> clock.sleep(Duration(5, TimeUnit.MILLISECONDS)))
      _  <- pb.finishWithMessage("done")
    } yield ()
  }
}
