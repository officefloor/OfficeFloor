/*-
 * #%L
 * Cats
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
