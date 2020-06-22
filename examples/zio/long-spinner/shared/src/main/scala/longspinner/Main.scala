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

package longspinner

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
    for {
      pb <- ProgresBar.defaultSpinner
        .withStyle(
          SpinnerStyle.defaultSpinner
            .withSpinner(
              Seq("▹▹▹▹▹", "▸▹▹▹▹", "▹▸▹▹▹", "▹▹▸▹▹", "▹▹▹▸▹", "▹▹▹▹▸", "▪▪▪▪▪")
            )
            .withTemplate(template"{spinner:.blue} {msg:}")
            .build())
        .build()
      _ <- pb.enableSteadyTick(120)
      _ <- pb.setMessage("Calculating...")
      _ <- clock.sleep(Duration(5, TimeUnit.SECONDS))
      _ <- pb.finishWithMessage("Done")
    } yield ()
  }
}
