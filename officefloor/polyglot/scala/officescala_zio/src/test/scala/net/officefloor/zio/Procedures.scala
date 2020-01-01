package net.officefloor.zio

import java.sql.SQLException

import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.system.System

class Procedures {

  def zioObject = ZIO.succeed(Procedures.OBJECT)

  /*
   * ========= Type Aliases ==============
   */

  def typeZIO: ZIO[Any, Throwable, Object] = zioObject

  def typeUIO: UIO[Object] = zioObject

  def typeURIO: URIO[ZEnv, Object] = zioObject

  def typeTask: Task[Object] = zioObject

  def typeRIO: RIO[ZEnv, Object] = zioObject

  def typeIO: IO[Throwable, Object] = zioObject

  type CustomZio = ZIO[ZEnv, Throwable, Object]
  def typeCustomZio: CustomZio = zioObject

  /*
   * ========= Environments ==============
   */

  type Env[R] = ZIO[R, Throwable, Object]

  def envZEnv: Env[ZEnv] = zioObject

  def envClock: Env[Clock] = zioObject

  def envConsole: Env[Console] = zioObject

  def envSystem: Env[System] = zioObject

  def envRandom: Env[Random] = zioObject

  def envBlocking: Env[Blocking] = zioObject

  def envAny: Env[Any] = zioObject

  def envAnyRef: Env[Any] = zioObject

  def envNothing: Env[Nothing] = zioObject

  def envObject: Env[Object] = zioObject

  def envAnyVal: Env[AnyVal] = zioObject

  def envInt: Env[Int] = zioObject

  def envString: Env[String] = zioObject

}

object Procedures {
  val OBJECT = new Object()
}