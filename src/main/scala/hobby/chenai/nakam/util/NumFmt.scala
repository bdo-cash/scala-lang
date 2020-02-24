/*
 * Copyright (C) 2017-present, Chenai Nakam(chenai.nakam@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hobby.chenai.nakam.util

import java.text.{DecimalFormat, NumberFormat}

import scala.math.BigDecimal

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 16/07/2017;
  *          2.0, 24/02/2020, 当前版本使用`Double`可能造成精度问题，改成了`BigDecimal(String)`版本。
  */
trait NumFmt {
  /**
    * 用法示例：
    * {{{
    * formatted(12) {
    *   val f: DecimalFormat = new DecimalFormat
    *   f.setGroupingSize(3)
    *   f.setMaximumFractionDigits(12)
    *   f.setMinimumFractionDigits(0)
    *   f
    * }
    * }}}
    *
    * @param length          输出字符串的总长度，若忽略应传 -1。
    * @param fixedFracDigits 仅接受的小数位数，是否四舍五入取决于 `round` 参数。注意格式化器通常对小数的处理是四舍五入。
    *                        若忽略应传 -1。
    * @param round           是四舍五入还是截断。
    * @param fmtr            数字格式化器。若传 null 表示输出原始值。
    */
  def formatted(length: Int = -1, fixedFracDigits: Int = -1, round: Boolean = false)
               (implicit fmtr: NumberFormat = formatter): String = {
    val s = format(fixedFracDigits, round, fmtr)
    if (length <= 0) s else s formatted s"%${length}s"
  }

  /**
    * @return 原始数字的格式化表示。
    */
  def original = formatted()(null)

  protected def format(fixedFracDigits: Int, round: Boolean, fmtr: NumberFormat): String = {
    if (fmtr == null) valueFfd(fixedFracDigits, round)
    else fmtr.format(valueFfd(fixedFracDigits, round))
  } + " " + unitNameFmt

  final def valueFfd(fixedFracDigits: Int, round: Boolean = false): BigDecimal = NumFmt.cut2FixedFracDigits(value, fixedFracDigits, round)

  /**
    * 注意：如果是`字面量`的数值，应该先把数值[[toString]]再[[BigDecimal.apply(String)]]，不要直接
    * 用`Float`或`Double`去`BigDecimal.apply(Float)`, 因为：
    * The default conversion from Float may not do what you want.
    * 即会造成精度问题。如：值`1.01f`会格式化后输出`1.009999990463`.
    */
  protected def value: BigDecimal

  protected def unitNameFmt: String

  def getFormatter(group: Int, maxFrac: Int, minFrac: Int) = {
    val f: DecimalFormat = new DecimalFormat
    f.setGroupingSize(group)
    f.setMaximumFractionDigits(maxFrac)
    f.setMinimumFractionDigits(minFrac)
    f
  }

  implicit lazy val formatter: NumberFormat = getFormatter(3, 12, 0)
}

object NumFmt {
  final def cut2FixedFracDigits(value: BigDecimal, fixedFracDigits: Int, round: Boolean = false): BigDecimal = {
    if (fixedFracDigits < 0) value
    else {
      val r = math.pow(10, fixedFracDigits)
      (if (round) (value * r).rounded else BigDecimal((value * r).toBigInt)) / r
    }
  }
}
