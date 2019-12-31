package net.officefloor.zio

/**
 * Tests type aliases.
 */
class TypeAliasTest extends TestSpec {

  "Type Alias" can "ZIO" in {
    testType("zio", classOf[Throwable], classOf[Object])
  }

  it can "UIO" in {
    testType("uio", null, classOf[Object])
  }

  it can "URIO" in {
    testType("urio", null, classOf[Object])
  }

  it can "Task" in {
    testType("task", classOf[Throwable], classOf[Object])
  }

  it can "RIO" in {
    testType("rio", classOf[Throwable], classOf[Object])
  }

  it can "IO" in {
    testType("io", classOf[Throwable], classOf[Object])
  }

  it can "CustomZIO" in {
    testType("customZio", classOf[Throwable], classOf[Object])
  }

}