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
import zio.Exit.{Failure, Success}
import zio.ZIO

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

    // Asynchronously run effects return result
    val flow = context.getManagedFunctionContext.createAsynchronousFlow
    OfficeFloorZio.runtime(executor, logger).unsafeRunAsync(zio) { exit =>
      flow.complete { () =>
        exit match {
          case Success(value) => context.setTranslatedReturnValue(value)
          case Failure(cause) => {
            val failure = cause.failureOption match {
              case None => cause.dieOption
              case some => some
            }
            failure match {
              case Some(ex: Throwable) => throw ex;
              case Some(failure) => throw new ZioException(cause.prettyPrint, failure)
              case failure => throw new ZioException(cause.prettyPrint, failure)
            }
          }
        }
      }
    }
  }

}
