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

/**
 * Tests failure values.
 */
class FailureTest extends TestSpec {

  def failThrowable: IO[Nothing] = IO.raiseError(FailureTest.THROWABLE)

  it can "Throwable" in {
    valid("Throwable")
  }

  def failException: IO[Nothing] = IO.raiseError(FailureTest.EXCEPTION)

  it can "Exception" in {
    valid("Exception", classOf[Exception], { ex =>
      assert(ex == FailureTest.EXCEPTION)
    })
  }

  def failError: IO[Nothing] = IO.raiseError(FailureTest.ERROR)

  it can "Error" in {
    valid("Error", classOf[Error], { ex =>
      assert(ex == FailureTest.ERROR)
    })
  }

  def failEither: IO[Nothing] = IO.fromEither(Left(FailureTest.THROWABLE))

  it can "Either" in {
    valid("Either")
  }

  def failTry: IO[Nothing] = IO.fromTry(throw FailureTest.THROWABLE)

  it can "Try" in {
    valid("Try")
  }

  def valid(methodSuffix: String): Unit =
    valid(methodSuffix, classOf[Throwable], { ex =>
      assert(ex == FailureTest.THROWABLE)
    })

  def valid(methodSuffix: String, failureClass: Class[_ <: Throwable], exceptionHandler: Throwable => Unit): Unit =
    failure("fail" + methodSuffix, exceptionHandler, { builder =>
      builder.addEscalationType(classOf[Throwable])
    })

}

object FailureTest {
  val THROWABLE = new Throwable("TEST")
  val EXCEPTION = new Exception("TEST")
  val ERROR = new Error("TEST")
}
