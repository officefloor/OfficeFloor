/*-
 * #%L
 * ZIO
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.zio

import net.officefloor.plugin.clazz.method.{MethodReturnTranslator, MethodReturnTranslatorContext}
import zio.Cause.Die
import zio.Exit.{Failure, Success}
import zio.{Cause, Executor, Exit, FiberId, FiberRefs, LogLevel, LogSpan, Runtime, Trace, Unsafe, ZIO, ZLogger}

/**
 * {@link MethodReturnTranslator} to resolve a {@link ZIO} to its success/failure.
 *
 * @tparam A Success type.
 */
class ZioMethodReturnTranslator[A] extends MethodReturnTranslator[ZIO[Any, _, A], A] {

  override def translate(context: MethodReturnTranslatorContext[ZIO[Any, _, A], A]): Unit = {

    // Obtain the ZIO
    val zio = context.getReturnValue

    // Obtain the runtime details
    val managedFunctionContext = context.getManagedFunctionContext
    val executor = managedFunctionContext.getExecutor
    val logger = managedFunctionContext.getLogger

    // Create logger
    val zLogger = new ZLogger[String, Unit] {
      override def apply(
        trace: Trace,
        fiberId: FiberId,
        logLevel: LogLevel,
        message: () => String,
        cause: Cause[Any],
        context: FiberRefs,
        spans: List[LogSpan],
        annotations: Map[String, String]
      ) = {
        logLevel match {
          case LogLevel.Trace => logger.finest(message())
          case LogLevel.Debug => logger.fine(message())
          case LogLevel.Info => logger.info(message())
          case LogLevel.Warning => logger.warning(message())
          case LogLevel.Error => logger.severe(message())
          case LogLevel.Fatal => logger.severe(message())
        }
      }
    }

    // Asynchronously run effects return result
    val flow = context.getManagedFunctionContext.createAsynchronousFlow
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default
        .unsafe
        .fork(zio.provide(
          Runtime.removeDefaultLoggers ++ Runtime.addLogger(zLogger)
        ).provide(
          Runtime.setExecutor(Executor.fromJavaExecutor(executor))
        ))
        .unsafe
        .addObserver(exit =>
          flow.complete(() =>
            exit match {
              case Exit.Failure(cause) =>
                cause match {
                  case Cause.Die(ex: Throwable, _) => throw ex;
                  case Cause.Fail(ex: Throwable, _) => throw ex;
                  case cause@Cause.Fail(failure, _) => throw new ZioException(cause.prettyPrint, failure)
                  case cause@failure => throw new ZioException (cause.prettyPrint, failure)
                }
              case Exit.Success(value) =>
                context.setTranslatedReturnValue(value)
            })
         )
    }
  }

}
