/*
 * Copyright (C) 2020-present, Chenai Nakam(chenai.nakam@gmail.com)
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

package hobby.chenai.nakam.tool.macros

import hobby.chenai.nakam.lang.TypeBring.AsIs

import scala.collection.immutable
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 09/09/2020
  */
private[macros] object impl {
  def valName(c: whitebox.Context): c.Expr[String] = {
    import c.universe._
    val enclosingValName = keyImpl.definingValName(c, methodName => s"""$methodName must be directly assigned to a val, such as `val x = $methodName`.""")
    val name = c.Expr[String](Literal(Constant(enclosingValName)))
    println(s"---> macros.valName: ${name.tree}")
    reify(name.splice)
  }

  // https://docs.scala-lang.org/overviews/macros/annotations.html
  // https://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html#definitions
  def fieldsAsLiteral(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs = annottees.map(_.tree).toList

    val (clazz: ClassDef, companion: ModuleDef, paramss: Seq[ValDef], compDefined: Boolean) = inputs match {
      case /*Brackets `()` must be added here, otherwise it will be parsed as `Seq[ClassDef]`, and `extends {..$earlydefns }` matches abnormally.*/
        // q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }"
        (clazz@q"$_ class $tpname[..$_] $_(...$paramss) extends { ..$_ } with ..$_ { $_ => ..$_ }") :: companion :: Nil =>
        (clazz, companion, paramss.as[Seq[Seq[ValDef]]].flatten, true)
      case /*same as above*/
        (clazz@q"$_ class $tpname[..$_] $_(...$paramss) extends { ..$_ } with ..$_ { $_ => ..$_ }") :: Nil =>
        (clazz, q"object ${tpname.as[TypeName].toTermName}", paramss.as[Seq[Seq[ValDef]]].flatten, false)
      case defn@_ => throw new MacroIllegalStateException(s"@fieldsAsLiteral must annotate a `case class` or `class`. $defn")
    }

    val paramNames2LiteralStr: Seq[Tree] = paramss.map { p =>
      val pn = p.name
      /*Added the type qualification, will be considered not constant.*/
      q"""final val $pn /*: String*/ = ${pn.decodedName.toString}"""
    }

    val paramNamesSeq = {
      val tpe = typeOf[immutable.Seq[String]]
      // val lits = paramss.map(p => p.name)

      // This way of writing can't use `immutable.Seq[String]`, would cause:
      // xxx.scala:489: not found: value scala.collection.immutable.Seq/immutable.Seq$/immutable.Seq.apply()
      // various methods have been tried.
      // q"""def toSeq: $tpe = Seq(..$lits)"""

      // val appl = Select(reify(immutable.Seq).tree, TermName("apply"))
      val appl = reify(immutable.Seq).tree
      val idents = paramss.map(p => Ident(p.name)).toList
      q"""def toSeq: $tpe = ${Apply(appl, idents)}"""
    }

    val clazzObject =
      q"""
        object clazz {
          final val name/*: String*/ = ${clazz.name.decodedName.toString}
        }
      """

    val fieldsObject =
      q"""
        object fields {
          ..$paramNames2LiteralStr
          $paramNamesSeq
        }
      """

    val newCompanion = companion match {
      case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" =>
        q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..${body :+ clazzObject :+ fieldsObject} }"
      case _ => throw new MacroIllegalStateException(s"object `${companion.symbol.name.decodedName.toString}` not defined correctly? $companion")
    }
    println(s"---> macros.@${classOf[fieldsAsLiteral].getSimpleName} ${if (compDefined) "INSERT TREEs into" else "GEN"} COMPANION:\n$newCompanion")

    c.Expr(q"$clazz; $newCompanion")
  }
}

// from `sbt.std.KeyMacros`
object keyImpl {
  def definingValName(c: whitebox.Context, invalidEnclosingTree: String => String): String = {
    import c.universe.{Apply => ApplyTree, _}
    val methodName = c.macroApplication.symbol.name

    def processName(n: Name): String = n.decodedName.toString.trim // trim is not strictly correct, but macros don't expose the API necessary
    def enclosingVal(trees: List[c.Tree]): String = {
      trees match {
        case vd@ValDef(_, name, _, _) :: ts => processName(name)
        case (_: ApplyTree | _: Select | _: TypeApply) :: xs => enclosingVal(xs)
        // lazy val x: X = <methodName> has this form for some reason (only when the explicit type is present, though)
        case Block(_, _) :: DefDef(mods, name, _, _, _, _) :: xs if mods.hasFlag(Flag.LAZY) => processName(name)
        case _ =>
          c.error(c.enclosingPosition, invalidEnclosingTree(methodName.decodedName.toString))
          "<error>"
      }
    }

    enclosingVal(enclosingTrees(c).toList)
  }

  def enclosingTrees(c: whitebox.Context): Seq[c.Tree] =
    c.asInstanceOf[reflect.macros.runtime.Context].callsiteTyper.context.enclosingContextChain.map(_.tree.asInstanceOf[c.Tree])
}