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

import hobby.wei.c.tools.LruCache

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 22/07/2017
  */
trait Lru extends MemFunc {
  /**
    * @return lru 缓存记录的最大条数。一个 `K -> V` 表示一条记录。
    */
  protected val maxCacheSize: Int

  override protected[cache] lazy val memory = new MemStore[K, V] {
    private val lru = new LruCache[K, Option[V]](maxCacheSize) {
      override protected def sizeOf(key: K, value: Option[V]) = {
        /*
           * 由于通过反射递归遍历父类属性来计算对象实际内存占用是不现实的：
           * 一是性能问题；
           * 二是涉及到共享对象、循环引用、32/64位处理器对象头、引用压缩、4/8bytes对齐等因素的不确定性，
           * 要做到准确性，代价很大，没必要。
           * 因此这里按条数，一条数据的占用就是1。
           */
        1
      }
    }

    // 本实现的调研都是线程安全的，不用调用 Sync 的相关接口。
    override def get(key: K): Option[Option[V]] = Option(lru.get(key))

    override def put(key: K, value: Option[V]): Unit = lru.put(key, value)

    override def remove(key: K): Unit = lru.remove(key)

    override def clear(): Unit = lru.evictAll()
  }
}
