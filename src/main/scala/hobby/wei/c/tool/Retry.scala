/*
 * Copyright (C) 2018-present, Chenai Nakam(chenai.nakam@gmail.com)
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

package hobby.wei.c.tool

import hobby.chenai.nakam.basis.TAG
import hobby.wei.c.log.Logger


/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 12/06/2018
  */
trait Retry extends TAG.ClassName {
  protected val logger: Logger

  import logger._

  /**
    * 重试一个操作指定的次数，直到成功，或者用完次数。
    *
    * @param delayMillis 延迟多长时间后重试。单位：毫秒。
    * @param times       最多重试多少次。
    * @param increase    延时递增，在`delay`的基础上。
    * @param from        从什么时间开始递增。
    * @param action      具体要执行的操作。该函数的参数为`times`，返回`true`表示成功，结束重试。
    * @param delayer     用于延迟`action`执行时间的延迟器。
    */
  def retryForceful(delayMillis: Int, times: Int = 8, increase: Int = 0, from: Int = 0)(action: Int => Boolean)(
    implicit delayer: Delayer): Unit = if (times > 0) {
    i("retryForceful | delayMillis: %s, times: %s, increase: %s, from: %s, action: %s.", delayMillis, times, increase, from, action)
    if (!action(times) && times > 1) delayer.delay(
      if ((times - 1) < from) delayMillis + increase * (from - (times - 1)) else delayMillis) {
      retryForceful(delayMillis, times - 1, increase, from)(action)
    }
  }

  def delay(delayMillis: Int)(action: => Unit)(implicit delayer: Delayer): Unit = delayer.delay(delayMillis)((() => action) ())

  trait Delayer {
    def delay(delayMillis: Int)(action: => Unit): Unit
  }
}

trait RetryBySleep extends Retry {
  override protected lazy val logger = new Logger
  import logger._

  implicit lazy val delayer: Delayer = new Delayer {
    override def delay(delayMillis: Int)(action: => Unit): Unit = {
      w("delayAction | delayMillis: %s.", delayMillis)
      Thread.sleep(delayMillis)
      action
    }
  }
}
