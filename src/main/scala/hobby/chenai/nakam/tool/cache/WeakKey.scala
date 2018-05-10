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

import scala.collection.mutable

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 10/05/2018
  */
trait WeakKey extends MemFunc {
  override protected[cache] lazy val memory = new MemStore[K, V] {
    private val weak = new mutable.WeakHashMap[K, Option[V]]

    override def get(key: K): Option[Option[V]] = weak.get(key)

    override def put(key: K, value: Option[V]): Unit = weak.put(key, value)

    override def remove(key: K): Unit = weak.remove(key)

    override def clear(): Unit = weak.clear()
  }
}

object WeakKey {
  trait Sync extends MemFunc {
    override protected[cache] lazy val memory = new MemStore[K, V] with MemStore.Sync[K, V] {
      private val weak = new mutable.WeakHashMap[K, Option[V]]

      override def get(key: K): Option[Option[V]] = weak.get(key)

      override def put(key: K, value: Option[V]): Unit = weak.put(key, value)

      override def remove(key: K): Unit = weak.remove(key)

      override def clear(): Unit = weak.clear()
    }
  }
}
