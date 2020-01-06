/*-
 * #%L
 * ZIO
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.zio

import net.officefloor.plugin.managedfunction.method.{MethodReturnTranslator, MethodReturnTranslatorContext}
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
