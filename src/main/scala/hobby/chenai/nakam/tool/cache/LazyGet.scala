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
  * `get(K)` 等同于单例懒加载。本实现仅针对于首次加载内容非常耗时的操作有意义，即：
  * {{{ 不会让多数线程都去执行这个耗时加载操作。 }}}
  * 即使结果相同。也适用于必须要使用单例的数据模型，不过理论上用 `lazy val` 会更好。
  *
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 08/09/2017
  */
trait LazyGet extends Sync {
  override def get(key: K) = memory.get(key) match {
    case some: Some[Option[V]] => some.get
    case _ => synchronized(super.get(key))
  }
}
