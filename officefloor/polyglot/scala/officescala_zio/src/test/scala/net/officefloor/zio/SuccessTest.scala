package net.officefloor.zio

import zio.{Task, ZIO}

import scala.reflect.runtime.universe._

/**
 * Tests success values.
 */
class SuccessTest extends TestSpec {

  def successAny: Task[Any] = zioObject

  it can "Any" in {
    valid("Any", TestSpec.OBJECT, classOf[Object])
  }

  def successAnyVal: Task[AnyVal] = ZIO.succeed(1)

  it can "AnyVal" in {
    valid("AnyVal", 1, classOf[Object])
  }

  def successBoolean: Task[Boolean] = ZIO.succeed(true)

  it can "Boolean" in {
    valid("Boolean", true, classOf[Boolean])
  }

  def successByte: Task[Byte] = ZIO.succeed(1)

  it can "Byte" in {
    valid("Byte", 1, classOf[Byte])
  }

  def successShort: Task[Short] = ZIO.succeed(1)

  it can "Short" in {
    valid("Short", 1, classOf[Short])
  }

  def successChar: Task[Char] = ZIO.succeed('A')

  it can "Char" in {
    valid("Char", 'A', classOf[Char])
  }

  def successInt: Task[Int] = ZIO.succeed(1)

  it can "Int" in {
    valid("Int", 1, classOf[Int])
  }

  def successLong: Task[Long] = ZIO.succeed(1)

  it can "Long" in {
    valid("Long", 1, classOf[Long])
  }

  def successUnit: Task[Unit] = ZIO.succeed(())

  it can "Unit" in {
    valid("Unit", (), null)
  }

  def successAnyRef: Task[AnyRef] = zioObject

  it can "AnyRef" in {
    valid("AnyRef", TestSpec.OBJECT, classOf[Object])
  }

  def successObject: Task[Object] = zioObject

  it can "Object" in {
    valid("Object", TestSpec.OBJECT, classOf[Object])
  }

  def successString: Task[String] = ZIO.succeed("TEST")

  it can "String" in {
    valid("String", "TEST", classOf[String])
  }

  def successOption: Task[Option[Object]] = ZIO.succeed(SuccessTest.OPTION)

  it can "Option" in {
    valid("Option", SuccessTest.OPTION, classOf[Option[Object]])
  }

  def successNull: Task[Null] = ZIO.succeed(null)

  it can "Null" in {
    valid("Null", null, null)
  }

  def successNothing: Task[Nothing] = ZIO.succeed({
    throw new RuntimeException()
  })

  it can "Nothing" in {
    valid("Nothing", null, null)
  }

  def valid(methodSuffix: String, expectedSuccess: Any, successType: Class[_]): Unit =
    success("success" + methodSuffix, expectedSuccess, { builder =>
      if (successType != null) {
        builder.setNextArgumentType(successType)
      }
    })

}

object SuccessTest {

  val OPTION: Option[Object] = Some(TestSpec.OBJECT)

}