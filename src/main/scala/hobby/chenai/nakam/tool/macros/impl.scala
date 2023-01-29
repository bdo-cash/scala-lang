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
import scala.annotation.tailrec
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
    val tp2           = keyImpl.definingValOrDefName(c, Some(true), methodName => s"""$methodName must be directly assigned to a val, such as `val x = $methodName`.""")
    val enclosingName = tp2._1.ensuring(_.isDefined).get // 如果为`None`，上一步应该报错。
    val name          = c.Expr[String](Literal(Constant(enclosingName)))
    println(s"---> macros.valName: ${name.tree}")
    reify(name.splice)
  }

  def defName(c: whitebox.Context): c.Expr[String] = {
    import c.universe._
    val tp2           = keyImpl.definingValOrDefName(c, Some(false), methodName => s"""$methodName must be directly assigned to a def, such as `def x = $methodName`.""")
    val enclosingName = tp2._2.ensuring(_.isDefined).get // 如果为`None`，上一步应该报错。
    val name          = c.Expr[String](Literal(Constant(enclosingName)))
    println(s"---> macros.defName: ${name.tree}")
    reify(name.splice)
  }

  def valOrDefName(c: whitebox.Context): c.Expr[String] = {
    import c.universe._
    val tp2           = keyImpl.definingValOrDefName(c, None, methodName => s"""$methodName must be directly assigned to a val or def, such as `val/def x = $methodName`.""")
    val enclosingName = tp2._1.orElse(tp2._2).ensuring(_.isDefined).get // 如果为`None`，上一步应该报错。
    val name          = c.Expr[String](Literal(Constant(enclosingName)))
    println(s"---> macros.valOrDefName: ${name.tree}")
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
          (clazz @ q"$_ class $tpname[..$_] $_(...$paramss) extends { ..$_ } with ..$_ { $_ => ..$_ }") :: companion :: Nil =>
        (clazz, companion, paramss.as[Seq[Seq[ValDef]]].flatten, true)
      case /*same as above*/
          (clazz @ q"$_ class $tpname[..$_] $_(...$paramss) extends { ..$_ } with ..$_ { $_ => ..$_ }") :: Nil =>
        (clazz, q"object ${tpname.as[TypeName].toTermName}", paramss.as[Seq[Seq[ValDef]]].flatten, false)
      case defn @ _ => throw new MacroIllegalStateException(s"@fieldsAsLiteral must annotate a `case class` or `class`. $defn")
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
      val appl   = reify(immutable.Seq).tree
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
    // "macros.@${classOf[fieldsAsLiteral].getSimpleName}" // 报错符合`@compileTimeOnly`的定义
    println(s"---> macros.@fieldsAsLiteral ${if (compDefined) "INSERT TREEs into" else "GEN"} COMPANION:\n$newCompanion")

    c.Expr(q"$clazz; $newCompanion")
  }
}

// from `sbt.std.KeyMacros`
object keyImpl {

  def definingValOrDefName(c: whitebox.Context, valOrDef: Option[Boolean], invalidEnclosingTree: String => String): (Option[String], Option[String]) = {
    import c.universe.{Apply => ApplyTree, _}
    // 目测应该是`调用`而不是`定义`的方法名。
    val methodName                   = c.macroApplication.symbol.name
    def processName(n: Name): String = n.decodedName.toString.trim // trim is not strictly correct, but macros don't expose the API necessary
    def processError()               = c.error(c.enclosingPosition, invalidEnclosingTree(methodName.decodedName.toString))
    @tailrec
    def enclosingVal(trees: List[c.Tree], strict: Boolean = true): Option[String] = {
      trees match {
        case ValDef(_, name, _, _) :: _                      => Some(processName(name))
        case (_: ApplyTree | _: Select | _: TypeApply) :: xs => enclosingVal(xs)
        // (only when the explicit type is present, though) ：
        // 虽然，仅当提供了显式的类型 X 时。潜台词是说，写代码时很可能没写`: X`，但…可能编译之后就有显式的类型了。
        // lazy val x: X = <methodName> has this form for some reason (only when the explicit type is present, though)
        case Block(_, _) :: DefDef(mods, name, _, _, _, _) :: _ if mods.hasFlag(Flag.LAZY) => Some(processName(name))
        case _ =>
          if (strict) processError()
          //"<error>"
          None
      }
    }

    // TODO: 不行，不是要找`def xxx = <methodName>(p0, p1, p2)`，而是`def xxx(p0, p1, p2) = { macros.defName }`。
    @tailrec
    def enclosingDef(trees: List[c.Tree], strict: Boolean = true): Option[String] = {
      trees match {
        case (_: ValDef | _: ApplyTree | _: Select | _: TypeApply) :: xs => enclosingDef(xs)
        // 改为 if !xxx.
        case Block(_, _) :: DefDef(mods, name, _, _, _, _) :: _ if !mods.hasFlag(Flag.LAZY) => Some(processName(name))
        case _ =>
          if (strict) processError()
          //"<error>"
          None
      }
    }
    val lis = enclosingTrees(c).asInstanceOf[List[c.Tree]]
    if (valOrDef.isEmpty) {
      val tp2 = (enclosingVal(lis, false), enclosingDef(lis, false))
      if (tp2._1.orElse(tp2._2).isEmpty) processError()
      tp2
    } else if (valOrDef.get) (enclosingVal(lis), None)
    else (None, enclosingDef(lis))
  }

  def enclosingTrees(c: whitebox.Context): List[c.Tree] = c.enclosingImplicits.map(_.tree)
}
