package net.officefloor.zio

import net.officefloor.plugin.managedfunction.method.{MethodReturnTranslator, MethodReturnTranslatorContext}
import zio.Exit.{Failure, Success}
import zio.ZIO

/**
 * ZIO {@link MethodReturnTranslator} to resolve the {@link ZIO} to its result.
 *
 * @tparam A Result type.
 */
class ZioMethodReturnTranslator[A] extends MethodReturnTranslator[ZIO[Any, _, A], A] {

  override def translate(context: MethodReturnTranslatorContext[ZIO[Any, _, A], A]): Unit = {

    // Obtain the ZIO
    val zio = context.getReturnValue

    // Obtain the runtime details
    val managedFunctionContext = context.getManagedFunctionContext
    val executor = managedFunctionContext.getExecutor
    val logger = managedFunctionContext.getLogger

    // Asynchronously run effects return result
    val flow = context.getManagedFunctionContext.createAsynchronousFlow
    OfficeFloorZio.runtime(executor, logger).unsafeRunAsync(zio) { exit =>
      flow.complete { () =>
        exit match {
          case Success(value) => context.setTranslatedReturnValue(value)
          case Failure(cause) => cause.failureOption match {
            case Some(ex: Throwable) => throw ex;
            case Some(failure) => throw new ZioException(cause.prettyPrint, failure)
            case failure => throw new ZioException(cause.prettyPrint, failure)
          }
        }
      }
    }
  }

}