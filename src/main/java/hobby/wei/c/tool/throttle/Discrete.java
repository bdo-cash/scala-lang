/*
 * Copyright (C) 2016-present, Wei.Chou(weichou2010@gmail.com)
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

package hobby.wei.c.tool.throttle;

/**
 * 离散数据需满足的基本能力。
 *
 * @param <T> 强制要求T类型与当前对象类型相同。
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 17/08/2016
 */
public interface Discrete<T extends Discrete<T>> {
    /**
     * 求偏移量。
     *
     * @return 返回相差的数量。必须与{@link SorterR#take(int, boolean)}的第一个参数的单位相同。
     */
    int delta(T t);

    /**
     * 生成基于当前对象的偏移量为delta的差值。
     *
     * @param delta 偏移量。
     * @return
     */
    T offset(int delta);

    /**
     * 唯一标识。
     *
     * @return
     */
    String unique();
}