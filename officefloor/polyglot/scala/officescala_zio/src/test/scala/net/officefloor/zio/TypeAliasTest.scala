package net.officefloor.zio

import zio._

/**
 * Tests type aliases.
 */
class TypeAliasTest extends TestSpec {

  type CustomZio = ZIO[ZEnv, Throwable, Object]

  it can "ZIO" in {
    typeAlias("ZIO", classOf[Throwable], classOf[Object])
  }

  def typeZIO: ZIO[Any, Throwable, Object] = zioObject

  it can "UIO" in {
    typeAlias("UIO", null, classOf[Object])
  }

  def typeUIO: UIO[Object] = zioObject

  it can "URIO" in {
    typeAlias("URIO", null, classOf[Object])
  }

  def typeURIO: URIO[ZEnv, Object] = zioObject

  it can "Task" in {
    typeAlias("Task", classOf[Throwable], classOf[Object])
  }

  def typeTask: Task[Object] = zioObject

  it can "RIO" in {
    typeAlias("RIO", classOf[Throwable], classOf[Object])
  }

  def typeRIO: RIO[ZEnv, Object] = zioObject

  it can "IO" in {
    typeAlias("IO", classOf[Throwable], classOf[Object])
  }

  def typeIO: IO[Throwable, Object] = zioObject

  def typeCustomZio: CustomZio = zioObject

  it can "CustomZIO" in {
    typeAlias("CustomZio", classOf[Throwable], classOf[Object])
  }

  def typeAlias(typeAlias: String, failureClass: Class[_ <: Throwable], successClass: Class[_]): Unit =
    success("type" + typeAlias, TestSpec.OBJECT, { builder =>
      if (failureClass != null) {
        builder.addEscalationType(failureClass)
      }
      if (successClass != null) {
        builder.setNextArgumentType(successClass)
      }
    })

}