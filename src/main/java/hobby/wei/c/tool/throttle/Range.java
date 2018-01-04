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

import static scala.Predef.require;

/**
 * @author Wei.Chou(weichou2010@gmail.com)
 * @version 1.0, 17/08/2016
 */
public final class Range<D extends Discrete<D>> extends AbsSorter.AbsR<D> {
    public final D from, to;

    public Range(D from, D to) {
        require(from.delta(to) <= 0);
        this.from = from;
        this.to = to;
    }

    @Override
    final D from() {
        return from;
    }

    @Override
    final D to() {
        return to;
    }

    @Override
    public final String unique() {
        return from.unique() + (single() ? "" : "~" + to.unique());
    }

    @Override
    public final String toString() {
        return "[" + from + ", " + to + "]";
    }
}
