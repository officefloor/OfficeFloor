/*-
 * #%L
 * Cats
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
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
