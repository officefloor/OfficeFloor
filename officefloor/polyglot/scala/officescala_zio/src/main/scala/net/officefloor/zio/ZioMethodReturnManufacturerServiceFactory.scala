package net.officefloor.zio

import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.managedfunction.method.{MethodReturnManufacturer, MethodReturnManufacturerContext, MethodReturnManufacturerServiceFactory, MethodReturnTranslator}
import zio.ZIO

class ZioMethodReturnManufacturerServiceFactory[A] extends MethodReturnManufacturerServiceFactory with MethodReturnManufacturer[ZIO[Any, _, A], A] {

  override def createService(serviceContext: ServiceContext): MethodReturnManufacturer[ZIO[Any, _, A], A] = this

  override def createReturnTranslator(context: MethodReturnManufacturerContext[A]): MethodReturnTranslator[ZIO[Any, _, A], A] = {

    // Determine if ZIO return
    if (classOf[ZIO[_, _, _]].isAssignableFrom(context.getReturnClass)) {
      context.setTranslatedReturnClass(classOf[Int].asInstanceOf[Class[A]])
      return new ZioMethodReturnTranslator[A]()
    }

    // Not ZIO return
    null
  }
}
