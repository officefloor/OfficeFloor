package net.officefloor.zio

/**
 * Tests type aliases.
 */
class TypeAliasTest extends TestSpec {

  it can "ZIO" in {
    typeAlias("ZIO", classOf[Throwable], classOf[Object])
  }

  it can "UIO" in {
    typeAlias("UIO", null, classOf[Object])
  }

  it can "URIO" in {
    typeAlias("URIO", null, classOf[Object])
  }

  it can "Task" in {
    typeAlias("Task", classOf[Throwable], classOf[Object])
  }

  it can "RIO" in {
    typeAlias("RIO", classOf[Throwable], classOf[Object])
  }

  it can "IO" in {
    typeAlias("IO", classOf[Throwable], classOf[Object])
  }

  it can "CustomZIO" in {
    typeAlias("CustomZio", classOf[Throwable], classOf[Object])
  }

  def typeAlias(typeAlias: String, failureClass: Class[_], successClass: Class[_]): Unit =
    valid("type" + typeAlias, Procedures.OBJECT, { builder =>
      if (failureClass != null) {
        // TODO load expected exception
      }
      if (successClass != null) {
        builder.setNextArgumentType(successClass)
      }
    })

}