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
import hobby.chenai.nakam.lang.J2S;
import hobby.chenai.nakam.tool.pool.S;
import hobby.wei.c.anno.proguard.Burden;
import scala.runtime.AbstractFunction0;

/**
 * @author Chenai Nakam(chenai.nakam@gmail.com)
 * @version 1.0, 05/01/2018
 */
public class LoggerJ {
    // 直接引用 Scala 的已有实现。
    private Logger logger = new Logger();

    public LoggerJ() {
    }

    public LoggerJ(Logger logger) {
        this.logger = logger;
    }

    public static S s(String s) {
        return Logger.s(s);
    }

    @Burden
    public void v(TAG.LogTag tag, String s, Object... args) {
        v(tag, null, s, args);
    }

    @Burden
    public void v(TAG.LogTag tag, Throwable e, String s, Object... args) {
        logger.v(e, new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return s;
            }
        }, J2S.toSeq(args), tag);
    }

    @Burden
    public void v(TAG.LogTag tag, Throwable e) {
        v(tag, e, null);
    }

    @Burden
    public void d(TAG.LogTag tag, String s, Object... args) {
        d(tag, null, s, args);
    }

    @Burden
    public void d(TAG.LogTag tag, Throwable e, String s, Object... args) {
        logger.d(e, new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return s;
            }
        }, J2S.toSeq(args), tag);
    }

    @Burden
    public void d(TAG.LogTag tag, Throwable e) {
        d(tag, e, null);
    }

    @Burden
    public void i(TAG.LogTag tag, String s, Object... args) {
        i(tag, null, s, args);
    }

    @Burden
    public void i(TAG.LogTag tag, Throwable e, String s, Object... args) {
        logger.i(e, new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return s;
            }
        }, J2S.toSeq(args), tag);
    }

    @Burden
    public void i(TAG.LogTag tag, Throwable e) {
        i(tag, e, null);
    }

    @Burden
    public void w(TAG.LogTag tag, String s, Object... args) {
        w(tag, null, s, args);
    }

    @Burden
    public void w(TAG.LogTag tag, Throwable e, String s, Object... args) {
        logger.w(e, new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return s;
            }
        }, J2S.toSeq(args), tag);
    }

    @Burden
    public void w(TAG.LogTag tag, Throwable e) {
        w(tag, e, null);
    }

    public void e(TAG.LogTag tag, String s, Object... args) {
        e(tag, null, s, args);
    }

    public void e(TAG.LogTag tag, Throwable e, String s, Object... args) {
        logger.e(e, new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return s;
            }
        }, J2S.toSeq(args), tag);
    }

    public void e(TAG.LogTag tag, Throwable e) {
        e(tag, e, null);
    }
}
