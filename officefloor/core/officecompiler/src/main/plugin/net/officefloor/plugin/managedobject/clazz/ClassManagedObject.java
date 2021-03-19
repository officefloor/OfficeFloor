/*-
 * #%L
 * OfficeCompiler
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
