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
  /**
    * 用于日志输出时的参数，e.g:
    * {{{
    *   LOG.e("some msg", exception, args list)(logTag)
    * }}}
    * 通常，logTag 可通过隐式转换来省略，隐式转换的通常通过在类后面接入
    * `with ClassName/MethodName` 的方式引入。
    * <p>
    * 注意：针对于 Android Log API, 如果 tag 包含了`非单字节`字符， 仍然会触发超过23字符的异常。
    */
  implicit class LogTag(tag: String) extends RuledString {
    override protected lazy val minSize = leadWith.length + 5
    override protected lazy val maxSize = 22

    override protected val leadWith = "@|"
    override protected val fillWith = "~"

    override protected val align = A.RIGHT

    override protected val original = tag

    override def toString = trim
  }

  /** 用于在异常信息中增加前缀，以便于在日志中搜索。e.g:
    * {{{
    *   new IllegalArgumentException("some msg".tag)
    * }}}
    */
  implicit class ThrowMsg(_tag: String) extends LogTag(_tag: String) {
    override protected lazy val maxSize = 1000

    def tag = toString
  }

  /**
    * 用于将消息变短。
    */
  implicit class ShortMsg(_tag: String) extends LogTag(_tag: String) {
    def tag = toString
  }

  /** 接入到类后面以便引入 `LogTag`。 */
  trait ClassName {
    implicit lazy val className: LogTag = LogTag(getClass.getName + "@" + hashCode.toHexString.take(3))
  }

  // TODO:  需要通过宏来实现。
  //  trait MethodName {
  //    implicit lazy val methodName: LogTag = /*需要通过宏来实现*/
  //  }
}
