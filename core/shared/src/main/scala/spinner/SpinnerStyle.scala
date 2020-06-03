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

case class SpinnerStyle private (
  spinner: Seq[String],
  template: Template
)

object SpinnerStyle {
  val default: SpinnerStyle =
    SpinnerStyle(
      spinner = "⠁⠁⠉⠙⠚⠒⠂⠂⠒⠲⠴⠤⠄⠄⠤⠠⠠⠤⠦⠖⠒⠐⠐⠒⠓⠋⠉⠈⠈ ".toCharArray().map(_.toString).toSeq,
      template = Template(
        Seq(
          TemplateElement.Var(Key.Prefix),
          TemplateElement.Var(Key.Spinner),
          TemplateElement.Val(" "),
          TemplateElement.Var(Key.Message),
          TemplateElement.Val(" Elapsed: "),
          TemplateElement.Var(Key.Elapsed)
        ))
    )
}
