/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;

/**
 * {@link ClassDependencyManufacturer} for the {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionContextClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory, ClassDependencyFactory {

	/*
	 * =========== ClassDependencyManufacturerServiceFactory ===============
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== ClassDependencyManufacturer =====================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Determine if managed function context
		if (!ManagedFunctionContext.class.equals(context.getDependencyClass())) {
			return null; // not context
		}

		// Provide context
		return this;
	}

	/*
	 * ====================== ClassDependencyFactory =============================
	 */

	@Override
	public Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		return context;
	}

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {
		throw new IllegalStateException("Should not use " + ManagedFunctionContext.class.getSimpleName() + " for "
				+ ManagedObject.class.getSimpleName());
	}

	@Override
	public Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {
		throw new IllegalStateException("Should not use " + ManagedFunctionContext.class.getSimpleName() + " for "
				+ Administration.class.getSimpleName());
	}

}
