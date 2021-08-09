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

import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.clazz.dependency.{ClassDependencyFactory, ClassDependencyManufacturer, ClassDependencyManufacturerContext, ClassDependencyManufacturerServiceFactory}

import scala.concurrent.ExecutionContext

/**
 * {@link MethodParameterManufacturerServiceFactory} for a {@link ExecutionContext}.
 */
class ExecutionContextClassDependencyManufacturerServiceFactory extends ClassDependencyManufacturerServiceFactory with ClassDependencyManufacturer {

  /*
   * ================== ClassDependencyManufacturerServiceFactory ==================
   */

  override def createService(serviceContext: ServiceContext): ClassDependencyManufacturer = this

  /*
   * ========================= ClassDependencyManufacturer =========================
   */

  override def createParameterFactory(context: ClassDependencyManufacturerContext): ClassDependencyFactory =
    if (classOf[ExecutionContext].equals(context.getDependencyClass)) new ExecutionContextClassDependencyFactory() else null

}
