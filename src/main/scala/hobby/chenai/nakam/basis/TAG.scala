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

package hobby.chenai.nakam.basis

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 21/09/2017
  */
object TAG {
  implicit class LogTag(tag: String) extends RuledString {
    override protected val minSize = 6
    override protected val maxSize = 22

    override protected val leadWith = "@|"
    override protected val fillWith = "~"

    override protected val align = A.RIGHT

    override protected def original = tag

    override def toString = trim
  }

  trait ClassName {
    implicit lazy val className: LogTag = getClass.getName
  }

  // TODO:  需要通过宏来实现。
  //  trait MethodName {
  //    implicit lazy val methodName: LogTag = /*需要通过宏来实现*/
  //  }
}
