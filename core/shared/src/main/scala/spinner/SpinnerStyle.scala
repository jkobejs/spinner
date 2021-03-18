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

import spinner.template._
import fastparse.parse
import fastparse.Parsed

case class ProgressChars(
  )

case class SpinnerStyle private (
  spinner: Seq[String],
  template: Template,
  progressChars: Array[Char]
)

object SpinnerStyle {
  case class Builder(
    spinner: Seq[String],
    template: Template,
    progressChars: Array[Char]
  ) {
    def withSpinner(spinnerString: String) = copy(spinner = spinnerString.toCharArray().map(_.toString).toSeq)
    def withSpinner(spinner: Seq[String]) = copy(spinner = spinner)
    def withTemplate(template: Template) = copy(template = template)
    def withTemplate(template: String) = {

      val tmplate =
        parse(template, Parser.template(_)) match {
          case Parsed.Failure(_, _, _) =>
            Template(Seq(TemplateElement.Val(template)))

          case Parsed.Success(template, _) => template
        }

      copy(template = tmplate)
    }
    def withProgressChars(progressChars: String) = copy(progressChars = progressChars.toCharArray())

    def build() = SpinnerStyle(
      spinner = spinner,
      template = template,
      progressChars = progressChars
    )
  }
  val default: SpinnerStyle =
    SpinnerStyle(
      spinner = "⠁⠁⠉⠙⠚⠒⠂⠂⠒⠲⠴⠤⠄⠄⠤⠠⠠⠤⠦⠖⠒⠐⠐⠒⠓⠋⠉⠈⠈ ".toCharArray().map(_.toString).toSeq,
      template = Template(
        Seq(
          TemplateElement.Var(Key.Bar, width = Some(20)),
          TemplateElement.Val(" "),
          TemplateElement.Var(Key.Position),
          TemplateElement.Val("/"),
          TemplateElement.Var(Key.Length)
        )),
      progressChars = Array('█', '░')
    )

  val defaultBar: Builder =
    Builder(
      spinner = "⠁⠁⠉⠙⠚⠒⠂⠂⠒⠲⠴⠤⠄⠄⠤⠠⠠⠤⠦⠖⠒⠐⠐⠒⠓⠋⠉⠈⠈ ".toCharArray().map(_.toString).toSeq,
      template = Template(
        Seq(
          TemplateElement.Var(Key.Bar, width = Some(20)),
          TemplateElement.Val(" "),
          TemplateElement.Var(Key.Position),
          TemplateElement.Val("/"),
          TemplateElement.Var(Key.Length)
        )),
      progressChars = Array('█', '░')
    )

  val defaultSpinner: Builder =
    Builder(
      spinner = "⠁⠁⠉⠙⠚⠒⠂⠂⠒⠲⠴⠤⠄⠄⠤⠠⠠⠤⠦⠖⠒⠐⠐⠒⠓⠋⠉⠈⠈ ".toCharArray().map(_.toString).toSeq,
      template = Template(
        Seq(
          TemplateElement.Var(Key.Spinner),
          TemplateElement.Val(" "),
          TemplateElement.Var(Key.Message)
        )),
      progressChars = Array('█', '░')
    )
}
