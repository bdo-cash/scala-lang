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
  *
  */
package fieldnames

import hobby.chenai.nakam.tool.fieldsAsLiteral
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 17 Jul 2017
  *
  */
class FieldsAsLiteralTest extends FunSuite {

  test("@FieldNames annotation on case class") {

    @fieldsAsLiteral
    case class CaseClass(
                          id: Int,
                          name: String,
                          names: Seq[String]
                        )

    """CaseClass.fields.id""" should compile
    """CaseClass.fields.name""" should compile
    """CaseClass.fields.names""" should compile
    """val x: String = CaseClass.fields.names""" should compile
  }

  test("@FieldNames annotation on case class with explicit companion object") {

    @fieldsAsLiteral
    case class CaseClassWC(
                            id: Int,
                            name: String,
                            names: Seq[String]
                          )

    object CaseClassWC {
      val PreExistingField: String = "test"
    }

    """CaseClassWC.fields.id""" should compile
    """CaseClassWC.fields.name""" should compile
    """CaseClassWC.fields.names""" should compile
    """CaseClassWC.PreExistingField""" should compile
    """CaseClassWC(id = 42, name = "this is a name", names = Seq("n", "m"))""" should compile
    """val x: String = CaseClassWC.fields.names""" should compile
    assert(CaseClassWC.PreExistingField == "test")
  }

  test("@FieldNames annotation on non-case class") {

    @fieldsAsLiteral
    class SimpleClass(
                       id: Int,
                       name: String,
                       names: Seq[String]
                     )

    """SimpleClass.fields.id""" should compile
    """SimpleClass.fields.name""" should compile
    """SimpleClass.fields.names""" should compile
    """val x: String = SimpleClass.fields.names""" should compile
  }

  test("@FieldNames annotation on non-case class with private-ctor") {

    @fieldsAsLiteral
    class SimpleClassWPrivateCtor private(
                                           id: Int,
                                           name: String,
                                           names: Seq[String]
                                         )

    """SimpleClassWPrivateCtor.fields.id""" should compile
    """SimpleClassWPrivateCtor.fields.name""" should compile
    """SimpleClassWPrivateCtor.fields.names""" should compile
    """val x: String = SimpleClassWPrivateCtor.fields.names""" should compile
  }

  test("fail compilation w/ @FieldNames annotation on object") {
    """
      |@FieldNames
      |object Test{}
    """ shouldNot compile
  }

  test("fail compilation w/ @FieldNames annotation on field") {
    """
      |    class SimpleClassWPrivateCtor private(
      |      @FieldNames id: Int,
      |      name: String,
      |      names: Seq[String]
      |    )
    """ shouldNot compile
  }

  test("fail compilation w/ @FieldNames annotation on random code block") {
    """
      {
        @FieldNames println("Hello World")
      }
    """ shouldNot compile
  }
}
