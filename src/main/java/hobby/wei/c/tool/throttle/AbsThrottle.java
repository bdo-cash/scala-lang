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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import hobby.wei.c.tool.LruCache;

import static scala.Predef.require;

/**
 * 负载节流控制器。
 * <p>
 * 很多业务中，比如按日期翻页的内容展示，常常在连续翻动过程中，只有最后停留的一页需要展示，而被翻过了的页面没必要进行
 * 加载，那么本组件正是为这种场景而设计的。每个页面都可以简单直接发起请求——将请求参数{@link #put(AbsSorter.AbsR, TAG,
 * boolean) 输入}，本组件会把最新的请求优先排队{@link #pushRequest(Tagged) push}, 而把{@link Counter#maxReqCount
 * 超限}的任务参数挤出去并{@link AbsExecutor#cancel$(AbsSorter.AbsR, TAG)
 * cancel}掉。但为了避免挤出某些必须执行的任务参数，所有{@link #put(AbsSorter.AbsR, TAG,
 * boolean)}方法都有<code>promised</code>参数, 表示是否承诺完成。而任何一个输入都可以用{@link TAG}参数来标记分类。
 * <p>
 * 基本I/O规则：总是优先{@link #pushRequest(Tagged) push}最后的{@link #put(AbsSorter.AbsR, TAG, boolean)
 * 输入}(取栈顶)，每个输出将{@link #limit 最大限度}地[{@link AbsSorter.AbsR#joinable(AbsSorter.AbsR)
 * 合并/联结}]而满载。同时每一种{@link TAG 标记}分类都会公平获得{@link #pushRequest(Tagged)
 * push}机会；如果满载队列{@link Counter#maxReqCount 超限}(注意与单个输出的{@link #limit
 * 最大限度}相区别，这里也直接把最大允许同时运行请求数作为满载队列的最大限度)，则会被挤出并{@link
 * AbsExecutor#cancel$(AbsSorter.AbsR, TAG) cancel}.
 *
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 17/08/2016, 创建;
 * 1.1, 21/02/2017, 增加tag参数;
 * 2.0, 24/02/2017, 重构I/O规则。
 */
abstract class AbsThrottle<D extends Discrete<D>, R extends AbsSorter.AbsR<D>, TAG> {
    private final Map<TAG, Tagged<TAG, D, R>> taggedMap = new HashMap<>();
    private final AtomicBoolean pushing = new AtomicBoolean(false);
    private final AtomicBoolean request = new AtomicBoolean(false);
    private final Counter counter;
    private final int limit;
    private final boolean minFirst;
    private final AbsExecutor<D, R, TAG> executor;
    private Tagged<TAG, D, R> tagNull;

    /**
     * @param counter  参见{@link Counter}, 可以是全局的。
     * @param limit    同{@link AbsSorter#takeSerial(D, int, boolean)}的第一个参数。
     * @param minFirst 同{@link AbsSorter#takeSerial(D, int, boolean)}的第二个参数。
     * @param executor 任务执行者。
     */
    AbsThrottle(final Counter counter, int limit, boolean minFirst, final AbsExecutor<D, R, TAG> executor) {
        this.counter = counter;
        this.limit = limit;
        this.minFirst = minFirst;
        this.executor = executor;
        counter.register(this);
    }

    /**
     * 关闭控制器。
     *
     * @param cancel 是否取消掉正在执行的任务，<code>promised</code>任务除外。
     */
    public void destroy(boolean cancel) {
        // counter.hold(this)会失败，不会重新发起请求。
        counter.unregister(this);
        final Tagged fnull = tagNull;
        if (fnull != null) fnull.destroy(cancel);
        final Collection<Tagged<TAG, D, R>> list;
        synchronized (this) {
            list = new LinkedList<>(taggedMap.values());
        }
        for (Tagged f : list) f.destroy(cancel);
    }

    @Override
    protected void finalize() throws Throwable {
        destroy(false);
        super.finalize();
    }

    private synchronized void releaseMe(Tagged<TAG, D, R> f) {
        if (f == tagNull) tagNull = null;
        else taggedMap.remove(f.tag);
    }

    private Tagged<TAG, D, R> getTagged(TAG tag) {
        if (tag == null) {
            if (tagNull == null)
                synchronized (this) {
                    if (tagNull == null) {
                        tagNull = new Tagged<>(null, this);
                    }
                }
            return tagNull;
        }
        synchronized (this) {
            Tagged<TAG, D, R> tagged = taggedMap.get(tag);
            if (tagged == null) {
                tagged = new Tagged<>(tag, this);
                taggedMap.put(tag, tagged);
            }
            return tagged;
        }
    }

    protected abstract AbsSorter<D, R> newSorter();

    /**
     * 新增某任务的输入参数范围。
     *
     * @param range    范围参数，同{@link AbsSorter#put(AbsSorter.AbsR)}的参数。
     * @param tag      标签，用于给range分类。
     * @param promised 该任务是否[承诺完成]（不会被挤出局）。
     */
    void put(R range, TAG tag, boolean promised) {
        getTagged(tag).put(range, promised);
    }

    /**
     * 丢弃还没有进行的任务的参数范围。
     *
     * @param range        范围参数，同{@link AbsSorter#put(AbsSorter.AbsR)}的参数。
     * @param tag          标签，用于给range分类。
     * @param withPromised 是否将[承诺完成]的任务一并丢弃。
     */
    void drop(R range, TAG tag, boolean withPromised) {
        getTagged(tag).drop(range, withPromised);
    }

    /**
     * 取消正在进行的任务的参数范围。若未正在进行，则{@link #drop(AbsSorter.AbsR, TAG, boolean) 丢弃}。
     *
     * @param range        范围参数，同{@link AbsSorter#put(AbsSorter.AbsR)}的参数。
     * @param tag          标签，用于给range分类。
     * @param withPromised 是否将[承诺完成]的任务一并取消。
     */
    void cancel(R range, TAG tag, boolean withPromised) {
        getTagged(tag).cancel(range, withPromised);
    }

    /**
     * @param first 优先
     */
    private void pushRequest(Tagged<TAG, D, R> first) {
        request.set(true);
        if (pushing.compareAndSet(false, true)) {
            request.set(false);
        } else return;
        boolean empty = first.pushRequest$(); // 只是优先权，不执行也罢
        while (true) {
            final Tagged fnull = tagNull;
            if (fnull != null) {
                empty &= fnull.pushRequest$();
            }
            final List<Tagged<TAG, D, R>> list;
            synchronized (this) {
                list = new LinkedList<>(taggedMap.values()); // 必须new, 不然这个values会变。
            }
            for (Tagged f : list) {
                empty &= f.pushRequest$();
                // if (!empty) 当然还要继续
            }
            if (empty) {
                pushing.set(false); // 必须在检查之前置为false, 然后重新竞争。
                if (request.get() && pushing.compareAndSet(false, true)) {
                    request.set(false);
                    // continue;
                } else break;
            } else empty = true;
        }
    }

    private static class Tagged<TAG, D extends Discrete<D>, R extends AbsSorter.AbsR<D>> {
        // new LinkedHashMap<>(0, .75f, true/*是否进行重排序*/)
        private final Map<String, R> executingPromised = new HashMap<>();
        private final LinkedHashMap<String, R> executing = new LinkedHashMap<>(); // 需要保留顺序以备驱逐取消
        private final Stack<R> waitingPromised = new Stack<>();
        private final Stack<R> waiting = new Stack<>();
        private final AbsThrottle<D, R, TAG> throttle;
        private final LruCache<String, R> limiter;
        private final AbsSorter<D, R> sorter;
        private final TAG tag;

        Tagged(TAG tag, AbsThrottle<D, R, TAG> throttle) {
            this.tag = tag;
            this.throttle = throttle;
            this.sorter = throttle.newSorter();
            this.limiter = new LruCache<String, R>(throttle.counter.maxReqCount) {
                @Override
                protected void entryRemoved(boolean evicted, final String key, R oldValue, R newValue) {
                    if (evicted) {  // true表示填满了被驱逐，false表示手动put相同的key或remove.
                        cancel$(executing, oldValue, false);
                    }
                }
            };
        }

        /**
         * 关闭控制器。
         *
         * @param cancel 是否取消掉正在执行的任务，<code>promised</code>任务除外。
         */
        void destroy(boolean cancel) {
            if (cancel) synchronized (this) {
                for (R r : executing.values()) {
                    throttle.executor.cancel$(r, tag);
                }
                // limiter.evictAll(); // 驱逐会触发cancel调用
                // waitingPromised 忽略
            }
            throttle.releaseMe(this);
        }

        private synchronized void releaseWhenIdle() {
            if (waitingPromised.isEmpty() && waiting.isEmpty()
                    && executingPromised.isEmpty() && executing.isEmpty())
                destroy(false);
        }

        /**
         * 新增某任务的输入参数范围。
         *
         * @param range    范围参数，同{@link AbsSorter#put(AbsSorter.AbsR)}的参数。
         * @param promised 该任务是否[承诺完成]（不会被挤出局）。
         */
        void put(R range, boolean promised) {
            if (promised) synchronized (this) {
                waitingPromised.push(range);
            }
            else synchronized (this) {
                waiting.push(range);
            }
            pushRequest();
        }

        void drop(R range, boolean withPromised) {
            if (withPromised) drop$(waitingPromised, range);
            drop$(waiting, range);
            pushRequest();
        }

        void cancel(R range, boolean withPromised) {
            if (withPromised) cancel$(executingPromised, range, true);
            cancel$(executing, range, true);
            drop(range, withPromised);
        }

        private void pushRequest() {
            throttle.pushRequest(this);
        }

        private synchronized void cancel$(Map<String, R> map, R range, boolean force) {
            for (R r : map.values()) {
                if (force ? range.contains(r) : range.equals(r)) {
                    throttle.executor.cancel$(r, tag);
                }
            }
        }

        private synchronized void drop$(List<R> list, R range) {
            for (int i = list.size() - 1; i >= 0; i--) {
                final R r = list.get(i);
                if (r.overlap(range)) { // 有交集
                    list.remove(i);   // 先删掉，再插入
                    if (range.contains(r)) { // r在drop的范围内
                        // nothing...
                    } else { // 需要drop操作
                        require(!sorter.hasMore());
                        sorter.put(r);
                        sorter.drop(range);
                        R rd;
                        while ((rd = sorter.takeSerial(null, Integer.MAX_VALUE, throttle.minFirst)) != null) {
                            list.add(i, rd); // 注意index不变，则先take出来的，会往stack顶部方向挤压。
                        }
                    }
                }
            }
        }

        /**
         * 从等待栈中取出不超过最大限度(throttle.limit)的范围参数。
         */
        private synchronized R take$(Stack<R> stack) {
            if (stack.empty()) return null;
            R range = stack.pop();
            if (range.delta() + 1 > throttle.limit) {
                require(!sorter.hasMore());
                sorter.put(range);
                range = sorter.takeSerial(null, throttle.limit, throttle.minFirst);
                final R rd = sorter.takeSerial(null, Integer.MAX_VALUE, throttle.minFirst);
                if (rd != null) stack.push(rd);
            }
            return range;
        }

        /**
         * 将range尽可能滚到最大限度(throttle.limit), 即参数满载。
         */
        private synchronized R merge$(List<R> list, R range) {
            // 例如：list[[9, 20], [6], [11], [3], [7], [8], [12]], range[10],
            // 如果仅一遍：则结果是list[[6], [11], [3], [7], [8], [12]], return[9, 20]
            // 显然应该多遍，但到底多少遍？那就每join一次就再来一遍。
            while (true) {
                boolean loop = false;
                for (int i = list.size() - 1; i >= 0; i--) {
                    final int len = range.delta() + 1;
                    require(len <= throttle.limit);
                    if (len == throttle.limit) {
                        // drop$(list, i, range); // 全部drop, 而不是从i开始drop, 原因见上面示例。
                        // return range;
                        break;
                    }
                    final R r = list.get(i);
                    if (range.contains(r)) {
                        list.remove(i); // 合并的结果就是删掉
                    } else if (r.joinable(range)) { // 是否可合并
                        require(!sorter.hasMore());
                        sorter.put(r);
                        sorter.put(range);
                        final R favor = sorter.takeSerial(null, throttle.limit, throttle.minFirst);
                        final R rd = sorter.takeSerial(null, Integer.MAX_VALUE, throttle.minFirst);
                        require(!sorter.hasMore()); // 两次应该拿完
                        if (rd == null) {
                            range = favor; // 滚雪球把range滚大
                            list.remove(i); // 已经被吸收，那么就删掉
                        } else {
                            if (range.contains(rd)) {
                                // range内的任何元素都是优先的，不可以留下来。但可以换一头再试试。
                                sorter.put(r);
                                sorter.put(range);
                                final R favor1 = sorter.takeSerial(null, throttle.limit, !/*换一头*/throttle.minFirst);
                                final R rd1 = sorter.takeSerial(null, Integer.MAX_VALUE, throttle.minFirst);
                                require(!range.contains(rd1));
                                range = favor1;
                                list.set(i, rd1);
                            } else {
                                range = favor; // 滚雪球把range滚大
                                list.set(i, rd); // 替换掉
                            }
                        }
                        loop = true;
                        // break; // 基于上面的测试数据，如果break则循环次数更多。
                    }
                }
                if (!loop) break;
            }
            return range;
        }

        /**
         * @return true 不可以继续push, false 反之。
         */
        private boolean pushRequest$() {
            boolean empty = true;
            if (throttle.counter.hold(throttle)) {
                if (waitingPromised.empty()) {
                    throttle.counter.drop(throttle);
                    empty &= true;
                } else {
                    final List<R> list;
                    synchronized (this) {
                        list = new LinkedList<>(executingPromised.values());
                    }
                    for (R r : list) {
                        drop$(waitingPromised, r);
                    }
                    R range = take$(waitingPromised);
                    if (range == null) {
                        throttle.counter.drop(throttle);
                        empty &= true;
                    } else {
                        range = merge$(waitingPromised, range);
                        synchronized (this) {
                            executingPromised.put(range.unique(), range);
                        }
                        postExec(range, true);
                        empty &= waitingPromised.empty();
                    }
                }
            } else return true;
            // 与上面promised的逻辑有些不同
            if (throttle.counter.hold(throttle)) {
                if (waiting.empty()) {
                    throttle.counter.drop(throttle);
                    empty &= true;
                } else {
                    List<R> list;
                    synchronized (this) {
                        // 可以用promised吞掉非promised的，但不能反过来，因为非promised可能因为被挤出去而cancel.
                        list = new LinkedList<>(executingPromised.values());
                    }
                    for (R r : list) {
                        drop$(waiting, r);
                    }
                    synchronized (this) {
                        list = new LinkedList<>(executing.values());
                    }
                    for (R r : list) {
                        drop$(waiting, r);
                    }
                    if (waiting.empty()) {
                        throttle.counter.drop(throttle);
                        empty &= true;
                    } else {
                        final Stack<R> stack = new Stack<>();
                        R range;
                        while ((range = take$(waiting)) != null) {
                            stack.push(merge$(waiting, range));
                        }
                        if (stack.empty()) {
                            throttle.counter.drop(throttle);
                            empty &= true;
                        } else {
                            // 由于throttle.pushRequest(this)方法确保了一次仅有一个线程进入本方法，
                            // 因此这里尽可能缩小了同步块范围。
                            final Set<Map.Entry<String, R>> set;
                            synchronized (this) {
                                set = new LinkedHashSet<>(executing.entrySet());
                            }
                            for (Map.Entry<String, R> entry : set) {
                                limiter.remove(entry.getKey());
                            }
                            while (stack.size() > 1) {
                                final R r = stack.pop();
                                limiter.put(r.unique(), r); // 先放进去，以便后面能挤出来。
                            }
                            range = stack.pop();
                            final String unique = range.unique();
                            synchronized (this) {
                                executing.put(unique, range);
                            }
                            for (Map.Entry<String, R> entry : set) {
                                limiter.put(entry.getKey(), entry.getValue()); // 挤出去
                            }
                            limiter.put(unique, range);
                            for (Map.Entry<String, R> entry : set) {
                                limiter.remove(entry.getKey()); // 删除，剩下的就是没有挤完的。
                            }
                            limiter.remove(unique);
                            for (Map.Entry<String, R> entry : limiter.snapshot().entrySet()) {
                                synchronized (this) {
                                    waiting.push(entry.getValue()); // 没有挤完的，进入waiting.
                                }
                                limiter.remove(entry.getKey());
                            }
                            postExec(range, false);
                            empty &= waiting.empty();
                        }
                    }
                }
            } else return true;
            return empty;
        }

        private void postExec(R range, boolean promised) {
            final Runnable ending;
            if (promised) {
                ending = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (Tagged.this) {
                            executingPromised.remove(range.unique());
                            cancel$(executing, range, true);
                            drop$(waitingPromised, range);
                            drop$(waiting, range);
                        }
                    }
                };
            } else {
                ending = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (Tagged.this) {
                            executing.remove(range.unique());
                            cancel$(executingPromised, range, true);
                            drop$(waitingPromised, range);
                            drop$(waiting, range);
                        }
                    }
                };
            }
            final Runnable onDone = new Runnable() {    // 保证只执行一次
                final AtomicBoolean done = new AtomicBoolean(false);

                @Override
                public void run() {
                    if (done.getAndSet(true)) return;
                    ending.run();
                    throttle.counter.drop(throttle);
                    pushRequest();
                    releaseWhenIdle();
                }
            };
            try {
                throttle.executor.execAsync$(range, tag, onDone);
            } catch (Exception e) {
                onDone.run();   // 注意不在finally条件下执行
                throw e;
            }
        }

        @Override
        public String toString() {
            return "Tagged{" + tag + '}';
        }
    }

    abstract static class AbsExecutor<D extends Discrete<D>, R extends AbsSorter.AbsR<D>, TAG> {
        /**
         * 异步发起任务调用。
         *
         * @param range  范围参数，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第一个参数。
         * @param tag    标签，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第二个参数。
         * @param onDone 执行完成后的回调，无论成功还是失败。
         */
        abstract void execAsync$(R range, TAG tag, Runnable onDone);

        /**
         * 取消任务。
         *
         * @param range 范围参数，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第一个参数。
         * @param tag   标签，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第二个参数。
         */
        abstract void cancel$(R range, TAG tag);
    }
}
