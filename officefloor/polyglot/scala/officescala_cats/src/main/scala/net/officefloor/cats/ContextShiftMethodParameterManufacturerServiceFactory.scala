package net.officefloor.cats

import cats.effect.{ContextShift, IO}
import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.managedfunction.method.{MethodParameterFactory, MethodParameterManufacturer, MethodParameterManufacturerContext, MethodParameterManufacturerServiceFactory}

/**
 * {@link MethodParameterManufacturerServiceFactory} for a {@link ContextShift}.
 */
class ContextShiftMethodParameterManufacturerServiceFactory extends MethodParameterManufacturerServiceFactory with MethodParameterManufacturer {

  /*
   * ================== MethodParameterManufacturerServiceFactory ==================
   */

  override def createService(serviceContext: ServiceContext): MethodParameterManufacturer = this

  /*
   * ========================= MethodParameterManufacturer =========================
   */

  override def createParameterFactory(context: MethodParameterManufacturerContext): MethodParameterFactory =
    if (context.getParameterClass.isAssignableFrom(classOf[ContextShift[IO]])) new ContextShiftMethodParameterFactory() else null


}