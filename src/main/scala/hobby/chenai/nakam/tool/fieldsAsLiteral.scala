/**
  * Copyright 2017 Loránd Szakács
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package hobby.chenai.nakam.tool

import scala.annotation.StaticAnnotation

/**
  * 摘自：https://github.com/lorandszakacs/field-names.<br>
  * 并进行了二次加工：<br>
  * - 重命名：`FieldNames` -> `fieldsAsLiteral`;
  * - 新增；`object clazz.name`和`def toSeq`;<br>
  * - 增加`final`限定，以便在其它注解中可以引用。
  * <P>
  *
  * Usage:
  *
  * {{{
  *   @FieldNames
  *   case class Repr(
  *     id: String
  *   )
  * }}}
  * Adds an object called `fields` to the companion object of the
  * annotated class which contains variables named after the fields
  * with their names as string values. i.e.
  * {{{
  *   object Repr {
  *     object fields {
  *       val id: String = "id"
  *     }
  *   }
  * }}}
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2017
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 30/06/2018, 功能增强。
  */
class fieldsAsLiteral extends StaticAnnotation {
  // for quasiquotes => q"""sth""". 如果不用`scala.meta`的话，才需要引入这个。否则有冲突。
  // val universe: scala.reflect.runtime.universe.type = scala.reflect.runtime.universe
  // import universe._

  inline def apply(defn: Any): Any = meta {
    import scala.collection._
    import scala.meta._

    val (clazz, companion) = defn match {
      case q"${clazz: Defn.Class}; ${companion: Defn.Object}" => (clazz, companion)
      case clazz: Defn.Class => (clazz, q"object ${Term.Name(clazz.name.value)}")
      case _ => abort("@FieldNames must annotate a case class or class.")
    }

    val paramNames2LiteralStr = clazz.ctor.paramss.flatten.map { p =>
      /*加了类型限定，会被认为不是常量。*/
      q"""final val ${Pat.Var.Term(Term.Name(p.name.value))}/*: String*/ = ${Lit.String(p.name.value)}"""
    }

    val paramNamesSeq =
      q"""def toSeq: ${Type.Apply(Type.Name(classOf[immutable.Seq[_]].getName), List(Type.Name(classOf[String].getName)))} = ${
        Term.Apply(Term.Name(classOf[immutable.Seq[_]].getName), clazz.ctor.paramss.flatten.map { q => Term.Name(q.name.value) })
      }"""

    // 以下俩不能合成一个，否则会在外面多套一层大括号。
    val clazzObject =
      q"""
        object clazz {
          final val name/*: String*/ = ${Lit.String(clazz.name.value)}
        }
      """

    val fieldsObject =
      q"""
        object fields {
          ..$paramNames2LiteralStr
          $paramNamesSeq
        }
      """

    // 伴生对象，复制类体部分（`templ`），并在里面追加`fieldsObject`。
    val newCompanion = companion.copy(
      templ = companion.templ.copy(
        // `stats`几乎表示类中的所有内容（https://scalameta.org/tutorial/#Comprehensivetrees）。
        stats = Some((companion.templ.stats.getOrElse(Nil) :+ clazzObject) :+ fieldsObject)
      )
    )

    // println(s"@${classOf[fieldsAsLiteral].getName} gen class:\n---->{${clazz.mods.mkString(" ")} class " +
    //  s"${clazz.name.value}${clazz.tparams.mkString("[", ", ", "]")}${clazz.ctor.paramss.flatten.mkString("(", ", ", ")")}")
    println(s"----> @${classOf[fieldsAsLiteral].getSimpleName} added newCompanion:\n$newCompanion")

    q"$clazz; $newCompanion"
  }
}
