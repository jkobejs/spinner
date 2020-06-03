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

package spinner.example.cats

import cats.effect._
import scala.concurrent.duration._
import spinner.cats.Spinner
import spinner.cats.State

object Main extends IOApp {

  def program(args: List[String]): IO[ExitCode] = {
    import spinner.template.interpolator._
    val indicator1 = Spinner(
      spinner = "⠁⠁⠉⠙⠚⠒⠂⠂⠒⠲⠴⠤⠄⠄⠤⠠⠠⠤⠦⠖⠒⠐⠐⠒⠓⠋⠉⠈⠈ ",
      template = template"{prefix:} {spinner:.blue} {msg:>.green} {elapsed_precise:.blue}",
      message = "Spinning...",
      prefix = "[1/3]"
    )

    val indicator2 = Spinner(
      spinner = List("🙈 ", "🙈 ", "🙉 ", "🙊 "),
      template = template"{prefix:} {spinner:} {msg:<.green.rapid_blink} {elapsed_precise:.blue}",
      message = "",
      prefix = "[2/3]"
    )

    val indicator3 = Spinner(
      spinner = List(
        "( ●    )",
        "(  ●   )",
        "(   ●  )",
        "(    ● )",
        "(     ●)",
        "(    ● )",
        "(   ●  )",
        "(  ●   )",
        "( ●    )",
        "(●     )"),
      template = template"{prefix:} {spinner:} {msg:^.green} {elapsed_precise:.blue.on_white}",
      message = "Spinning...",
      prefix = "[3/3]"
    )

    for {
      _ <- IO(println(args))
      _ <- IO(println("Starting spinner!"))
      _ <- indicator1.spin(IO.sleep(1.second))
      _ <- IO(println(""))
      _ <- indicator2.spinWithState { state: State[IO] =>
        for {
          _ <- state.updateMessage("1st effect")
          _ <- IO.sleep(1.second)
          _ <- state.updateMessage("2nd effect")
          _ <- IO.sleep(1.second)
          _ <- state.updateMessage("3rd effect")
          _ <- IO.sleep(1.second)
        } yield ()
      }
      _ <- IO(println(""))
      _ <- indicator3.spin(IO.sleep(5.second))

      _ <- IO(println("\nDone"))
    } yield ExitCode.Success
  }

  override def run(args: List[String]): IO[ExitCode] =
    program(args)
}
