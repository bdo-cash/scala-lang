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

package hobby.chenai.nakam.basis

import hobby.chenai.nakam.util.Cuttable._
import hobby.chenai.nakam.util.Filler._

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 22/09/2017
  */
trait Ruled[T] extends Equals {
  object A extends Enumeration {
    type A = Value

    val LEFT, RIGHT = Value
  }
  import A._

  protected def minSize: Int

  protected def maxSize: Int

  /** 填充的开头（对齐方向的另一头）。 */
  protected def leadWith: T

  protected val fillWith: T

  protected val align: A

  protected def original: T

  val trim: T

  protected def sizeOf(t: T): Int

  require(minSize <= maxSize)
  require(sizeOf(leadWith) < maxSize)
}

trait RuledString extends Ruled[String] {
  import A._

  override protected def sizeOf(s: String) = s.length

  override lazy val trim = {
    val len = original.length + leadWith.length
    if (len < minSize) {
      val filled = fillStr2Len(minSize - len, fillWith, maxSize - len, align == LEFT)
      if (align == LEFT) original + filled + leadWith else leadWith + filled + original
    } else if (len > maxSize) {
      val cutted = original.cut(maxSize - leadWith.length, align == RIGHT)
      if (align == LEFT) cutted + leadWith else leadWith + cutted
    } else if (align == LEFT) original + leadWith else leadWith + original
  }

  override def equals(o: Any) = o match {
    case that: RuledString => that.canEqual(this) && that.trim == this.trim
    case _ => false
  }

  override def canEqual(that: Any) = that.isInstanceOf[RuledString]
}

trait RuledSeq[E] extends Ruled[Seq[E]] {
  import A._

  override protected def sizeOf(seq: Seq[E]) = seq.size

  override lazy val trim = {
    val len = original.length + leadWith.length
    if (len < minSize) {
      val filled = fill(minSize - len, fillWith)
      if (align == LEFT) original ++ filled ++ leadWith else leadWith ++ filled ++ original
    } else if (len > maxSize) {
      val cutted = original.cut(maxSize - leadWith.length, align == RIGHT)
      if (align == LEFT) cutted ++ leadWith else leadWith ++ cutted
    } else if (align == LEFT) original ++ leadWith else leadWith ++ original
  }

  override def equals(o: Any) = o match {
    case that: RuledSeq[_] => that.canEqual(this) && that.trim == this.trim
    case _ => false
  }

  override def canEqual(that: Any) = that.isInstanceOf[RuledSeq[E]]
}
