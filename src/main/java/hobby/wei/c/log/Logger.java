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

package hobby.wei.c.log;

import hobby.chenai.nakam.basis.TAG;

/**
 * @author Chenai Nakam(chenai.nakam@gmail.com)
 * @version 1.0, 05/01/2018
 */
public abstract class Logger {
    public void v(TAG.LogTag tag, String s, Object... args) {
        v(tag, null, s, args);
    }

    public abstract void v(TAG.LogTag tag, Throwable e, String s, Object... args);

    public void v(TAG.LogTag tag, Throwable e) {
        v(tag, e, null);
    }

    public void d(TAG.LogTag tag, String s, Object... args) {
        d(tag, null, s, args);
    }

    public abstract void d(TAG.LogTag tag, Throwable e, String s, Object... args);

    public void d(TAG.LogTag tag, Throwable e) {
        d(tag, e, null);
    }

    public void i(TAG.LogTag tag, String s, Object... args) {
        i(tag, null, s, args);
    }

    public abstract void i(TAG.LogTag tag, Throwable e, String s, Object... args);

    public void i(TAG.LogTag tag, Throwable e) {
        i(tag, e, null);
    }

    public void w(TAG.LogTag tag, String s, Object... args) {
        w(tag, null, s, args);
    }

    public abstract void w(TAG.LogTag tag, Throwable e, String s, Object... args);

    public void w(TAG.LogTag tag, Throwable e) {
        w(tag, e, null);
    }

    public void e(TAG.LogTag tag, String s, Object... args) {
        e(tag, null, s, args);
    }

    public abstract void e(TAG.LogTag tag, Throwable e, String s, Object... args);

    public void e(TAG.LogTag tag, Throwable e) {
        e(tag, e, null);
    }
}
