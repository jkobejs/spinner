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

package spinner.template

import contextual._
import fastparse.parse
import fastparse.Parsed

object interpolator {

  object TemplateInterpolator extends Interpolator {
    type Output = Template
    type Input = String

    def contextualize(interpolation: StaticInterpolation): Seq[ContextType] = {
      val lit @ Literal(_, templateString) = interpolation.parts.head

      parse(templateString, Parser.template(_), verboseFailures = true) match {
        case f @ Parsed.Failure(_, _, _) =>
          interpolation.abort(lit, 0, f.trace().longMsg)
        case _ => Nil
      }
    }

    def evaluate(interpolation: RuntimeInterpolation): Template =
      parse(interpolation.literals.head, Parser.template(_)).get.value
  }

  implicit class TemplateContext(sc: StringContext) { val template = Prefix(TemplateInterpolator, sc) }
}
