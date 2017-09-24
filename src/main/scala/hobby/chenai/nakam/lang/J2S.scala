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

package hobby.chenai.nakam.lang

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 23/09/2017
  */
object J2S {
  /**
    * 主要用于在 Java 代码中调用 Scala 代码时将 `Object...` 转换为 `Any*`（反向转换是自动的）。
    * <p>
    * 只不过这会导致另一个潜在问题：当将转换后的 `Object...` 传过去之后，这个列表会变成
    * `Any*` 的一个元素，因此有了下面的 `Flatten`。
    */
  def array[E](arr: Array[E]): Seq[E] = arr.toSeq

  def empty[E]: Seq[E] = Seq.empty[E]

  // 4 Scala
  implicit class Flatten(seq: Seq[_]) {
    /**
      * 区别于 SDK 的 `seq.flatten` 方法，本方法可以把任意 `高阶列表和对象的混合列表` 平坦化。
      */
    def flatten$: Seq[_] = J2S.flatten$(seq)

    def nonFlat: NonFlat = J2S.nonFlat(seq)
  }

  // 4 Java
  def flatten$(seq: Seq[_]): Seq[_] = flatten$$(seq).reverse

  private def flatten$$(seq: Seq[_]): List[_] = (List.empty[Any] /: seq) {
    (list: List[_], any: Any) =>
      any match {
        case as: Seq[_] => flatten$$(as) ::: list
        case nf: NonFlat => nf.seq :: list // 注意这里是俩冒号，上面是三冒号。
        case _ => any :: list
      }
  }

  // 4 Java
  def nonFlat(seq: Seq[_]): NonFlat = NonFlat(seq)

  /**
    * 那么问题来了（接 `array()`）：如果不想让这个元素被 flat 化🌺，怎么办呢？
    * 用本对象装箱，即调用隐式方法 `nonFlat`。在 `flatten$` 之后会被拆箱。
    */
  case class NonFlat(seq: Seq[_])

  /**
    * 以便在任何对象上面调用 `nonNull` 断言。例如：
    * {{{
    *   require(xxx.nonNull, "xxx不能为空")
    * }}}
    *
    * @param any 一切事物。
    *            注意这里不能定义为 `AnyRef`，否则无法应用到方法或函数上面。
    */
  implicit class NonNull(any: Any) {
    def nonNull: Boolean = any != null
  }

  implicit class IsNull(any: Any) {
    def isNull: Boolean = any == null
  }
}
