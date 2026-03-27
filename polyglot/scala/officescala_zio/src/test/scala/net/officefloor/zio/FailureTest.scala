/*-
 * #%L
 * ZIO
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

  def failFoldM: ZIO[Any, Nothing, Int] = ZIO.attempt[Int](throw FailureTest.THROWABLE).foldZIO(
    _ => ZIO.succeed(1),
    _ => throw new Exception("Should not be successful")
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
