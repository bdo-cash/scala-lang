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
 * 适用于{@link SorterR}的负载节流控制器。
 * <p>
 * 更多细节，见{@link AbsThrottle}.
 *
 * @author Wei.Chou(weichou2010@gmail.com)
 * @version 1.0, 18/08/2016, 创建;
 *          1.1, 21/02/2017, 增加tag参数;
 *          2.0, 24/02/2017, 重构I/O规则。
 */
public class ThrottleR<D extends Discrete<D>, TAG> extends AbsThrottle<D, Range<D>, TAG> {
    /**
     * @param counter  参见{@link Counter}, 可以是全局的。
     * @param limit    同{@link SorterR#takeSerial(int, boolean)}的第一个参数。
     * @param minFirst 同{@link SorterR#takeSerial(int, boolean)}的第二个参数。
     * @param executor 任务执行者。
     */
    public ThrottleR(Counter counter, int limit, boolean minFirst, Executor<D, TAG> executor) {
        super(counter, limit, minFirst, executor);
    }

    public void put(D discrete, TAG tag, boolean promised) {
        put(discrete, discrete, tag, promised);
    }

    public void put(D from, D to, TAG tag, boolean promised) {
        put(new Range<>(from, to), tag, promised);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(Range<D> range, TAG tag, boolean promised) {
        super.put(range, tag, promised);
    }

    public void drop(D discrete, TAG tag, boolean promised) {
        drop(discrete, discrete, tag, promised);
    }

    public void drop(D from, D to, TAG tag, boolean promised) {
        drop(new Range<>(from, to), tag, promised);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drop(Range<D> range, TAG tag, boolean promised) {
        super.drop(range, tag, promised);
    }

    public void cancel(D discrete, TAG tag, boolean promised) {
        cancel(discrete, discrete, tag, promised);
    }

    public void cancel(D from, D to, TAG tag, boolean promised) {
        cancel(new Range<>(from, to), tag, promised);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel(Range<D> range, TAG tag, boolean promised) {
        super.cancel(range, tag, promised);
    }

    @Override
    protected AbsSorter<D, Range<D>> newSorter() {
        return new SorterR<D>();
    }

    public abstract static class Executor<D extends Discrete<D>, TAG> extends AbsExecutor<D, Range<D>, TAG> {
        @Override
        void execAsync$(Range<D> range, TAG tag, Runnable onDone) {
            execAsync(range, tag, onDone);
        }

        @Override
        void cancel$(Range<D> range, TAG tag) {
            cancel(range, tag);
        }

        /**
         * 异步发起任务调用。
         *
         * @param range  范围参数，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第一个参数。
         * @param tag    标签，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第二个参数。
         * @param onDone 执行完成后的回调，无论成功还是失败。
         */
        protected abstract void execAsync(Range<D> range, TAG tag, Runnable onDone);

        /**
         * 取消任务。
         *
         * @param range 范围参数，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第一个参数。
         * @param tag   标签，同{@link #put(AbsSorter.AbsR, TAG, boolean)}的第二个参数。
         */
        protected abstract void cancel(Range<D> range, TAG tag);
    }
}
