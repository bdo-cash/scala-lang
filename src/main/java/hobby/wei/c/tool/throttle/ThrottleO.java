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
 * 适用于{@link SorterO}的负载节流控制器。
 * <p>
 * 更多细节，见{@link AbsThrottle}.
 *
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 18/08/2016, 创建;
 *          1.1, 21/02/2017, 增加tag参数;
 *          2.0, 24/02/2017, 重构I/O规则。
 */
public class ThrottleO<O, TAG> extends AbsThrottle<Linkless<O>, Rs<O>, TAG> {
    /**
     * @param counter  参见{@link Counter}, 可以是全局的。
     * @param executor 任务执行者。
     */
    public ThrottleO(Counter counter, Executor<O, TAG> executor) {
        super(counter, 1/*<O>是不连续的,只能是1*/, true/*不连续的，无所谓方向*/, executor);
    }

    /**
     * 新增某任务的输入参数。
     *
     * @param obj      任务的参数或key。
     * @param tag      标签，用于给obj分类。
     * @param promised 该任务是否[承诺完成]（不会被挤出局）。
     */
    public void put(O obj, TAG tag, boolean promised) {
        super.put(new Rs<>(obj), tag, promised);
    }

    /**
     * 丢弃还没有进行的任务的参数。
     *
     * @param obj          任务的参数或key。
     * @param tag          标签，用于给obj分类。
     * @param withPromised 是否将[承诺完成]的任务一并丢弃。
     */
    public void drop(O obj, TAG tag, boolean withPromised) {
        super.drop(new Rs<>(obj), tag, withPromised);
    }

    /**
     * 取消正在进行的任务的参数。若未正在进行，则{@link #drop(O, TAG, boolean) 丢弃}。
     *
     * @param obj          任务的参数或key。
     * @param tag          标签，用于给obj分类。
     * @param withPromised 是否将[承诺完成]的任务一并取消。
     */
    public void cancel(O obj, TAG tag, boolean withPromised) {
        super.cancel(new Rs<>(obj), tag, withPromised);
    }

    @Override
    protected AbsSorter<Linkless<O>, Rs<O>> newSorter() {
        return new SorterO<>();
    }

    public abstract static class Executor<O, TAG> extends AbsExecutor<Linkless<O>, Rs<O>, TAG> {
        @Override
        void execAsync$(Rs<O> range, TAG tag, Runnable onDone) {
            execAsync(range.r.obj, tag, onDone);
        }

        @Override
        void cancel$(Rs<O> range, TAG tag) {
            cancel(range.r.obj, tag);
        }

        /**
         * 异步发起任务调用。
         *
         * @param obj    任务的参数或key。
         * @param tag    标签，同{@link #put(O, TAG, boolean)}的第二个参数。
         * @param onDone 执行完成后的回调，无论成功还是失败。
         */
        protected abstract void execAsync(O obj, TAG tag, Runnable onDone);

        /**
         * 取消任务。
         *
         * @param obj 任务的参数或key。
         * @param tag 标签，同{@link #put(O, TAG, boolean)}的第二个参数。
         */
        protected abstract void cancel(O obj, TAG tag);
    }
}
