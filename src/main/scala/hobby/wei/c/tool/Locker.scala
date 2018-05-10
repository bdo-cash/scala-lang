/*
 * Copyright (C) 2016-present, Wei Chou(weichou2010@gmail.com)
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

import java.util.concurrent.locks.{Condition, ReentrantLock}
import hobby.chenai.nakam.lang.J2S
import hobby.chenai.nakam.lang.TypeBring.AsIs

import scala.collection.mutable
import scala.ref.WeakReference

/**
  * 基于{@link ReentrantLock}的<code>synchronized</code>锁实现{@link #sync(Codes, AnyRef)}。
  * 优势在于, 当{@link Thread#interrupt()}请求到达时, 如果还处于等待获取锁状态, 则可以立即中断。
  * <p>
  * 以及基于{@link #sync(Codes, AnyRef)}的{@link #lazyGet(Codes, Codes, ReentrantLock)}懒加载实现。
  *
  * @author Wei Chou(weichou2010@gmail.com)
  * @version 1.0, 27/07/2016
  */
object Locker {
  private val sLocks = new mutable.WeakHashMap[AnyRef, ref.WeakReference[ReentrantLock]]
  private val sLock = new ReentrantLock

  /**
    * 一个等同于synchronized关键字功能的实现, 区别是本方法使用{@link ReentrantLock}锁机制,
    * 当{@link Thread#interrupt()}请求到达时, 如果还处于等待获取锁状态, 则可以立即中断。
    *
    * @param codes     要执行的代码段, 应包裹在{CodeZ#exec()}或{CodeC#exec(Condition[])}中。
    * @param lockScope 在哪个范围进行串行化, 可以是普通对象也可以是Class实例。
    * @tparam T 返回值类型。
    * @return {CodeZ#exec()}或{CodeC#exec(Condition[])}的返回值。
    * @throws InterruptedException 锁中断, codes并未开始执行。
    */
  @throws[InterruptedException]
  def sync$[T](lockScope: AnyRef)(codes: => T): T = sync$(new CodeZ[T] {
    override def exec() = codes
  }, asLock(lockScope))

  @throws[InterruptedException]
  def sync[T](codes: Codes[T], lockScope: AnyRef): T = sync$(codes, asLock(lockScope))

  @throws[InterruptedException]
  def sync[T](codes: => T)(implicit lock: ReentrantLock): T = sync$(new CodeZ[T] {
    override def exec() = codes
  }, lock)

  private def asLock(lockScope: AnyRef, r: Boolean = false): ReentrantLock =
    if (lockScope.isInstanceOf[ReentrantLock]) lockScope.as[ReentrantLock]
    else if (r) getLockr(lockScope) else getLock(lockScope)

  @throws[InterruptedException]
  def sync$[T](codes: Codes[T], lock: ReentrantLock, interruptable: Boolean = true): T = {
    // 如果中断了, 则并没有获取到锁, 不需要unlock(), 同时抛出异常中止本sync方法。
    if (interruptable) lock.lockInterruptibly() else lock.lock()
    try {
      call(codes, lock)
    } finally {
      lock.unlock()
    }
  }

  @throws[InterruptedException]
  private def call[T](codes: Codes[T], lock: ReentrantLock): T = {
    if (codes.isInstanceOf[CodeC[_]]) {
      codes.as[CodeC[T]].exec$(lock)
    } else callr(codes.as[CodeZ[T]], lock)
  }

  /**
    * {@link #sync(Codes, AnyRef)}的无{@link InterruptedException 中断}版。
    */
  def sync$r[T](lockScope: AnyRef)(codes: => T): T = syncr$(new CodeZ[T] {
    override def exec() = codes
  }, asLock(lockScope, r = true))

  def syncr[T](codes: CodeZ[T], lockScope: AnyRef): T = syncr$(codes, asLock(lockScope, r = true))

  /**
    * {@link #sync(Codes, ReentrantLock)}的无{@link InterruptedException 中断}版。
    */
  def syncr[T](codes: => T)(implicit lock: ReentrantLock): T = syncr$(new CodeZ[T] {
    override def exec() = codes
  }, lock)

  def syncr$[T](codes: CodeZ[T], lock: ReentrantLock): T = {
    lock.lock()
    try {
      callr(codes, lock)
    } finally {
      lock.unlock()
    }
  }

  private def callr[T](codes: CodeZ[T], lock: ReentrantLock): T = codes.exec()

  /**
    * 懒加载。
    *
    * @param get    仅仅用来取值的方法。
    * @param create 仅仅用来创建值的方法(不用判断值是否存在)。
    * @param lock   同步锁。
    * @tparam T 返回值类型。
    * @return 需要加载的内容, 是否为null取决于create结果。
    * @throws InterruptedException 被中断。
    */
  @throws[InterruptedException]
  def lazyGet[T](get: => T)(create: => T)(implicit lock: ReentrantLock): T = lazyGet(
    new CodeZ[T] {
      override def exec() = get
    }, new CodeC[T](0) { // 使用这个，主要用于在Java版本的代码中接收异常。
      @throws[InterruptedException]
      override protected def exec(cons: Array[Condition]) = create
    }, lock)

  @throws[InterruptedException]
  def lazyGet[T](get: Codes[T], create: Codes[T], lock: ReentrantLock): T = Option(call(get, lock))
    .getOrElse {
      sync$(new CodeZ[T] {
        override def exec() = Option(call(get, lock)).getOrElse {
          call(create, lock)
        }
      }, lock)
    }

  @throws[InterruptedException]
  def getLock(lockScope: AnyRef): ReentrantLock = lazyGet(
    J2S.getRef(sLocks.getOrElse(lockScope, null)).get) {
    val lock = new ReentrantLock(true) // 公平锁
    sLocks.put(lockScope, new WeakReference(lock))
    lock
  }(sLock)

  /**
    * {@link #lazyGet(Codes, Codes, ReentrantLock)}的无{@link InterruptedException 中断}版。
    */
  def lazyGetr[T](get: => T)(create: => T)(implicit lock: ReentrantLock): T = lazyGetr(
    new CodeZ[T] {
      override def exec() = get
    }, new CodeZ[T] {
      override def exec() = create
    }, lock)

  def lazyGetr[T](get: CodeZ[T], create: CodeZ[T], lock: ReentrantLock): T = Option(callr(get, lock))
    .getOrElse {
      syncr$(new CodeZ[T]() {
        override def exec() = Option(callr(get, lock)).getOrElse {
          callr(create, lock)
        }
      }, lock)
    }

  /**
    * {@link #getLock(AnyRef)}的无{@link InterruptedException 中断}版。
    */
  def getLockr(lockScope: AnyRef): ReentrantLock = lazyGetr(
    J2S.getRef(sLocks.getOrElse(lockScope, null)).orNull) {
    val lock = new ReentrantLock(/*true*/)
    sLocks.put(lockScope, new WeakReference(lock))
    lock
  }(sLock)

  protected trait Codes[T]

  /**
    * 仅返回结果而不支持中断的{@link Codes}.
    *
    * @tparam T
    */
  trait CodeZ[T] extends Codes[T] {
    def exec(): T
  }

  /**
    * 支持{@link Condition}和中断的{@link Codes}.
    *
    * @param num {Condition}需要的数量。
    * @tparam T
    */
  abstract class CodeC[T] protected(num: Int) extends Codes[T] {
    import CodeC._

    @throws[InterruptedException]
    private[Locker] def exec$(lock: ReentrantLock): T = exec(
      lazyGet(if (num == 0) EMPTY else sLockCons.getOrElse(lock, null)) {
        val cons = new Array[Condition](num)
        for (i <- cons.indices) cons(i) = lock.newCondition()
        sLockCons.put(lock, cons)
        cons
      }(lock))

    @throws[InterruptedException]
    protected def exec(cons: Array[Condition]): T
  }

  object CodeC {
    private lazy val sLockCons = new mutable.WeakHashMap[ReentrantLock, Array[Condition]]
    private lazy val EMPTY = new Array[Condition](0)
  }
}
