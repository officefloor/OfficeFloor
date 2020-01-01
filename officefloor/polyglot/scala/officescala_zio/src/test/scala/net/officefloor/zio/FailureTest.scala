package net.officefloor.zio

import zio.ZIO

/**
 * Tests failure values.
 */
class FailureTest extends TestSpec {

  type Fail[E] = ZIO[Any, E, Object]

  def failThrowable: Fail[Any] = ZIO.fail(FailureTest.FAILURE)

  it can "Throwable" in {
    valid("Throwable", classOf[Throwable], { ex =>
      assert(ex == FailureTest.FAILURE)
    })
  }

  def failString: Fail[String] = ZIO.fail("FAIL")

  it can "String" in {
    valid("String", classOf[ZioException], { ex =>
      ex match {
        case zioEx: ZioException => assert(zioEx.zioCause == "FAIL")
        case _ => fail("Should be " + classOf[ZioException].getName)
      }
    })
  }

  def valid(methodSuffix: String, failureType: Class[_ <: Throwable], exceptionHandler: Throwable => Unit): Unit =
    failure("fail" + methodSuffix, exceptionHandler, { builder =>
      builder.setNextArgumentType(classOf[Object])
    })

}

object FailureTest {
  val FAILURE = new Throwable("FAIL")
}