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

import scala.concurrent.Future

/**
 * Tests success values.
 */
class SuccessTest extends TestSpec {

  type Success[A] = ZIO[Any, Any, A]

  def successAny: Success[Any] = zioObject

  it can "Any" in {
    valid("Any", TestSpec.OBJECT, classOf[Object])
  }

  def successAnyVal: Success[AnyVal] = ZIO.succeed(1)

  it can "AnyVal" in {
    valid("AnyVal", 1, classOf[Object])
  }

  def successBoolean: Success[Boolean] = ZIO.succeed(true)

  it can "Boolean" in {
    valid("Boolean", true, classOf[Boolean])
  }

  def successByte: Success[Byte] = ZIO.succeed(1)

  it can "Byte" in {
    valid("Byte", 1, classOf[Byte])
  }

  def successShort: Success[Short] = ZIO.succeed(1)

  it can "Short" in {
    valid("Short", 1, classOf[Short])
  }

  def successChar: Success[Char] = ZIO.succeed('A')

  it can "Char" in {
    valid("Char", 'A', classOf[Char])
  }

  def successInt: Success[Int] = ZIO.succeed(1)

  it can "Int" in {
    valid("Int", 1, classOf[Int])
  }

  def successLong: Success[Long] = ZIO.succeed(1L)

  it can "Long" in {
    valid("Long", 1, classOf[Long])
  }

  def successUnit: Success[Unit] = ZIO.succeed(())

  it can "Unit" in {
    valid("Unit", (), null)
  }

  def successAnyRef: Success[AnyRef] = zioObject

  it can "AnyRef" in {
    valid("AnyRef", TestSpec.OBJECT, classOf[Object])
  }

  def successObject: Success[Object] = zioObject

  it can "Object" in {
    valid("Object", TestSpec.OBJECT, classOf[Object])
  }

  def successString: Success[String] = ZIO.succeed("TEST")

  it can "String" in {
    valid("String", "TEST", classOf[String])
  }

  def successOption: Success[Option[Object]] = ZIO.succeed(SuccessTest.OPTION)

  it can "Option" in {
    valid("Option", SuccessTest.OPTION, classOf[Option[Object]])
  }

  def successEither: Success[String] = ZIO.fromEither(Right("TEST"))

  it can "Either" in {
    valid("Either", "TEST", classOf[String])
  }

  def successFuture: Success[String] = ZIO.fromFuture({ implicit ec =>
    Future.successful("FUTURE")
  })

  it can "Future" in {
    valid("Future", "FUTURE", classOf[String])
  }

  def successFutureAsync: Success[Thread] = ZIO.fromFuture({ implicit ec =>
    Future {
      Thread.currentThread()
    }
  })

  it can "Future (async)" in {
    val currentThread = Thread.currentThread()
    test("successFutureAsync", { builder =>
      builder.addEscalationType(classOf[ZioException])
      builder.setNextArgumentType(classOf[Thread])
    }, { _ =>
      assert(TestSpec.success != null)
      assert(TestSpec.success.isInstanceOf[Thread])
      assert(TestSpec.success != currentThread)
    })
  }

  def successEffect: Success[String] = ZIO.attempt("EFFECT")

  it can "Effect" in {
    valid("Effect", "EFFECT", classOf[String])
  }

  def successEffectAsync: Success[String] = ZIO.async[Any, Throwable, String] { callback =>
    callback(ZIO.succeed("EFFECT ASYNC"))
  }

  it can "Effect (async)" in {
    valid("EffectAsync", "EFFECT ASYNC", classOf[String])
  }

  def successEffectBlocking: ZIO[Any, Throwable, String] = ZIO.attemptBlocking("EFFECT BLOCKING")

  it can "Effect (blocking)" in {
    valid("EffectBlocking", "EFFECT BLOCKING", classOf[String], classOf[Throwable])
  }

  def successMap: Success[String] = ZIO.succeed(1).map(v => String.valueOf(v))

  it can "map" in {
    valid("Map", "1", classOf[String])
  }

  def successChain: Success[String] = ZIO.succeed(1).flatMap(v => ZIO.succeed(String.valueOf(v)))

  it can "chain" in {
    valid("Chain", "1", classOf[String])
  }

  def successFor: Success[String] = for {
    a <- ZIO.succeed(1)
    b <- ZIO.succeed(String.valueOf(a))
  } yield b

  it can "for" in {
    valid("For", "1", classOf[String])
  }

  def successZip: Success[(Int, String)] = ZIO.succeed(1).zip(ZIO.succeed("ZIP"))

  it can "zip" in {
    valid("Zip", (1, "ZIP"), classOf[Tuple2[Int, String]])
  }

  def successNull: Success[Null] = ZIO.succeed(null)

  it can "Null" in {
    valid("Null", null, null)
  }

  def successNothing: Success[Nothing] = ZIO.succeed(throw SuccessTest.NOTHING_ESCALATION)

  it can "Nothing" in {
    failure("successNothing", { ex =>
      assert(ex == SuccessTest.NOTHING_ESCALATION)
    }, { builder =>
      builder.addEscalationType(classOf[ZioException])
    })
  }

  it can "on Object" in {
    valid("OnObject", TestSpec.OBJECT, classOf[Object])
  }

  def valid(methodSuffix: String, expectedSuccess: Any, successClass: Class[_]): Unit =
    valid(methodSuffix, expectedSuccess, successClass, classOf[ZioException])


  def valid(methodSuffix: String, expectedSuccess: Any, successClass: Class[_], failureClass: Class[_ <: Throwable]): Unit =
    success("success" + methodSuffix, expectedSuccess, { builder =>
      builder.addEscalationType(failureClass)
      if (successClass != null) {
        builder.setNextArgumentType(successClass)
      }
    })

}

object SuccessTest {
  val OPTION: Option[Object] = Some(TestSpec.OBJECT)
  val NOTHING_ESCALATION = new RuntimeException()

  def successOnObject: ZIO[Any, Any, Object] = ZIO.succeed(TestSpec.OBJECT)
}
