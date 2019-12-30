package net.officefloor.zio

import net.officefloor.plugin.managedfunction.method.{MethodReturnTranslator, MethodReturnTranslatorContext}
import zio.Exit.{Failure, Success}
import zio.{FiberFailure, ZIO}

/**
 * ZIO {@link MethodReturnTranslator} to resolve the {@link ZIO} to its result.
 *
 * @tparam A Result type.
 */
class ZioMethodReturnTranslator[A] extends MethodReturnTranslator[ZIO[Any, _, A], A] {

  override def translate(context: MethodReturnTranslatorContext[ZIO[Any, _, A], A]): Unit = {

    // Obtain the ZIO
    val zio = context.getReturnValue

    // Start asynchronous flow
    val flow = context.getManagedFunctionContext.createAsynchronousFlow

    // Asynchronously run effects
    OfficeFloorZio.defaultRuntime.unsafeRunAsync(zio) { exit =>
      flow.complete { () =>
        exit match {
          case Success(value) => context.setTranslatedReturnValue(value)
          case Failure(cause) => throw FiberFailure(cause)
        }
      }
    }
  }

}