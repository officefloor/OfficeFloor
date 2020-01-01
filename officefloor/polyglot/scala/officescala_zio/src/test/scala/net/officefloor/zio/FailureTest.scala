package net.officefloor.zio

import zio.{Task, ZIO}

/**
 * Tests failure values.
 */
class FailureTest extends TestSpec {

  type Fail[E] = ZIO[Any, E, Object]

  def failThrowable: Fail[Any] = ZIO.fail(new Throwable("FAIL"))

  it can "Throwable" in {
    failure("Throwable", classOf[Throwable], "FAIL")
  }

  def failure(methodSuffix: String, failureType: Class[_ <: Throwable], message: String): Unit =
    failure("fail" + methodSuffix, failureType, message, { builder =>
      builder.setNextArgumentType(classOf[Object])
    })

}