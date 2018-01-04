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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 离散对象分拣器(去重复、排序等)。输入数据需满足接口{@link Discrete}的基本要求。
 *
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 15/08/2016
 */
abstract class AbsSorter<D extends Discrete<D>, R extends AbsSorter.AbsR<D>> {
    private final Comparator<R> comparatorIncrement = new Comparator<R>() {
        @Override
        public int compare(R left, R right) {
            int delta = left.from().delta(right.from());
            if (delta == 0) {
                delta = left.to().delta(right.to());
            }
            // 必须校正到[-1, 1]，遇到过返回 -1 和 Integer.MAX_VALUE 出现bug的问题。
            return Integer.compare(delta, 0);
        }
    };
    private final Comparator<R> comparatorDecrement = new Comparator<R>() {
        @Override
        public int compare(R left, R right) {
            int delta = left.to().delta(right.to());
            if (delta == 0) {
                delta = left.from().delta(right.from());
            }
            // reverse
            return Integer.compare(0, delta);
        }
    };
    private final SortedSet<R> sortedSetInc = new TreeSet<>(comparatorIncrement);
    private final SortedSet<R> sortedSetDec = new TreeSet<>(comparatorDecrement);

    /**
     * 输入单一离散对象。
     *
     * @param discrete
     */
    void put(D discrete) {
        put(discrete, discrete);
    }

    /**
     * 输入一个范围的离散对象。
     *
     * @param from
     * @param to
     */
    void put(D from, D to) {
        put(create(from, to));
    }

    /**
     * 输入一个范围的离散对象。
     *
     * @param range
     */
    synchronized void put(R range) {
        sortedSetInc.add(range);
    }

    /**
     * 取出一个不连续的{@link R}集合并删除，各元素之间有间隙。
     *
     * @param prev     表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit    范围最大是多少个。
     * @param minFirst true 从最小的开始，false 从最大的开始。
     * @return {@link SortedSet}排序集合，排序方式取决于参数<code>minFirst</code>.
     */
    SortedSet<R> takeMore(D prev, int limit, boolean minFirst) {
        return getMore$(prev, limit, minFirst, true);
    }

    /**
     * 取出一个不连续的{@link R}集合但不删除，各元素之间有间隙。
     *
     * @param prev     表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit    范围最大是多少个。
     * @param minFirst true 从最小的开始，false 从最大的开始。
     * @return {@link SortedSet}排序集合，排序方式取决于参数<code>minFirst</code>.
     */
    SortedSet<R> getMore(D prev, int limit, boolean minFirst) {
        return getMore$(prev, limit, minFirst, false);
    }

    private SortedSet<R> getMore$(D prev, int limit, boolean minFirst, boolean drop) {
        final SortedSet<R> result;
        if (minFirst) {
            result = new TreeSet<>(comparatorIncrement);
        } else {
            result = new TreeSet<>(comparatorDecrement);
        }
        R range = prev == null ? null : create(prev, prev);
        int count = 0;
        synchronized (this) { // 必须线程安全，否则rp参数可能无法被正确评估。
            while (count < limit) {
                range = getSerial$(range, limit - count, minFirst);
                if (range == null) break;
                else if (drop) drop(range);
                result.add(range);
                count += range.delta() + 1;
            }
        }
        return result;
    }

    /**
     * 取出一个连续的{@link D}的{@link R}集合并删除, 其{@link R#from() from} ~ {@link R#to() to}
     * 的差值在<code>limit</code>范围内。
     *
     * @param prev     表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit    范围最大是多少个。
     * @param minFirst true 从最小的开始，false 从最大的开始。
     * @return 可能为null.
     */
    R takeSerial(D prev, int limit, boolean minFirst) {
        final R result = getSerial(prev, limit, minFirst);
        if (result != null) drop(result);
        return result;
    }

    /**
     * 取出一个连续的{@link D}的{@link R}集合但不删除, 其{@link R#from() from}～{@link R#to() to}
     * 的差值在<code>limit</code>范围内。
     *
     * @param prev     表示以本{@link D}为起始位置，但返回结果不包括本{@link D}. 可以为null.
     * @param limit    范围最大是多少个。
     * @param minFirst true 从最小的开始，false 从最大的开始。
     * @return 可能为null.
     */
    R getSerial(D prev, int limit, boolean minFirst) {
        return getSerial$(prev == null ? null : create(prev, prev), limit, minFirst);
    }

    private R getSerial$(R prev, int limit, boolean minFirst) {
        SortedSet<R> sortedSet;
        if (minFirst) {
            sortedSet = sortedSetInc;
        } else {
            synchronized (this) {
                sortedSetDec.addAll(sortedSetInc);
            }
            sortedSet = sortedSetDec;
        }
        R begin = null;
        R end = null;
        synchronized (this) {
            for (R range : sortedSet) {
                if (begin == null) {
                    if (prev == null) end = begin = range;
                    else {
                        // prev存在两种情况：
                        // 1. 开放给外部使用的单一Discrete元素，prev.delta() == 0;
                        // 2. 内部实现，本方法在上一次返回的结果，但不一定是range.equals(prev), 因为上一次的返回值可能是截断的结果。
                        // 不过这两种情况都可以用下面的逻辑概括而达到目的。
                        if (minFirst) {
                            if (range.from().delta(prev.to()) > 0) {
                                end = begin = range;
                            } else if (range.to().delta(prev.to()) > 0) {
                                end = begin = create(prev.to().offset(1), range.to());
                            } else continue;
                        } else {
                            if (range.to().delta(prev.from()) < 0) {
                                end = begin = range;
                            } else if (range.from().delta(prev.from()) < 0) {
                                end = begin = create(range.from(), prev.from().offset(-1));
                            } else continue;
                        }
                    }
                }
                if (minFirst) {
                    if (end.to().delta(range.from()) < -1) break;
                    if (end.to().delta(range.to()) < 0 || end == begin) {
                        final int delta = range.to().delta(begin.from()) + 1/*包括from和to本身*/;
                        if (delta < limit) {
                            end = range;
                        } else if (delta == limit) {
                            end = range;
                            break;
                        } else {    // 切断
                            end = create(range.from(), begin.from().offset(limit - 1));
                            break;
                        }
                    }
                } else {
                    if (end.from().delta(range.to()) > 1) break;
                    if (end.from().delta(range.from()) > 0 || end == begin) {
                        final int delta = begin.to().delta(range.from()) + 1/*包括from和to本身*/;
                        if (delta < limit) {
                            end = range;
                        } else if (delta == limit) {
                            end = range;
                            break;
                        } else {    // 切断
                            end = create(begin.to().offset(-(limit - 1)), range.to());
                            break;
                        }
                    }
                }
            }
            sortedSetDec.clear();
        }
        return end == begin ? begin : minFirst ? create(begin.from(), end.to()) : create(end.from(), begin.to());
    }

    boolean contains(D discrete) {
        return contains(discrete, discrete);
    }

    boolean contains(R range) {
        return contains(range.from(), range.to());
    }

    boolean contains(D from, D to) {
        R range = null;
        while (true) {
            range = getSerial$(range, Integer.MAX_VALUE, true);
            if (range == null) break;
            if (range.contains(from, to)) return true;
        }
        return false;
    }

    boolean drop(D discrete) {
        return drop(discrete, discrete);
    }

    boolean drop(R range) {
        return drop(range.from(), range.to());
    }

    /**
     * @return 是否还有 {@link #hasMore() 更多}。
     */
    synchronized boolean drop(D from, D to) {
        if (!hasMore()) return false;
        final List<R> adds = new LinkedList<>();
        final List<R> removes = new LinkedList<>();
        for (R range : sortedSetInc) {
            final int dfm = range.from().delta(from);
            final int dto = range.to().delta(to);
            if (dfm >= 0 && dto <= 0) {
                removes.add(range);
            } else {
                boolean rm = true;
                if (dfm < 0 && range.to().delta(from) >= 0) {
                    adds.add(create(range.from(), from.offset(-1)));
                    removes.add(range);
                    rm = false;
                }
                if (dto > 0 && range.from().delta(to) <= 0) {
                    adds.add(create(to.offset(1), range.to()));
                    if (rm) removes.add(range);
                }
            }
        }
        sortedSetInc.removeAll(removes);
        sortedSetInc.addAll(adds);
        return hasMore();
    }

    public synchronized boolean hasMore() {
        return sortedSetInc.size() != 0;
    }

    abstract R create(D from, D to);

    static abstract class AbsR<D extends Discrete<D>> {
        abstract D from();

        abstract D to();

        abstract String unique();

        public final int delta() {
            return to() == from() ? 0 : to().delta(from());
        }

        public final boolean single() {
            return delta() == 0;
        }

        /**
         * @see #contains(Discrete, Discrete)
         */
        public final <R extends AbsR<D>> boolean contains(R range) {
            return contains(range.from(), range.to());
        }

        /**
         * 是否有包含关系（参数指定范围被包含在本对象指定范围内）。
         */
        public final boolean contains(D from, D to) {
            return contains(from, to, from(), to());
        }

        /**
         * @see #overlap(Discrete, Discrete)
         */
        public final <R extends AbsR<D>> boolean overlap(R range) {
            return overlap(range.from(), range.to());
        }

        /**
         * 是否有交集。
         */
        public final boolean overlap(D from, D to) {
            return contains(from, from(), to()) || contains(to, from(), to())
                    || contains(from(), from, to) || contains(to(), from, to);
        }

        /**
         * @see #joinable(Discrete, Discrete)
         */
        public final <R extends AbsR<D>> boolean joinable(R range) {
            return joinable(range.from(), range.to());
        }

        /**
         * 是否可合并。
         */
        public final boolean joinable(D from, D to) {
            return overlap(from, to) || from.delta(to()) == 1 || from().delta(to) == 1;
        }

        private static <D extends Discrete<D>> boolean contains(D discrete, D from1, D to1) {
            return contains(discrete, discrete, from1, to1);
        }

        private static <D extends Discrete<D>> boolean contains(D from, D to, D from1, D to1) {
            return from.delta(from1) >= 0 && to.delta(to1) <= 0;
        }

        private static <D extends Discrete<D>> boolean equals(AbsR<D> range, AbsR<D> range1) {
            return range.from().delta(range1.from()) == 0 && range.to().delta(range1.to()) == 0;
        }

        @Override
        public final boolean equals(Object o) {
            return o instanceof AbsR && equals(this, (AbsR) o);
        }

        @Override
        public final int hashCode() {
            return from().hashCode() * 41 + to().hashCode();
        }
    }
}
