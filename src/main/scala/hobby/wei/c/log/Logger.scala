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
import hobby.chenai.nakam.tool.pool.{_2S, S}
import hobby.wei.c.anno.proguard.Burden
import java.util.Date

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 05/01/2018
  */
object Logger extends _2S

class Logger {

  @Burden
  @throws[IllegalArgumentException]
  private def checkArgs(args: Any*): Unit = {
    for (o <- args if o.isInstanceOf[String]) {
      throw new IllegalArgumentException(
        s"请不要直接使用：\n`$o`\n作为参数，以防使用常量字符串，在数组里无法被混淆优化掉。"
        + "如果为常量，请拼接到 String 类型的那个参数一起，"
        + "如果为变量，请使用 `str.s/L.s(str)` 方法包装，并放到格式化参数列表中。"
      )
    }
  }

  private def recycleArgs(args: Any*): Unit = args.foreach {
    case s: S => s.recycle()
    case _    =>
  }

  @inline
  private def level(log: (LogTag, Throwable, => String, Seq[_]) => _)(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = {
    val flats = args.flatten$
    checkArgs(flats: _*) // 放在外面接受检查，等`release`的时候会被直接删除。
    log(tag, e, s, flats)
    recycleArgs(flats: _*)
  }
  //***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****//
  private val _logv = logv _

  @Burden def v(s: => String, args: Any*)(implicit tag: LogTag): Unit         = v(null.asInstanceOf[Throwable], s, args)
  @Burden def v(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = level(_logv)(e, s, args: _*)
  @Burden def v(e: Throwable)(implicit tag: LogTag): Unit                     = v(e, null)
  // TODO: `macros.defName`暂未实现。
  //@Burden def vm(s: => String, args: Any*)(implicit tag: LogTag, defName: String = macros.valOrDefName): Unit = v(s"[$defName]$s", args: _*)
  //@Burden def vm(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag, defName: String = macros.valOrDefName) = v(e, s"[$defName]$s", args: _*)
  //***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****//
  private val _logd = logd _

  def d(s: => String, args: Any*)(implicit tag: LogTag): Unit         = d(null.asInstanceOf[Throwable], s, args)
  def d(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = level(_logd)(e, s, args: _*)
  def d(e: Throwable)(implicit tag: LogTag): Unit                     = d(e, null)
  //***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****//
  private val _logi = logi _

  @Burden def i(s: => String, args: Any*)(implicit tag: LogTag): Unit         = i(null.asInstanceOf[Throwable], s, args)
  @Burden def i(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = level(_logi)(e, s, args: _*)
  @Burden def i(e: Throwable)(implicit tag: LogTag): Unit                     = i(e, null)
  //***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****//
  private val _logw = logw _

  @Burden def w(s: => String, args: Any*)(implicit tag: LogTag): Unit         = w(null.asInstanceOf[Throwable], s, args)
  @Burden def w(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = level(_logw)(e, s, args: _*)
  @Burden def w(e: Throwable)(implicit tag: LogTag): Unit                     = w(e, null)
  //***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****//
  private val _loge = loge _

  def e(s: => String, args: Any*)(implicit tag: LogTag): Unit         = e(null.asInstanceOf[Throwable], s, args)
  def e(e: Throwable, s: => String, args: Any*)(implicit tag: LogTag) = level(_loge)(e, s, args: _*)
  def e(t: Throwable)(implicit tag: LogTag): Unit                     = e(t, null)
  //***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****//

  protected def logv(tag: LogTag, e: Throwable, s: => String, args: Any*): Unit = {
    System.out.println(toMsg("V", tag, e.nonNull, s, args: _*))
    if (e.nonNull) e.printStackTrace(System.out)
  }

  protected def logd(tag: LogTag, e: Throwable, s: => String, args: Any*): Unit = {
    System.out.println(toMsg("D", tag, e.nonNull, s, args: _*))
    if (e.nonNull) e.printStackTrace(System.out)
  }

  protected def logi(tag: LogTag, e: Throwable, s: => String, args: Any*): Unit = {
    System.out.println(toMsg("I", tag, e.nonNull, s, args: _*))
    if (e.nonNull) e.printStackTrace(System.out)
  }

  protected def logw(tag: LogTag, e: Throwable, s: => String, args: Any*): Unit = {
    System.out.println(toMsg("W", tag, e.nonNull, s, args: _*))
    if (e.nonNull) e.printStackTrace(System.out)
  }

  protected def loge(tag: LogTag, e: Throwable, s: => String, args: Any*): Unit = {
    System.out.println(toMsg("E", tag, e.nonNull, s, args: _*))
    if (e.nonNull) e.printStackTrace(System.out)
  }

  private def toMsg(level: String, tag: LogTag, throws: Boolean, s: => String, args: Any*): String =
    new Date + s"<$level/$tag>" + (
      if (args.isEmpty) { val s0 = s; String.valueOf(if (s0.isNull) "<null>" else s0) }
      else s.format(args: _*)
    ) + (if (throws) " | e >:" else "")
}
