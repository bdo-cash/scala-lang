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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link AbsThrottle Throttle}中正在执行任务的计数器。
 *
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 18/08/2016
 */
public class Counter {
    private final Map<AbsThrottle, BI> selfCountMap = new WeakHashMap<>();
    public final int maxReqCount;

    public Counter(int maxReqCount) {
        this.maxReqCount = maxReqCount;
    }

    private static class BI {
        final AtomicBoolean boo = new AtomicBoolean(false);
        final AtomicInteger count = new AtomicInteger(0);

        BI(boolean b, int i) {
            boo.set(b);
            count.set(i);
        }
    }

    /**
     * 注册之后会给留一个坑。
     *
     * @param throttle
     */
    void register(AbsThrottle throttle) {
        final BI value = get(throttle);
        if (value == null) put(throttle, new BI(true, 0));
        else value.boo.set(true);
    }

    void unregister(AbsThrottle throttle) {
        final BI value = get(throttle);
        if (value != null) {
            value.boo.set(false);
        }
    }

    boolean hold(AbsThrottle throttle) {
        final BI value = get(throttle);
        if (value == null) {
            throw new IllegalStateException("请先调用register()方法进行注册。");
        } else if (!value.boo.get()) {
            return false;
        }
        synchronized (this) {
            if (count(false) < maxReqCount || value.count.get() == 0) {
                value.count.incrementAndGet();
                return true;
            } else {
                return false;
            }
        }
    }

    void drop(AbsThrottle throttle) {
        final BI value = get(throttle);
        if (value != null) {
            synchronized (this) {
                if (value.count.get() > 0) {
                    value.count.decrementAndGet();
                }
            }
        }
    }

    public int count() {
        return count(true);
    }

    private synchronized int count(boolean real) {
        int count = 0;
        // 虽然get()操作表面上看貌似不会改变size(), 但会触发WeakHashMap.poll()操作以删除被释放掉的keys,
        // 也就是还是改变了size(), 然后导致了本values()在遍历期间的异常。
        for (BI bi : selfCountMap.values()) {
            count += (bi.count.get() > 0 ? bi.count.get() : bi.boo.get() ? real ? 0 : 1 : 0);
        }
        return count;
    }

    private synchronized BI get(AbsThrottle throttle) {
        return selfCountMap.get(throttle);
    }

    private synchronized void put(AbsThrottle throttle, BI bi) {
        selfCountMap.put(throttle, bi);
    }
}
