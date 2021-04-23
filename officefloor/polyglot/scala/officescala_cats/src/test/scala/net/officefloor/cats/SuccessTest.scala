/*-
 * #%L
 * Cats
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.cats

import cats.effect.IO

import scala.concurrent.{ExecutionContext, Future}

/**
 * Tests success values.
 */
class SuccessTest extends TestSpec {

  def successAny: IO[Any] = ioObject

  it can "Any" in {
    valid("Any", TestSpec.OBJECT, classOf[Object])
  }

  def successAnyVal: IO[AnyVal] = IO.pure(1)

  it can "AnyVal" in {
    valid("AnyVal", 1, classOf[Object])
  }

  def successBoolean: IO[Boolean] = IO.pure(true)

  it can "Boolean" in {
    valid("Boolean", true, classOf[Boolean])
  }

  def successByte: IO[Byte] = IO.pure(1)

  it can "Byte" in {
    valid("Byte", 1, classOf[Byte])
  }

  def successShort: IO[Short] = IO.pure(1)

  it can "Short" in {
    valid("Short", 1, classOf[Short])
  }

  def successChar: IO[Char] = IO.pure('A')

  it can "Char" in {
    valid("Char", 'A', classOf[Char])
  }

  def successInt: IO[Int] = IO.pure(1)

  it can "Int" in {
    valid("Int", 1, classOf[Int])
  }

  def successLong: IO[Long] = IO.pure(1)

  it can "Long" in {
    valid("Long", 1, classOf[Long])
  }

  def successUnit: IO[Unit] = IO.unit

  it can "Unit" in {
    valid("Unit", (), null)
  }

  def successAnyRef: IO[AnyRef] = ioObject

  it can "AnyRef" in {
    valid("AnyRef", TestSpec.OBJECT, classOf[Object])
  }

  def successObject: IO[Object] = ioObject

  it can "Object" in {
    valid("Object", TestSpec.OBJECT, classOf[Object])
  }

  def successString: IO[String] = IO.pure("TEST")

  it can "String" in {
    valid("String", "TEST", classOf[String])
  }

  def successOption: IO[Option[Object]] = IO.pure(SuccessTest.OPTION)

  it can "Option" in {
    valid("Option", SuccessTest.OPTION, classOf[Option[Object]])
  }

  def successMap: IO[String] = IO(1).map(v => String.valueOf(v))

  it can "map" in {
    valid("Map", "1", classOf[String])
  }

  def successChain: IO[String] = IO(1).flatMap(v => IO.pure(String.valueOf(v)))

  it can "chain" in {
    valid("Chain", "1", classOf[String])
  }

  def successFor: IO[String] = for {
    a <- IO(1)
    b <- IO(String.valueOf(a))
  } yield b

  it can "for" in {
    valid("For", "1", classOf[String])
  }

  def successEffect: IO[String] = IO.apply("EFFECT")

  it can "Effect" in {
    valid("Effect", "EFFECT", classOf[String])
  }

  def successEffectAsync: IO[String] = IO.async { callback =>
    IO {
      callback(Right("EFFECT ASYNC"))
      Some(IO())
    }
  }

  it can "Effect (async)" in {
    valid("EffectAsync", "EFFECT ASYNC", classOf[String])
  }

  def successFuture(ec: ExecutionContext): IO[String] = IO.fromFuture(IO {
    Future.successful("FUTURE")
  })

  it can "Future" in {
    valid("Future", "FUTURE", classOf[String])
  }

  def successEither: IO[String] = IO.fromEither(Right("TEST"))

  it can "Either" in {
    valid("Either", "TEST", classOf[String])
  }

  def successNull: IO[Null] = IO.pure(null)

  it can "Null" in {
    valid("Null", null, null)
  }

  def successNothing: IO[Nothing] = IO.apply(throw SuccessTest.NOTHING_ESCALATION)

  it can "Nothing" in {
    failure("successNothing", { ex =>
      assert(ex == SuccessTest.NOTHING_ESCALATION)
    }, { builder =>
      builder.addEscalationType(classOf[Throwable])
    })
  }

  it can "on Object" in {
    valid("OnObject", TestSpec.OBJECT, classOf[Object])
  }

  def valid(methodSuffix: String, expectedSuccess: Any, successClass: Class[_]): Unit =
    success("success" + methodSuffix, expectedSuccess, { builder =>
      builder.addEscalationType(classOf[Throwable])
      if (successClass != null) {
        builder.setNextArgumentType(successClass)
      }
    })

}

object SuccessTest {
  val OPTION: Option[Object] = Some(TestSpec.OBJECT)
  val NOTHING_ESCALATION = new RuntimeException()

  def successOnObject: IO[Object] = IO.pure(TestSpec.OBJECT)
}
