/*
 * Copyright (C) 2016-present, Wei Chou(weichou2010@gmail.com)
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

import java.util.SortedSet;

/**
 * 离散对象或{@link Range}分拣器(去重复、排序等)。输入数据需满足接口{@link Discrete}的基本要求。
 *
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 15/08/2016
 */
public class SorterR<D extends Discrete<D>> extends AbsSorter<D, Range<D>> {
    @Override
    public void put(D discrete) {
        super.put(discrete);
    }

    @Override
    public void put(D from, D to) {
        super.put(from, to);
    }

    @Override
    public void put(Range<D> range) {
        super.put(range);
    }

    /**
     * @see #take(D, int, boolean)
     */
    public SortedSet<Range<D>> take(int limit, boolean fromMin) {
        return take(null, limit, fromMin);
    }

    /**
     * 取出一个不连续的{@link Range}集合并删除，各元素之间有间隙。
     *
     * @param prev    表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit   范围最大是多少个。
     * @param fromMin true 从最小的开始，false 从最大的开始。
     * @return {@link SortedSet}排序集合，排序方式取决于参数<code>fromMin</code>.
     */
    public SortedSet<Range<D>> take(D prev, int limit, boolean fromMin) {
        return super.takeMore(prev, limit, fromMin);
    }

    /**
     * @see #get(D, int, boolean)
     */
    public SortedSet<Range<D>> get(int limit, boolean fromMin) {
        return get(null, limit, fromMin);
    }

    /**
     * 取出一个不连续的{@link Range}集合但不删除，各元素之间有间隙。
     *
     * @param prev    表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit   范围最大是多少个。
     * @param fromMin true 从最小的开始，false 从最大的开始。
     * @return {@link SortedSet}排序集合，排序方式取决于参数<code>fromMin</code>.
     */
    public SortedSet<Range<D>> get(D prev, int limit, boolean fromMin) {
        return super.getMore(prev, limit, fromMin);
    }

    /**
     * @see #takeSerial(D, int, boolean)
     */
    public Range<D> takeSerial(int limit, boolean minFirst) {
        return takeSerial(null, limit, minFirst);
    }

    /**
     * 取出一个连续的{@link D}的{@link Range}并删除, 其{@link Range#from from}～{@link Range#to to}
     * 的差值在<code>limit</code>范围内。
     *
     * @param prev     表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit    范围最大是多少个。
     * @param minFirst true 从最小的开始，false 从最大的开始。
     * @return 可能为null.
     */
    @Override
    public Range<D> takeSerial(D prev, int limit, boolean minFirst) {
        return super.takeSerial(prev, limit, minFirst);
    }

    /**
     * @see #getSerial(D, int, boolean)
     */
    public Range<D> getSerial(int limit, boolean fromMin) {
        return getSerial(null, limit, fromMin);
    }

    /**
     * 取出一个连续的{@link D}的{@link Range}但不删除, 其{@link Range#from from}～{@link Range#to to}
     * 的差值在<code>limit</code>范围内。
     *
     * @param prev    表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit   范围最大是多少个。
     * @param fromMin true 从最小的开始，false 从最大的开始。
     * @return 可能为null.
     */
    @Override
    public Range<D> getSerial(D prev, int limit, boolean fromMin) {
        return super.getSerial(prev, limit, fromMin);
    }

    @Override
    public boolean contains(D discrete) {
        return super.contains(discrete, discrete);
    }

    @Override
    public boolean contains(D from, D to) {
        return super.contains(from, to);
    }

    @Override
    public boolean contains(Range<D> range) {
        return super.contains(range.from, range.to);
    }

    @Override
    public boolean drop(D discrete) {
        return super.drop(discrete);
    }

    @Override
    public boolean drop(D from, D to) {
        return super.drop(from, to);
    }

    @Override
    public boolean drop(Range<D> range) {
        return super.drop(range);
    }

    @Override
    Range<D> create(D from, D to) {
        return new Range<>(from, to);
    }
}
