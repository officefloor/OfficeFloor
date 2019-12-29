package net.officefloor.zio

import java.sql.SQLException

import zio._

class Procedure {

  def zioReturn(): ZIO[Any, NullPointerException, Int] = ZIO.succeed(42)

  def uioReturn(): UIO[String] = ZIO.succeed("TEST")

  def urioReturn(): URIO[Int, String] = ZIO.fromFunction((i: Int) => String.valueOf(i))

  def taskReturn(): Task[Long] = ZIO.succeed(10)

  def rioReturn(): RIO[Int, String] = ZIO.fromFunction((i: Int) => String.valueOf(i))

  def ioReturn(): IO[SQLException, Short] = ZIO.succeed(42)

  def zioUnitReturn(): ZIO[Unit, Unit, Unit] = ZIO.succeed(())

  def intReturn(): Int = 1

  def unitReturn(): Unit = ()
}
