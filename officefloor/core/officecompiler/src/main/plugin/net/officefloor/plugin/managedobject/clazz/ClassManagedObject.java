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

package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.factory.ClassObjectFactory;

/**
 * {@link CoordinatingManagedObject} to dependency inject the {@link Object}
 * instance and make it available for use.
 * 
 * @author Daniel Sagenschneider
 * 
 */
public class ClassManagedObject implements ContextAwareManagedObject, CoordinatingManagedObject<Indexed> {

	/**
	 * {@link ClassObjectFactory}.
	 */
	private final ClassObjectFactory objectFactory;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private ManagedObjectContext context;

	/**
	 * {@link Object} being managed by reflection.
	 */
	private Object object;

	/**
	 * Instantiate.
	 * 
	 * @param objectFactory {@link ClassObjectFactory}.
	 */
	public ClassManagedObject(ClassObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	/*
	 * ================= ContextAwareManagedObject ====================
	 */

	@Override
	public void setManagedObjectContext(ManagedObjectContext context) {
		this.context = context;
	}

	/*
	 * ================= CoordinatingManagedObject ====================
	 */

	@Override
	public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {

		// Create the object
		this.object = this.objectFactory.createObject(this, this.context, registry);
	}

	@Override
	public Object getObject() {
		return this.object;
	}

}
