package net.officefloor.scala

import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.managedfunction.method.{MethodParameterFactory, MethodParameterManufacturer, MethodParameterManufacturerContext, MethodParameterManufacturerServiceFactory}

import scala.concurrent.ExecutionContext

/**
 * {@link MethodParameterManufacturerServiceFactory} for a {@link ExecutionContext}.
 */
class ExecutionContextMethodParameterManufacturerServiceFactory extends MethodParameterManufacturerServiceFactory with MethodParameterManufacturer {

  /*
   * ================== MethodParameterManufacturerServiceFactory ==================
   */

  override def createService(serviceContext: ServiceContext): MethodParameterManufacturer = this

  /*
   * ========================= MethodParameterManufacturer =========================
   */

  override def createParameterFactory(context: MethodParameterManufacturerContext): MethodParameterFactory =
    if (classOf[ExecutionContext].equals(context.getParameterClass)) new ExecutionContextMethodParameterFactory() else null


}