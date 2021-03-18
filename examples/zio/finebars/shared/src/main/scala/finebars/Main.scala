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

// import zio.duration.Duration
// import java.util.concurrent.TimeUnit
// import spinner.zio._
// import spinner.template.Template

object Main {
// extends zio.App {

//   def run(args: List[String]) =
//     myAppLogic.exitCode

//   val myAppLogic = {
//     val styles = Seq(
//       ("Rough bar:", "█  ", "{prefix:.bold} ▕{bar:.red}▏{msg:}"),
//       ("Fine bar: ", "█▉▊▋▌▍▎▏  ", "{prefix:.bold} ▕{bar:.yellow}▏{msg:}"),
//       ("Vertical: ", "█▇▆▅▄▃▂▁  ", "{prefix:.bold} ▕{bar:.green}▏{msg:}"),
//       ("Fade in:  ", "█▓▒░  ", "{prefix:.bold} ▕{bar:.blue}▏{msg:}"),
//       ("Blocky:   ", "█▛▌▖  ", "{prefix:.bold} ▕{bar:.magenta}▏{msg:}")
//     )

//     zio.ZIO
//       .foreach(styles) {
//         case (prefix, progressChars, template) =>
//           zio.random.nextLongBetween(10, 20).map(wait => createProgressBar(prefix, progressChars, template, wait))
//       }
//       .flatMap(
//         bars =>
//           MultiProgressBar
//             .builder()
//             .progressBars(bars: _*)
//             .start())
//   }

//   private def createProgressBar(prefix: String, progressChars: String, template: String, wait: Long) = {
//     val len = 512
//     ProgressBar
//       .builder(state =>
//         zio.ZIO.foreach(0 to len) { i =>
//           for {
//             _ <- state.update(f"${100 * i / len}%3d%%", i)
//             _ <- zio.clock.sleep(Duration(wait, TimeUnit.MILLISECONDS))
//           } yield ()
//         })
//       .withTemplate(template)
//       .withProgressChars(progressChars)
//       .withLen(len)
//       .withPrefix(prefix)
//       .build
//   }
}
