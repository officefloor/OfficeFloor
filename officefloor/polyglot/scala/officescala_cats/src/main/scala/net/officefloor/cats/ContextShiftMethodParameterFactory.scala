package net.officefloor.cats

import cats.effect.{ContextShift, IO}
import net.officefloor.frame.api.function.ManagedFunctionContext
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory

import scala.concurrent.ExecutionContext

/**
 * {@link MethodParameterFactory} for {@link ContextShift}.
 */
class ContextShiftMethodParameterFactory extends MethodParameterFactory {

  /*
   * ======================== MethodParameterFactory =========================
   */

  override def createParameter(context: ManagedFunctionContext[_, _]): ContextShift[IO] =
    IO.contextShift(ExecutionContext.fromExecutor(context.getExecutor))
}
