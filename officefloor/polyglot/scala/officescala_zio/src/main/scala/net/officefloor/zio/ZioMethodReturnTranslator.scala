package net.officefloor.zio

import net.officefloor.plugin.managedfunction.method.MethodReturnTranslator
import zio.{DefaultRuntime, ZIO}


class ZioMethodReturnTranslator[A] extends MethodReturnTranslator[ZIO[Any, _, A], A] {

  override def translate(zio: ZIO[Any, _, A]): A =
    ZioMethodReturnTranslator.runtime.unsafeRun(zio)
}

object ZioMethodReturnTranslator {
  val runtime = new DefaultRuntime {}
}