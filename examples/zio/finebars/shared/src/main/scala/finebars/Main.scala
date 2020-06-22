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

package finebars

import zio.duration.Duration
import java.util.concurrent.TimeUnit
import spinner.zio._
import spinner.template.Template

object Main extends zio.App {

  def run(args: List[String]) =
    myAppLogic.exitCode

  val myAppLogic = {
    import spinner.template.interpolator._

    // val mp = MultiProgress()
    // for {
    //   pb <- mp.add(ProgressBar.new(5))
    //   _ <- ZIO.foreach(0 until 5) { _ =>
    //     for {
    //       pb2 <- mp.add(ProgressBar::new(128))
    //       -   <- ZIO.foreach(0 until 128) { _ =>
    //           for {
    //             _ <- pb2.inc(1)
    //             _ <- clock.sleep(5.millis)
    //           } yield ()
    //       _ <- pb2.finish()
    //       _ <- pb.inc(1)
    //     } yield ()
    //   }
    //   _ <- pb.finishWithMessage("Done")
    //   _ <- mp.join()
    // } yield ()

    // val mp = MultiProgress()

    // for {
    //   _ <- ZIO.foreach(styles) { style =>
    //     for {
    //       pb   <- mp.add(ProgressBar.new(512))
    //       wait <- zio.random.nextLongBetween(10, 20)
    //       _ <- ZIO.foreach(0 until 512) { i =>
    //         for {
    //           _ <- pb.inc(1)
    //           _ <- pb.set_message("")
    //           _ < clock.sleep(wait)
    //         } yield ()
    //           }
    //       pb.finishWithMessage("100%")
    //     } yield ()
    //   }
    // } yield ()

    val styles = Seq(
      ("Rough bar:", "█  ", template"{prefix:.bold} ▕{bar:.red}▏{msg:}"),
      ("Fine bar: ", "█▉▊▋▌▍▎▏  ", template"{prefix:.bold} ▕{bar:.yellow}▏{msg:}"),
      ("Vertical: ", "█▇▆▅▄▃▂▁  ", template"{prefix:.bold} ▕{bar:.green}▏{msg:}"),
      ("Fade in:  ", "█▓▒░  ", template"{prefix:.bold} ▕{bar:.blue}▏{msg:}"),
      ("Blocky:   ", "█▛▌▖  ", template"{prefix:.bold} ▕{bar:.magenta}▏{msg:}")
    )

    zio.ZIO
      .foreach(styles) {
        case (prefix, progressChars, template) =>
          zio.random.nextLongBetween(10, 20).map(wait => createProgressBar(prefix, progressChars, template, wait))
      }
      .flatMap(
        bars =>
          MultiProgressBar
            .builder()
            .progressBars(bars: _*)
            .start())
  }

  private def createProgressBar(prefix: String, progressChars: String, template: Template, wait: Long) = {
    val len = 512
    ProgressBar
      .builder(state =>
        zio.ZIO.foreach(0 to len) { i =>
          for {
            _ <- state.update(f"${100 * i / len}%3d%%", i)
            _ <- zio.clock.sleep(Duration(wait, TimeUnit.MILLISECONDS))
          } yield ()
        })
      .withTemplate(template)
      // .withTemplate(template"{prefix:.bold} ▕{bar:.${color}▏{msg:}")
      .withProgressChars(progressChars)
      .withLen(len)
      .withPrefix(prefix)
      .build
  }
}
