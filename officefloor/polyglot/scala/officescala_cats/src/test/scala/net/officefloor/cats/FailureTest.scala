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
