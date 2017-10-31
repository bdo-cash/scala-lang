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

import java.util

import scala.util.control.Breaks.{break, breakable}

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
  @inline def toSeq[E](arr: Array[E]): Seq[E] = arr

  @inline def nil[E]: Seq[E] = Nil

  // 4 Scala
  implicit class Flatten(seq: Seq[_]) {
    /**
      * 区别于 SDK 的 `seq.flatten` 方法，本方法可以把任意 `高阶列表和对象的混合列表` 平坦化。
      */
    @inline def flatten$: Seq[_] = J2S.flatten$$(seq).reverse
  }

  // 4 Java
  @inline def flatten$(seq: Seq[_]): Seq[_] = seq.flatten$

  private def flatten$$(seq: Seq[_]): List[_] = (List.empty[Any] /: seq) {
    (list: List[_], any: Any) =>
      any match {
        case as: Seq[_] => flatten$$(as) ::: list
        case ar: Array[_] => flatten$$(ar) ::: list
        case nf: NonFlat => nf.seq :: list // 注意这里是俩冒号，上面是三冒号。
        case nf: NonFlat$ => nf.arr :: list
        case _ => any :: list
      }
  }

  /**
    * 那么问题来了（接 `toSeq()`）：如果不想让这个元素被 flat 化🌺，怎么办呢？
    * 用本对象装箱，即调用隐式方法 `nonFlat`。在 `flatten$` 之后会被拆箱。
    */
  implicit class NonFlat(val seq: Seq[_]) {
    def nonFlat: NonFlat = this
  }

  implicit class NonFlat$(val arr: Array[_]) {
    def nonFlat: NonFlat$ = this
  }

  // 4 Java
  @inline def nonFlat(seq: Seq[_]): NonFlat = seq.nonFlat

  @inline def nonFlat(arr: Array[_]): NonFlat$ = arr.nonFlat

  /**
    * 以便在任何对象上面调用 `nonNull` 断言。例如：
    * {{{
    *   require(xxx.nonNull, "xxx不能为空")
    * }}}
    */
  implicit class NonNull(ref: AnyRef) {
    @inline def nonNull: Boolean = ref ne null

    @inline def isNull: Boolean = ref eq null
  }

  implicit class WrapEnumeration[A](e: util.Enumeration[A]) {
    def toSeq: Seq[A] = {
      var list: List[A] = Nil
      while (e.hasMoreElements) list ::= e.nextElement()
      list.reverse
    }
  }

  implicit class WrapIterator[A](e: util.Iterator[A]) {
    def toSeq: Seq[A] = {
      var list: List[A] = Nil
      while (e.hasNext) list ::= e.next()
      list.reverse
    }
  }

  implicit class Ifable[A](any: A) {
    /**
      * 对某值 `any` 的系列条件判断可以集中用这个函数表示，避免写一堆 `if...else...`。
      *
      * @param defValue 如果 `any` 不满足任何条件，则观察本默认值；如果默认值有定义，则返回该值，否则抛异常。
      * @param f        条件和结果集合。
      * @tparam R 返回值类型。
      */
    def ifAble[R](defValue: Option[R], f: (A => (Boolean, R))*): R = {
      if (f.isEmpty) if (defValue.isDefined) defValue.get else throw new IllegalArgumentException("没有符合条件的结果。")
      else {
        val t = f.head(any)
        if (t._1) t._2
        else ifAble(defValue, f.tail: _*)
      }
    }
  }

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
}
