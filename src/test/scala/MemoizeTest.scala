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

import hobby.chenai.nakam.tool.cache._

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 22/07/2017
  */
object MemoizeTest extends Memoize[String, Int] with Lru with LazyGet {
  override protected val maxCacheSize = 5

  override protected val delegate = new Delegate[String, Int] {
    var dbMock: Map[String, Int] = Map("a" -> 0, "b" -> 1, "c" -> 2, "d" -> 3, "f" -> 5)

    override def load(key: String) = {
      print("load:" + key + " -> ")
      dbMock.get(key)
    }

    override def update(key: String, value: Int) = {
      println("update:" + key + " -> " + value)
      dbMock += key -> value
      println(dbMock)
      Option(value)
    }
  }

  def main(args: Array[String]): Unit = {
    trait A {
      val i = 1

      def get = i

      def x(): Unit
    }

    trait B extends A {
      override val i = 5

      override def get = super.get + 1
    }

    trait Ax extends A {
      override val i = 100

      override def get = super.get * 2

      override def x() = ???
    }

    trait C extends B with Ax
    trait D extends Ax with B

    println(new C {}.get)
    println(new D {}.get)

    println(get("a"))
    println(get("d"))
    println(get("e"))
    println(get("f"))

    println(get("a"))
    println(get("d"))
    println(get("e"))
    println(get("f"))

    def run(f: => Unit): Runnable = {
      new Runnable {
        override def run(): Unit = f
      }
    }

    val runner0 = run {
      println("dirty----d, e")
      dirty("d")
      dirty("e")

      println(get("a"))
      println(get("d"))
      println(get("e"))
      println(get("f"))

      println("refresh----")
      refresh("b")
      println(">>>>b refreshed----")

      println(get("b"))

      println(get("a"))
      println(get("d"))
      println(get("e"))
      println(get("f"))
    }

    val runner1 = run {
      println("update----")
      update("e", 4)

      println(get("a"))
      println(get("d"))
      println(get("e"))
      println(get("f"))

      println("clear----")
      clear()

      println(get("a"))
      println(get("d"))
      println(get("e"))
      println(get("f"))
    }

    runner0.run()
    runner1.run()

    //    new Thread(runner0).start()
    //    new Thread(runner1).start()
  }
}
