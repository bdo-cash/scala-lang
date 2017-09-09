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

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 22/07/2017
  */
trait Memoize[KEY, VALUE] extends DefImpl[KEY, VALUE] {

  protected val delegate: Delegate[K, V]

  override def get(key: K) = memory.get(key) match {
    case some: Some[Option[V]] => some.get
    case _ => refresh(key)
  }

  def getOnly(key: K): Option[V] = memory.get(key) match {
    case some: Some[Option[V]] => some.get
    case _ => None
  }

  def refresh(key: K): Option[V] = memoize(key, delegate.load(key))

  def dirty(key: K): Unit = memory.remove(key)

  def update(key: K, value: V): Option[V] = memoize(key, delegate.update(key, value))


  private def memoize(key: K, value: Option[V]) = {
    memory.put(key, value)
    value
  }

  def clear(): Unit = memory.clear()
}

trait Delegate[K, V] {
  /**
    * 从数据库加载内容。
    *
    * @param key 要加载的数据的键。
    * @return `Some(V)` 表示有数据，`None` 表示没有数据。
    */
  def load(key: K): Option[V]

  /**
    * 将数据存入到数据库。
    *
    * @param key   要存入的数据的键。
    * @param value 要存入的数据内容。
    * @return `Some(V)` 表示有数据，`None` 表示没有数据。
    */
  def update(key: K, value: V): Option[V]
}
