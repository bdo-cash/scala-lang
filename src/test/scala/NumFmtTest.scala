/*
 * Copyright (C) 2021-present, Chenai Nakam(chenai.nakam@gmail.com)
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

import hobby.chenai.nakam.util.NumFmt
import org.scalatest.FunSuite

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 07/02/2021
  */
class NumFmtTest extends FunSuite {

  test("NumFmt.cut2FixedFracDigits") {
    assertResult(0) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(0), fixedFracDigits = 3)
    }
    assertResult(0) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(0), fixedFracDigits = 3, round = true)
    }
    assertResult(0) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(0), fixedFracDigits = 3, round = true, up = true)
    }

    assertResult(123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 3)
    }
    assertResult(123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 3, round = true)
    }
    assertResult(123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 3, round = true, up = true)
    }
    assertResult(-123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 3)
    }
    assertResult(-123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 3, round = true)
    }
    assertResult(-123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 3, round = true, up = true)
    }

    assertResult(123.12) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 2)
    }
    assertResult(123.12) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 2, round = true)
    }
    assertResult(123.13) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 2, round = true, up = true)
    }
    assertResult(-123.12) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 2)
    }
    assertResult(-123.12) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 2, round = true)
    }
    assertResult(-123.13) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 2, round = true, up = true)
    }

    assertResult(123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 9)
    }
    assertResult(123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 9, round = true)
    }
    assertResult(123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(123.123000), fixedFracDigits = 9, round = true, up = true)
    }
    assertResult(-123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 9)
    }
    assertResult(-123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 9, round = true)
    }
    assertResult(-123.123) {
      NumFmt.cut2FixedFracDigits(value = BigDecimal(-123.123000), fixedFracDigits = 9, round = true, up = true)
    }
  }
}
