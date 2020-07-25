/*-
 * #%L
 * Scala
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
