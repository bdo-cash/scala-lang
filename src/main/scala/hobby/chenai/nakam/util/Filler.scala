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

import hobby.chenai.nakam.lang.J2S._
import hobby.chenai.nakam.util.Cuttable._

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 22/09/2017
  */
object Filler {
  def fillStr2Len(count: Int, elem: String, limit: Int, cutLeftTrueRightFalse: Boolean): String = {
    require(limit >= count)
    require(elem.nonNull)
    require(elem.nonEmpty)
    val s = fill(count / elem.length + (if (count % elem.length == 0) 0 else 1), elem).mkString
    assert(s.length >= count)
    s.cut(limit, cutLeftTrueRightFalse)
  }

  def fill[E](count: Int, elem: E, list: List[E] = Nil): List[E] = {
    if (count <= 0) list
    else elem :: fill(count - 1, elem, list)
  }
}
