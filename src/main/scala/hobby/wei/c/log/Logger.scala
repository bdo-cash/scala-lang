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

package hobby.wei.c.log

import hobby.chenai.nakam.basis.TAG._
import hobby.chenai.nakam.lang.J2S._
import hobby.chenai.nakam.tool.pool.S
import hobby.wei.c.anno.proguard.Burden

/**
  * @author Chenakam (chenai.nakam@gmail.com)
  * @version 1.0, 05/01/2018
  */
abstract class Logger {
  @Burden
  @throws[IllegalArgumentException]
  private def checkArgs(args: Any*): Unit = {
    for (o <- args) {
      if (o.isInstanceOf[String]) throw new IllegalArgumentException("\"" + o
        + "\"\n请不要使用 String 作为参数，"
        + "以防使用常量字符串，在数组里无法被混淆优化掉。常量请拼接到 String 类型的那个参数一起，"
        + "如果为变量，请使用 `str.s/L.s(str)` 方法包装。")
    }
  }

  private def recycleArgs(args: Any*): Unit = {
    for (o <- args) {
      o match {
        case s: S => s.recycle()
        case _ =>
      }
    }
  }

  @Burden
  def v(s: => String, args: Any*)(implicit tag: LogTag): Unit = v(null.asInstanceOf[Throwable], s, args)

  @Burden
  def v(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = {
    val flats = args.flatten$
    checkArgs(flats: _*) // 放在外面接受检查，等release的时候会被直接删除。
    logv(tag, e, s, flats: _*)
    recycleArgs(flats: _*)
  }

  protected def logv(tag: LogTag, e: Throwable, s: => String, args: Any*)

  @Burden
  def v(e: Throwable)(implicit tag: LogTag): Unit = v(e, null)

  @Burden
  def d(s: => String, args: Any*)(implicit tag: LogTag): Unit = d(null.asInstanceOf[Throwable], s, args)

  @Burden
  def d(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = {
    val flats = args.flatten$
    checkArgs(flats: _*)
    logd(tag, e, s, flats: _*)
    recycleArgs(flats: _*)
  }

  protected def logd(tag: LogTag, e: Throwable, s: => String, args: Any*)

  @Burden
  def d(e: Throwable)(implicit tag: LogTag): Unit = d(e, null)

  @Burden
  def i(s: => String, args: Any*)(implicit tag: LogTag): Unit = i(null.asInstanceOf[Throwable], s, args)

  @Burden
  def i(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = {
    val flats = args.flatten$
    checkArgs(flats: _*)
    logi(tag, e, s, flats: _*)
    recycleArgs(flats: _*)
  }

  protected def logi(tag: LogTag, e: Throwable, s: => String, args: Any*)

  @Burden
  def i(e: Throwable)(implicit tag: LogTag): Unit = i(e, null)

  @Burden
  def w(s: => String, args: Any*)(implicit tag: LogTag): Unit = w(null.asInstanceOf[Throwable], s, args)

  @Burden
  def w(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = {
    val flats = args.flatten$
    checkArgs(flats: _*)
    logw(tag, e, s, flats: _*)
    recycleArgs(flats: _*)
  }

  protected def logw(tag: LogTag, e: Throwable, s: => String, args: Any*)

  @Burden
  def w(e: Throwable)(implicit tag: LogTag): Unit = w(e, null)

  def e(s: => String, args: Any*)(implicit tag: LogTag): Unit = e(null.asInstanceOf[Throwable], s, args)

  def e(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = {
    val flats = args.flatten$
    checkArgs(flats: _*)
    loge(tag, e, s, flats: _*)
    recycleArgs(flats: _*)
  }

  protected def loge(tag: LogTag, e: Throwable, s: => String, args: Any*)

  def e(t: Throwable)(implicit tag: LogTag): Unit = e(t, null)
}
