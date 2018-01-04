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

import scala.collection.immutable.NumericRange
import scala.util.control.Breaks.{break, breakable}

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 15/12/2017
  */
object SeqOps {

  implicit class NearBy(sizes: Array[Int]) {
    def nearby(n: Int) = {
      var p = 0
      breakable {
        for (s <- sizes.sorted) {
          if (s == n) {
            p = n
            break
          } else if (s > n) {
            p = if (p <= 0) s else if ((n - p).abs >= (s - n).abs) s else p
            break
          }
          p = s
        }
      }
      p
    }
  }

  implicit class WrapNumericRange[A](range: NumericRange[A]) {
    def containsAnyOf(seq: Seq[A]) = {
      var contains = false
      breakable {
        seq.foreach { e =>
          if (range contains e) {
            contains = true
            break
          }
        }
      }
      contains
    }
  }

  implicit class WrapRange(range: Range) {
    def containsAnyOf(seq: Seq[Int]) = {
      var contains = false
      breakable {
        seq.foreach { e =>
          if (range contains e) {
            contains = true
            break
          }
        }
      }
      contains
    }
  }

  implicit class WrapSequence[A](list: Seq[A]) {
    def containsAnyOf(seq: Seq[A]) = {
      var contains = false
      breakable {
        seq.foreach { e =>
          if (list contains e) {
            contains = true
            break
          }
        }
      }
      contains
    }
  }

  implicit class WrapChars(seq: Seq[Char]) {
    /**
      * @param adjacent 是否包括相邻的。
      * @return （包含在等差数列中的字符的个数， 有几个数列，平均差）。
      */
    def ladderCount(adjacent: Boolean = true) = {
      if (seq.length < 2) (0, 0, 0f)
      else {
        var prev = seq.head
        var delta = Int.MaxValue
        var count, repeat = 0
        var amass = 2
        var sum, sumCount = 0
        var b = true
        seq.tail.foreach { c =>
          if (delta != 0 && c - prev == delta) {
            count += amass
            if (b) {
              repeat += 1
              val i = amass min 2
              sumCount += i
              sum += (delta.abs * i)
              b = false
            } else {
              sumCount += amass
              sum += (delta.abs * amass)
            }
            amass = 1
          } else {
            if (adjacent && b && delta.abs == 1) {
              count += amass - 1
              repeat += 1
              sumCount += 1
              sum += 1
              amass = 1
            }
            if (amass < 3) amass += 1
            b = true
            delta = c - prev
          }
          prev = c
        }
        if (adjacent && b && delta.abs == 1) {
          count += amass - 1
          repeat += 1
          sumCount += 1
          sum += 1
          amass = 1
        }
        (count, repeat, if (sumCount == 0) 0 else sum * 1f / sumCount)
      }
    }
  }
}
