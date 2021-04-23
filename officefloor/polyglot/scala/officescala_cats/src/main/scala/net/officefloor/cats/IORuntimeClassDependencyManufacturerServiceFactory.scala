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

import cats.effect.unsafe.IORuntime
import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.clazz.dependency.{ClassDependencyFactory, ClassDependencyManufacturer, ClassDependencyManufacturerContext, ClassDependencyManufacturerServiceFactory}

/**
 * {@link ClassDependencyManufacturerServiceFactory} for a {@link IORuntime}.
 */
class IORuntimeClassDependencyManufacturerServiceFactory extends ClassDependencyManufacturerServiceFactory with ClassDependencyManufacturer {

  /*
   * ================== ClassDependencyManufacturerServiceFactory ==================
   */

  override def createService(serviceContext: ServiceContext): ClassDependencyManufacturer = this

  /*
   * ========================= ClassDependencyManufacturer =========================
   */

  override def createParameterFactory(context: ClassDependencyManufacturerContext): ClassDependencyFactory =
    if (classOf[IORuntime].equals(context.getDependencyClass)) new IORuntimeClassDependencyFactory() else null


}
