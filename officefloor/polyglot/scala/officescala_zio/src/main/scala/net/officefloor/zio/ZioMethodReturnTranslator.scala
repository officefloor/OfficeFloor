package net.officefloor.zio

import net.officefloor.frame.api.function.ManagedFunctionContext
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslator
import zio.Exit.{Failure, Success}
import zio.{FiberFailure, ZIO}

/**
 * ZIO {@link MethodReturnTranslator} to resolve the {@link ZIO} to its result.
 *
 * @tparam A Result type.
 */
class ZioMethodReturnTranslator[A] extends MethodReturnTranslator[ZIO[Any, _, A], A] {

  override def translate(zio: ZIO[Any, _, A], context: ManagedFunctionContext[_, _]): A = {

    // Start asynchronous flow
    val flow = context.createAsynchronousFlow()

    // Asynchronously run effects
    OfficeFloorZio.defaultRuntime.unsafeRunAsync(zio) { exit =>
      flow.complete { () =>
        exit match {
          case Success(value) => context.setNextFunctionArgument(value)
          case Failure(cause) => throw FiberFailure(cause)
        }
      }
    }

    // Nothing synchronously to return
    1.asInstanceOf[A]
  }

}