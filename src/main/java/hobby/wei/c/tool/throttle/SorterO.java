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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static scala.Predef.require;

/**
 * 离散对象分拣器(去重复、排序等)。输入数据可以是任意对象。
 *
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 15/08/2016
 */
public class SorterO<O> extends AbsSorter<Linkless<O>, Rs<O>> {
    /**
     * 输入一个任意对象。
     *
     * @param obj
     */
    public void put(O obj) {
        super.put(new Rs<>(obj));
    }

    /**
     * @see #take(Object, int, boolean)
     */
    public List<O> take(int limit, boolean stackMode) {
        return take(null, limit, stackMode);
    }

    /**
     * 取出一个{@link O}集合并删除，各元素之间有间隙。
     *
     * @param prev      表示以本{@link O}为起始位置，但返回结果不包括本{@link O}. 可以为null.
     * @param limit     范围最大是多少个。
     * @param stackMode true 从最后插入的开始，false 从先插入的开始。
     * @return {@link List}排序集合，排序方式取决于参数<code>stackMode</code>.
     */
    public List<O> take(O prev, int limit, boolean stackMode) {
        final SortedSet<Rs<O>> set = super.takeMore(prev == null ? null : new Linkless<>(prev), limit, !stackMode/*最近的就是最大的*/);
        final List<O> list = new ArrayList<>(set.size());
        for (Rs<O> rs : set) {
            list.add(rs.r.obj);
        }
        return list;
    }

    /**
     * @see #get(Object, int, boolean)
     */
    public List<O> get(int limit, boolean stackMode) {
        return get(null, limit, stackMode);
    }

    /**
     * 取出一个{@link O}集合但不删除，各元素之间有间隙。
     *
     * @param prev      表示以本{@link O}为起始位置，但返回结果不包括本{@link O}. 可以为null.
     * @param limit     范围最大是多少个。
     * @param stackMode true 从最后插入的开始，false 从先插入的开始。
     * @return {@link List}排序集合，排序方式取决于参数<code>stackMode</code>.
     */
    public List<O> get(O prev, int limit, boolean stackMode) {
        final SortedSet<Rs<O>> set = super.getMore(prev == null ? null : new Linkless<>(prev), limit, !stackMode/*最近的就是最大的*/);
        final List<O> list = new ArrayList<>(set.size());
        for (Rs<O> rs : set) {
            list.add(rs.r.obj);
        }
        return list;
    }

    /**
     * @see #take(Object, boolean)
     */
    public O take(boolean stackMode) {
        return take(null, stackMode);
    }

    /**
     * 取出一个{@link O}并删除，可能为null。
     *
     * @param prev      表示以本{@link O}为起始位置，但返回结果不包括本{@link O}. 可以为null.
     * @param stackMode true 从最后插入的开始，false 从先插入的开始。
     * @return 可能为null.
     */
    public O take(O prev, boolean stackMode) {
        final Rs<O> rs = super.takeSerial(prev == null ? null : new Linkless<>(prev), 1, !stackMode);
        return rs == null ? null : rs.r.obj;
    }

    /**
     * @see #get(Object, boolean)
     */
    public O get(boolean stackMode) {
        return get(null, stackMode);
    }

    /**
     * 取出一个{@link O}并删除，可能为null。
     *
     * @param prev      表示以本{@link O}为起始位置，但返回结果不包括本{@link O}. 可以为null.
     * @param stackMode true 从最后插入的开始，false 从先插入的开始。
     * @return 可能为null.
     */
    public O get(O prev, boolean stackMode) {
        final Rs<O> rs = super.getSerial(prev == null ? null : new Linkless<>(prev), 1, !stackMode);
        return rs == null ? null : rs.r.obj;
    }

    public boolean contains(O obj) {
        return super.contains(new Linkless<>(obj));
    }

    /**
     * 删除一个对象。
     *
     * @param obj
     */
    public boolean drop(O obj) {
        return super.drop(new Rs<>(obj));
    }

    @Override
    Rs<O> create(Linkless<O> from, Linkless<O> to) {
        require(from.equals(to));
        return new Rs<>(from);
    }
}

class Rs<O> extends AbsSorter.AbsR<Linkless<O>> {
    final Linkless<O> r;

    Rs(O obj) {
        this(new Linkless<>(obj));
    }

    Rs(Linkless<O> d) {
        r = d;
    }

    @Override
    final Linkless<O> from() {
        return r;
    }

    @Override
    final Linkless<O> to() {
        return r;
    }

    @Override
    final String unique() {
        return r.unique();
    }

    @Override
    public final String toString() {
        return "[" + r + "]";
    }
}

/**
 * 无法连续的离散点。
 */
final class Linkless<O> implements Discrete<Linkless<O>> {
    private static final AtomicInteger sNum = new AtomicInteger(0);
    private static final WeakHashMap<Object, Integer> sMap = new WeakHashMap<>();
    private final int n;
    final O obj;

    Linkless(O obj) {
        this.obj = requireNonNull(obj);
        Integer i;
        // 为了保证同一个对象排序不重复不紊乱
        synchronized (sMap) {
            i = sMap.get(obj);
            if (i == null) {
                i = sNum.addAndGet(2);
                sMap.put(obj, i);
            }
        }
        n = i;
    }

    @Override
    public final int delta(Linkless d) {
        return n - d.n;
    }

    @Override
    public final Linkless<O> offset(int delta) {
        return null;
    }

    @Override
    public final String unique() {
        return String.valueOf(n);
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof Linkless
                && ((Linkless) o).n == n;
    }

    @Override
    public final int hashCode() {
        return n;
    }

    @Override
    public final String toString() {
        return "(" + n + ")" + obj;
    }
}
