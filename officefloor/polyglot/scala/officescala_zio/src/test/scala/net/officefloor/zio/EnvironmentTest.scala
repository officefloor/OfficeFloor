package net.officefloor.zio

import zio.{ZEnv, ZIO}
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

  type Env[R] = ZIO[R, Throwable, Object]

  def envZEnv: Env[ZEnv] = zioObject

  it can "ZEnv" in {
    valid("ZEnv")
  }

  def envClock: Env[Clock] = zioObject


  it can "Clock" in {
    valid("Clock")
  }

  def envConsole: Env[Console] = zioObject


  it can "Console" in {
    valid("Console")
  }

  def envSystem: Env[System] = zioObject


  it can "System" in {
    valid("System")
  }

  def envRandom: Env[Random] = zioObject


  it can "Random" in {
    valid("Random")
  }

  def envBlocking: Env[Blocking] = zioObject


  it can "Blocking" in {
    valid("Blocking")
  }

  def envAny: Env[Any] = zioObject


  it can "Any" in {
    valid("Any")
  }

  def envAnyRef: Env[Any] = zioObject


  it can "AnyRef" in {
    valid("AnyRef")
  }

  def envNothing: Env[Nothing] = zioObject


  it can "Nothing" in {
    valid("Nothing")
  }

  def envObject: Env[Object] = zioObject


  it can "Object" in {
    valid("Object")
  }

  def envAnyVal: Env[AnyVal] = zioObject


  it should "not AnyVal" in {
    invalid("AnyVal", typeOf[AnyVal])
  }

  def envInt: Env[Int] = zioObject


  it should "not Int" in {
    invalid("Int", typeOf[Int])
  }

  def envString: Env[String] = zioObject

  it should "not String" in {
    invalid("String", typeOf[String])
  }

  def valid(environment: String): Unit =
    success("env" + environment, TestSpec.OBJECT, { builder =>
      builder.setNextArgumentType(classOf[Object])
    })

  def invalid(environment: String, environmentType: Type): Unit =
    invalid("env" + environment, classOf[IllegalArgumentException].getName + ": ZIO environment may not be custom (requiring " + environmentType.typeSymbol.fullName + ")")

}