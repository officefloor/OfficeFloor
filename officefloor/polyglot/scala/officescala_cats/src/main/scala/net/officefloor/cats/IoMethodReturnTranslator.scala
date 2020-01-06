package net.officefloor.cats

import cats.effect.IO
import net.officefloor.plugin.managedfunction.method.{MethodReturnTranslator, MethodReturnTranslatorContext}

/**
 * {@link MethodReturnTranslator} to resolve a {@link IO} to its success/failure.
 *
 * @tparam A Success type.
 */
class IoMethodReturnTranslator[A] extends MethodReturnTranslator[IO[A], A] {

  override def translate(context: MethodReturnTranslatorContext[IO[A], A]): Unit = {

    // Obtain the IO
    val io = context.getReturnValue

    // Asynchronously run effects return result
    val flow = context.getManagedFunctionContext.createAsynchronousFlow
    io.unsafeRunAsync(exit => flow.complete { () =>
      exit match {
        case Right(value) => context.setTranslatedReturnValue(value)
        case Left(cause) => throw cause
      }
    })
  }

}