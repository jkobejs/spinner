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

object Main extends zio.App {

  def run(args: List[String]) =
    myAppLogic.exitCode

  val myAppLogic = {
    import spinner.template.interpolator._

    val totalSize = 231023385

    def download(totalSize: Int): State => zio.ZIO[zio.clock.Clock, Nothing, Unit] =
      state =>
        for {
          ref <- zio.ZRef.make(0)
          _ <- (for {
            downloaded <- ref.get
            newPosition = Math.min(downloaded + 223211, totalSize)
            _ <- state.updatePosition(newPosition)
            _ <- zio.clock.sleep(Duration(12, TimeUnit.MILLISECONDS))
            _ <- ref.set(newPosition)
          } yield newPosition).doWhile(_ < totalSize)
        } yield ()

    val progressBarSpinnerBuilder =
      ProgressBar.builder { download(totalSize) }
        .withTemplate(template"{spinner:4.green} [{elapsed_precise:8}] [{bar:40.cyan}] {pos:>}/{len:} {eta:}")
        .withLen(totalSize)
        .withProgressChars("#>-")
    val mpParallel = MultiProgressBar
      .builder()
      .withHeader("Download")
      .progressBar(progressBarSpinnerBuilder.build)

    for {
      _ <- mpParallel.start()
    } yield ()
  }
}
