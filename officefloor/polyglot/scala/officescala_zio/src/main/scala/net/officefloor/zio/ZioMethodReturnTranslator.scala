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

  override def translate(zio: ZIO[Any, _, A], context: ManagedFunctionContext[_, _]): A =
    OfficeFloorZio.defaultRuntime.unsafeRunAsync(zio) { _ match {
        case Success(value) => value
        case Failure(cause) => throw FiberFailure(cause)
      }
    }