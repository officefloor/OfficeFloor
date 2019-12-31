package net.officefloor.zio

import java.sql.SQLException

import zio._

class Procedures {

  def zioObject = ZIO.succeed(Procedures.OBJECT)

  /*
   * ========= Type Aliases ==============
   */

  def zio: ZIO[Any, Throwable, Object] = zioObject

  def uio: UIO[Object] = zioObject

  def urio: URIO[ZEnv, Object] = zioObject

  def task: Task[Object] = zioObject

  def rio: RIO[ZEnv, Object] = zioObject

  def io: IO[Throwable, Object] = zioObject

  type CustomZio = ZIO[ZEnv, Throwable, Object]
  def customZio = zioObject

  /*
   * ========= Environments ==============
   */

  // TODO continue testing with environments
}

object Procedures {
  val OBJECT = new Object()
}