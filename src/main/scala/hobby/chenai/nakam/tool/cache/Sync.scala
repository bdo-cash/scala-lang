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

package hobby.chenai.nakam.tool.cache

import java.util.concurrent.locks.ReentrantLock
import hobby.wei.c.tool.Locker

/**
  * 本实现将同步阻塞范围降低到最小。
  * <p>
  * 对于多线程乱序的加载及更新操作，任何作用范围的同步语句都无法保证最终存储内容的确定性，即：
  * {{{ 任何扩大同步语句作用范围的做法都是毫无意义的。 }}}
  *
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 08/09/2017
  */
trait Sync extends MemFunc {
  implicit lazy val lock: ReentrantLock = new ReentrantLock(true) // 公平锁，让put具有顺序性，以防旧值覆盖新值。

  import Locker.sync
  override protected[cache] def <~(key: K) = sync(super.<~(key))

  override protected[cache] def +(value: (K, Option[V])): Unit = sync(super.+(value))

  override protected[cache] def -(key: K): Unit = sync(super.-(key))

  override protected[cache] def :=(m: Map[K, Option[V]]): Unit = sync(super.:=(m))

  override protected[cache] def ?(): Unit = sync(super.?())
}
