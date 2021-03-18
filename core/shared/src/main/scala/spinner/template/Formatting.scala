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

import java.util.concurrent.TimeUnit
import scala.annotation.tailrec

object Formatting {
  def humanReadableTime(timestamp: Long): String = {
    val hours = TimeUnit.MILLISECONDS.toHours(timestamp)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.HOURS.toMinutes(
      TimeUnit.MILLISECONDS.toHours(timestamp))
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MINUTES.toSeconds(
      TimeUnit.MILLISECONDS.toMinutes(timestamp))
    Seq(if (hours > 0) s"${hours}h" else "", if (minutes > 0) s"${minutes}m" else "", s"${seconds}s")
      .filter(_.nonEmpty)
      .mkString(" ")
  }

  def preciseTime(timestamp: Long): String =
    "%02d:%02d:%02d".format(
      TimeUnit.MILLISECONDS.toHours(timestamp),
      TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timestamp)),
      TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MINUTES.toSeconds(
        TimeUnit.MILLISECONDS.toMinutes(timestamp))
    )

  /**
    * Converts a number of bytes to human-readable string using base 10 SI units where 1000 bytes are 1kB.
    *
    * See https://en.wikipedia.org/wiki/Byte
    *
    * @param bytes the number of bytes we want to convert
    * @return the bytes as a human-readable string
    */
  def humanReadableSizeSI(bytes: Long): String = {
    val baseValue = 1000
    val unitStrings = Vector("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")

    val exponent = getExponent(bytes, baseValue)

    val divisor = Math.pow(baseValue.toDouble, exponent.toDouble)
    val unitString = unitStrings(exponent)

    // Divide the bytes and show 2 digits after the decimal point
    f"${bytes / divisor}%.2f$unitString"
  }

  /**
    * Converts a number of bytes to human-readable string using base 10 IEC units where 1024 bytes are 1KiB.
    *
    * See https://en.wikipedia.org/wiki/Byte
    *
    * @param bytes the number of bytes we want to convert
    * @return the bytes as a human-readable string
    */
  def humanReadableSizeIEC(bytes: Long): String = {
    val baseValue = 1024
    val unitStrings = Vector("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB")

    val exponent = getExponent(bytes, baseValue)

    val divisor = Math.pow(baseValue.toDouble, exponent.toDouble)
    val unitString = unitStrings(exponent)

    // Divide the bytes and show 2 digits after the decimal point
    f"${bytes / divisor}%.2f$unitString"
  }

  @tailrec
  private def getExponent(curBytes: Long, baseValue: Int, curExponent: Int = 0): Int =
    if (curBytes < baseValue) curExponent
    else {
      val newExponent = 1 + curExponent
      getExponent(curBytes / (baseValue * newExponent), baseValue, newExponent)
    }
}
