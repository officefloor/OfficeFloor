package net.officefloor.zio

import zio.ZEnv
import zio.clock.Clock
import zio.console.Console
import zio.system.System
import zio.random.Random
import zio.blocking.Blocking

import scala.reflect.runtime.universe._

/**
 * Tests environments.
 */
class EnvironmentTest extends TestSpec {

  it can "ZEnv" in {
    valid("ZEnv")
  }

  it can "Clock" in {
    valid("Clock")
  }

  it can "Console" in {
    valid("Console")
  }

  it can "System" in {
    valid("System")
  }

  it can "Random" in {
    valid("Random")
  }

  it can "Blocking" in {
    valid("Blocking")
  }

  it can "Any" in {
    valid("Any")
  }

  it can "AnyRef" in {
    valid("AnyRef")
  }

  it can "Nothing" in {
    valid("Nothing")
  }

  it can "Object" in {
    valid("Object")
  }

  it should "not AnyVal" in {
    invalid("AnyVal", typeOf[AnyVal])
  }

  it should "not Int" in {
    invalid("Int", typeOf[Int])
  }

  it should "not String" in {
    invalid("String", typeOf[String])
  }

  def valid(environment: String): Unit =
    valid("env" + environment, Procedures.OBJECT, { builder =>
      builder.setNextArgumentType(classOf[Object])
    })

  def invalid(environment: String, environmentType: Type): Unit =
    invalid("env" + environment, classOf[IllegalArgumentException].getName + ": ZIO environment may not be custom (requiring " + environmentType.typeSymbol.fullName + ")")

}