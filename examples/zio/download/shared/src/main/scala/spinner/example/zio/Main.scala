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

package spinner.example.zio

import zio.duration.Duration
import java.util.concurrent.TimeUnit
import spinner.zio._
import zio._
import spinner.SpinnerStyle
import spinner.template.interpolator._

object Main extends zio.App {

  def run(args: List[String]) =
    myAppLogic.exitCode

  val myAppLogic = {

    val totalSize = 231231231L

    for {
      pb <- ProgresBar
        .defaultBar(totalSize)
        .withStyle(
          SpinnerStyle.defaultBar
            .withProgressChars("#>-")
            .withTemplate(template"{spinner:.green} [{elapsed_precise:8}] [{bar:40.cyan}] {bytes}/{total_bytes} {eta}")
            .build())
        .build()
      downloaded <- Ref.make(0L)
      _ <- (for {
        newPosition <- downloaded.updateAndGet(d => Math.min(d + 223211L, totalSize))
        _           <- clock.sleep(Duration(12, TimeUnit.MILLISECONDS))
        _           <- pb.setPosition(newPosition)
      } yield newPosition).doWhile(_ < totalSize)
      _ <- pb.finishWithMessage("downloaded")
    } yield ()
  }
}
