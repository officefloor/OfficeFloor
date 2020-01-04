/*-
 * #%L
 * ZIO
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

package net.officefloor.zio

import zio.ZIO

/**
 * Tests failure values.
 */
class FailureTest extends TestSpec {

  type Fail[E] = ZIO[Any, E, Object]

  def failThrowable: Fail[Throwable] = ZIO.fail(FailureTest.THROWABLE)

  it can "Throwable" in {
    valid("Throwable")
  }

  def failException: Fail[Exception] = ZIO.fail(FailureTest.EXCEPTION)

  it can "Exception" in {
    valid("Exception", classOf[Exception], { ex =>
      assert(ex == FailureTest.EXCEPTION)
    })
  }

  def failError: Fail[Error] = ZIO.fail(FailureTest.ERROR)

  it can "Error" in {
    valid("Error", classOf[Error], { ex =>
      assert(ex == FailureTest.ERROR)
    })
  }

  def failAny: Fail[Any] = ZIO.fail(FailureTest.THROWABLE)

  it can "Any" in {
    valid("Any", classOf[ZioException], {ex =>
      assert(ex == FailureTest.THROWABLE)
    })
  }

  def failString: Fail[String] = ZIO.fail("FAIL")

  it can "String" in {
    valid("String", classOf[ZioException], { ex =>
      ex match {
        case zioEx: ZioException => assert(zioEx.zioCause == "FAIL")
        case _ => fail("Should be " + classOf[ZioException].getName + " but was " + ex.getClass.getName)
      }
    })
  }

  def failEither: Fail[Throwable] = ZIO.fromEither(Left(FailureTest.THROWABLE))

  it can "Either" in {
    valid("Either")
  }

  def failTry: Fail[Throwable] = ZIO.fromTry(throw FailureTest.THROWABLE)

  it can "Try" in {
    valid("Try")
  }

  def failFoldM: ZIO[Any, Nothing, Int] = ZIO.effect(throw FailureTest.THROWABLE).foldM(
    error => ZIO.succeed(1),
    success => throw new Exception("Should not be successful")
  )

  it can "foldM" in {
    success("failFoldM", 1, { builder =>
      builder.setNextArgumentType(classOf[Int])
    })
  }

  def failNothing: Fail[Nothing] = ZIO.fail(throw FailureTest.THROWABLE)

  it can "Nothing" in {
    valid("Nothing", null, {ex =>
      assert(ex == FailureTest.THROWABLE)
    })
  }

  def valid(methodSuffix: String): Unit =
    valid(methodSuffix, classOf[Throwable], { ex =>
      assert(ex == FailureTest.THROWABLE)
    })

  def valid(methodSuffix: String, failureClass: Class[_ <: Throwable], exceptionHandler: Throwable => Unit): Unit =
    failure("fail" + methodSuffix, exceptionHandler, { builder =>
      if (failureClass != null) {
        builder.addEscalationType(failureClass)
      }
      builder.setNextArgumentType(classOf[Object])
    })

}

object FailureTest {
  val THROWABLE = new Throwable("TEST")
  val EXCEPTION = new Exception("TEST")
  val ERROR = new Error("TEST")
}
