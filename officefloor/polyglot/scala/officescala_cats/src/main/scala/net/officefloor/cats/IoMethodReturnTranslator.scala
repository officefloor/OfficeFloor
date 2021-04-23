/*-
 * #%L
 * Cats
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

package net.officefloor.cats

import cats.effect.IO
import net.officefloor.plugin.clazz.method.{MethodReturnTranslator, MethodReturnTranslatorContext}

/**
 * {@link MethodReturnTranslator} to resolve a {@link IO} to its success/failure.
 *
 * @tparam A Success type.
 */
class IoMethodReturnTranslator[A] extends MethodReturnTranslator[IO[A], A] {

  override def translate(context: MethodReturnTranslatorContext[IO[A], A]): Unit = {

    // Obtain the runtime
    val executor = context.getManagedFunctionContext.getExecutor
    implicit val runtime = IORuntimeClassDependencyFactory.createIORuntime(executor)

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
