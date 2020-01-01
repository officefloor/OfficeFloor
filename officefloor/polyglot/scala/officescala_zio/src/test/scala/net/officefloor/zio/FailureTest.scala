package net.officefloor.zio

import zio.ZIO

/**
 * Tests failure values.
 */
class FailureTest extends TestSpec {

  type Fail[E] = ZIO[Any, E, Object]

  def failThrowable: Fail[Any] = ZIO.fail(FailureTest.THROWABLE)

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

  def valid(methodSuffix: String): Unit =
    valid(methodSuffix, classOf[Throwable], { ex =>
      assert(ex == FailureTest.THROWABLE)
    })

  def valid(methodSuffix: String, failureType: Class[_ <: Throwable], exceptionHandler: Throwable => Unit): Unit =
    failure("fail" + methodSuffix, exceptionHandler, { builder =>
      builder.setNextArgumentType(classOf[Object])
    })

}

object FailureTest {
  val THROWABLE = new Throwable("TEST")
  val EXCEPTION = new Exception("TEST")
  val ERROR = new Error("TEST")
}