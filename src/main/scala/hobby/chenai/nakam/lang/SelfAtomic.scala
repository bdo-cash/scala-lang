/*
 * Copyright (C) 2022-present, Chenai Nakam(chenai.nakam@gmail.com)
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
  * @version 1.0, 18/07/2022
  */
object SelfAtomic {

  /** e.g.
    * {{{
    *   val set   : mutable.Set[T] = ???
    *   val action: T              = ???
    *   // ...
    *   set.sync { _.add(action) }
    * }}}
    */
  implicit class Sync[A <: AnyRef](o: A) {
    @inline def sync[B](f: A => B): B = o.synchronized(f(o))
  }
}
