package net.officefloor.zio

import zio.{IO, RIO, Task, UIO, URIO, ZEnv, ZIO}

/**
 * Tests type aliases.
 */
class TypeAliasTest extends TestSpec {

  def typeZIO: ZIO[Any, Throwable, Object] = zioObject

  it can "ZIO" in {
    typeAlias("ZIO", classOf[Throwable], classOf[Object])
  }

  def typeUIO: UIO[Object] = zioObject

  it can "UIO" in {
    typeAlias("UIO", null, classOf[Object])
  }

  def typeURIO: URIO[ZEnv, Object] = zioObject

  it can "URIO" in {
    typeAlias("URIO", null, classOf[Object])
  }

  def typeTask: Task[Object] = zioObject

  it can "Task" in {
    typeAlias("Task", classOf[Throwable], classOf[Object])
  }

  def typeRIO: RIO[ZEnv, Object] = zioObject

  it can "RIO" in {
    typeAlias("RIO", classOf[Throwable], classOf[Object])
  }

  def typeIO: IO[Throwable, Object] = zioObject

  it can "IO" in {
    typeAlias("IO", classOf[Throwable], classOf[Object])
  }

  type CustomZio = ZIO[ZEnv, Throwable, Object]

  def typeCustomZio: CustomZio = zioObject

  it can "CustomZIO" in {
    typeAlias("CustomZio", classOf[Throwable], classOf[Object])
  }

  def typeAlias(typeAlias: String, failureClass: Class[_], successClass: Class[_]): Unit =
    success("type" + typeAlias, TestSpec.OBJECT, { builder =>
      if (failureClass != null) {
        // TODO load expected exception
      }
      if (successClass != null) {
        builder.setNextArgumentType(successClass)
      }
    })

}