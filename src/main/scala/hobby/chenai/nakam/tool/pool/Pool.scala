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

package hobby.chenai.nakam.tool.pool

import java.util.concurrent.locks.ReentrantLock
import hobby.chenai.nakam.lang.J2S.NonNull
import hobby.chenai.nakam.lang.TypeBring.AsIs

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 05/01/2018
  */
trait Pool[T, C <: Cap[T]] {
  private val sPoolSync = new ReentrantLock
  protected val maxPoolSize = 50

  private var sPool: C = _
  private var sPoolSize = 0

  def obtain(o: T): C = {
    val so = obtain()
    so.o = o
    so.pool = this
    so
  }

  private def obtain(): C = {
    sPoolSync.lock()
    try
        if (sPool != null) {
          val sp = sPool
          sPool = sp.next.as[C]
          sp.next = null
          sPoolSize -= 1
          return sp
        }
    finally sPoolSync.unlock()
    newCap
  }

  @throws[InterruptedException]
  def recycle(o: Cap[T]): Unit = {
    sPoolSync.lockInterruptibly()
    try
        if (sPoolSize < maxPoolSize) {
          o.next = sPool
          sPool = o.as[C]
          sPoolSize += 1
        }
    finally sPoolSync.unlock()
  }

  protected def newCap: C
}

trait Cap[T] extends Equals {
  private[pool] var pool: Pool[T, _] = _
  private[pool] var next: Cap[T] = _
  private[pool] var o: T = _

  def get: Option[T] = Option(o)

  override def toString = get.fold(null.asInstanceOf[String])(_.toString)

  def recycle(): Unit = if (pool.nonNull) {
    pool.recycle(this)
    pool == null
  }
}
