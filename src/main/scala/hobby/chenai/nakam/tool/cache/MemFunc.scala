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
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 25/07/2017
  */
protected trait MemFunc {
  protected type K
  protected type V

  def get(key: K): Option[V] = ???

  protected[cache] lazy val memory: MemStore[K, V] = ???

  protected[cache] def <~(key: K): Option[Option[V]] = ???

  protected[cache] def +(value: (K, Option[V])): Unit = ???

  protected[cache] def -(key: K): Unit = ???

  protected[cache] def :=(m: Map[K, Option[V]]): Unit = ???

  protected[cache] def ?(): Unit = ???
}

protected[cache] trait DefImpl[KEY, VALUE] extends MemFunc {
  self =>

  protected type K = KEY
  protected type V = VALUE

  private var map = Map.empty[K, Option[V]]

  override protected[cache] lazy val memory: MemStore[K, V] = new MemStore[K, V] {

    override def get(key: K): Option[Option[V]] = self <~ key

    override def put(key: K, value: Option[V]): Unit = self + (key, value)

    override def remove(key: K): Unit = self - key

    override def clear(): Unit = self := Map.empty[K, Option[V]]
  }

  override protected[cache] def <~(key: K) = map.get(key)

  override protected[cache] def +(value: (K, Option[V])): Unit = map += value

  override protected[cache] def -(key: K): Unit = map -= key

  override protected[cache] def :=(m: Map[K, Option[V]]): Unit = map = m
}

protected trait MemStore[K, V] {
  /**
    * @return 返回 `Option` 的第一层表示有没有存，第二层
    *         是客户代码存入的 `原始数据`，这个 `原始数据` 是
    *         经过数据库操作后返回的，即：这里跟数据库保持一致。
    */
  def get(key: K): Option[Option[V]] = ???

  /**
    * @return 返回 `value` 本身。
    */
  def put(key: K, value: Option[V]): Unit = ???

  def remove(key: K): Unit = ???

  def clear(): Unit = ???
}

object MemStore {
  trait Sync[K, V] extends MemStore[K, V] {
    implicit lazy val lock: ReentrantLock = new ReentrantLock(true) // 公平锁，让put具有顺序性，以防旧值覆盖新值。

    import Locker.sync
    override def get(key: K) = sync(super.get(key))

    override def put(key: K, value: Option[V]): Unit = sync(super.put(key, value))

    override def remove(key: K): Unit = sync(super.remove(key))

    override def clear(): Unit = sync(super.clear())
  }
}
