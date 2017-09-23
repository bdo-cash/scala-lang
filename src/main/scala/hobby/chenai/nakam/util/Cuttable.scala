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

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 23/09/2017
  */
object Cuttable {
  implicit class CutStr(s: String) {
    def cut(limit: Int, cutLeftTrueRightFalse: Boolean): String = {
      if (s.length <= limit) s
      else if (cutLeftTrueRightFalse) s.substring(s.length - limit)
      else s.substring(0, limit)
    }
  }

  implicit class CutSeq[E](seq: Seq[E]) {
    def cut(limit: Int, cutHeadTrueTailFalse: Boolean): Seq[E] = {
      if (seq.length <= limit) seq
      else if (cutHeadTrueTailFalse) seq.takeRight(limit)
      else seq.take(limit)
    }
  }
}
