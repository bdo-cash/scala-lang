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

import hobby.chenai.nakam.lang.TypeBring.AsIs

import scala.language.implicitConversions

/**
  * 主要用于路径依赖类型的转换。特别针对同时存在参数化类型的情况，仅有
  * 一种方式将某对象转换到目标泛型[T]：
  * {{{在 T 的上下文中调用 o.asInstanceOf[T]。}}}
  * 这里将其抽象为特质。
  * <p>
  * 注意事项：虽然一个 `with TypeBring` 能带来极大的便利，省略很多
  * code，但也会导致忽略很多非法转换。慎用。
  *
  * @tparam O 路径依赖类型的宽泛类型。如：Aa#Xx.
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 16/07/2017
  */
trait TypeBring[L, U >: L, -O] {
  implicit def ^[T >: L <: U](o: O): T = o.as[T]

  def combine[T >: L <: U](o1: O, o2: O)(implicit f: (T, T) => T): T = f(o1, o2)

  /** 对于子类中本来是 `T` 类型的 `this` 但需要强制确认，或者用 `import x._`
    * 方式引入作用范围的，隐式方法 `^(o)` 不会被直接 apply（原因不详）。
    * 只有这样显式定义[隐式函数值]才能起作用。 */
  implicit lazy val t2 = ^ _
}

object TypeBring {
  implicit class AsIs(a: Any) {
    def as[T]: T = a.asInstanceOf[T]

    // 大坑：这个永远返回 true。
    // def is[T]: Boolean = a.isInstanceOf[T]
  }

  implicit class Combine[L, U >: L, -O](o1: O)(implicit tb: TypeBring[L, U, O]) {
    import tb._
    def ++[T >: L <: U](o2: O)(implicit f: (T, T) => T): T = f(o1, o2)
  }
}

//    class TypeTo[T >: UNIT <: COIN] extends (AbsCoinGroup#AbsCoin => T) {
//      def apply(coin: AbsCoinGroup#AbsCoin): T = coin.asInstanceOf[T]
//    }
//    implicit val @: = new TypeTo[COIN]

//    implicit def apply[T >: UNIT <: COIN](coin: AbsCoinGroup#AbsCoin): T = coin.asInstanceOf[T]

//    /** 转换当前对象到目标类型。 */
// 能被用作前缀标识符的只有+、-、!和~。优先级：~（处于最高的行列）、+/-、!。
//    implicit def unary_~[T >: UNIT <: COIN]: T = asInstanceOf[T]

//    /** 转换参数对象到当前对象类型。 */
// 由于方法unary_~在前置调用的时候，无法传类型参数，只能用本方法的方案。
//    implicit def @:[T >: UNIT <: COIN](coin: AbsCoinGroup#AbsCoin): T = coin.asInstanceOf[T]
