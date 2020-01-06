package net.officefloor.scala

import net.officefloor.frame.api.function.ManagedFunctionContext
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory

import scala.concurrent.ExecutionContext

/**
 * {@link MethodParameterFactory} for {@link ExecutionContext}.
 */
class ExecutionContextMethodParameterFactory extends MethodParameterFactory {

  /*
   * ======================== MethodParameterFactory =========================
   */

  override def createParameter(context: ManagedFunctionContext[_, _]): ExecutionContext =
    ExecutionContext.fromExecutor(context.getExecutor)

}