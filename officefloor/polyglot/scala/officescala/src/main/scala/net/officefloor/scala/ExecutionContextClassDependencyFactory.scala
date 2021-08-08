/*-
 * #%L
 * Scala
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

package net.officefloor.scala

import java.util.concurrent.Executor

import net.officefloor.frame.api.administration.AdministrationContext
import net.officefloor.frame.api.build.Indexed
import net.officefloor.frame.api.function.ManagedFunctionContext
import net.officefloor.frame.api.managedobject.{ManagedObject, ManagedObjectContext, ObjectRegistry}
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory

import scala.concurrent.ExecutionContext

/**
 * {@link MethodParameterFactory} for {@link ExecutionContext}.
 */
class ExecutionContextClassDependencyFactory extends ClassDependencyFactory {

  /*
   * ======================== ClassDependencyFactory =========================
   */

  override def createDependency(managedObject: ManagedObject, context: ManagedObjectContext, objectRegistry: ObjectRegistry[Indexed]): AnyRef =
    throw new IllegalStateException(s"Can not obtain ${classOf[Executor].getSimpleName} for ${classOf[ManagedObject].getSimpleName}")

  override def createDependency(context: ManagedFunctionContext[Indexed, Indexed]): AnyRef = ExecutionContext.fromExecutor(context.getExecutor)

  override def createDependency(context: AdministrationContext[AnyRef, Indexed, Indexed]): AnyRef = ExecutionContext.fromExecutor(context.getExecutor)
}
