/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;

/**
 * {@link CoordinatingManagedObject} to dependency inject the {@link Object}
 * instance and make it available for use.
 * 
 * @author Daniel Sagenschneider
 * 
 */
public class ClassManagedObject implements ContextAwareManagedObject, CoordinatingManagedObject<Indexed> {

	/**
	 * {@link Object} being managed by reflection.
	 */
	private final Object object;

	/**
	 * {@link DependencyMetaData} instances.
	 */
	private final DependencyMetaData[] dependencyMetaData;

	/**
	 * {@link ProcessMetaData} instances.
	 */
	private final ProcessMetaData[] processMetaData;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private ManagedObjectContext context;

	/**
	 * Initiate.
	 * 
	 * @param object             {@link Object} being managed by reflection.
	 * @param dependencyMetaData {@link DependencyMetaData} instances.
	 * @param processMetaData    {@link ProcessMetaData} instances.
	 */
	public ClassManagedObject(Object object, DependencyMetaData[] dependencyMetaData,
			ProcessMetaData[] processMetaData) {
		this.object = object;
		this.dependencyMetaData = dependencyMetaData;
		this.processMetaData = processMetaData;
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

		// Inject the dependencies
		for (int i = 0; i < this.dependencyMetaData.length; i++) {
			DependencyMetaData metaData = this.dependencyMetaData[i];

			// Load based on type
			switch (metaData.type) {
			case MANAGE_OBJECT_CONTEXT:
				// Inject the managed object context
				metaData.injectDependency(this.object, this.context);
				break;

			case LOGGER:
				// Inject the logger
				metaData.injectDependency(this.object, this.context.getLogger());
				break;

			case DEPENDENCY:
				// Obtain the dependency
				Object dependency = registry.getObject(metaData.index);

				// Inject the dependency
				metaData.injectDependency(this.object, dependency);
				break;

			default:
				throw new IllegalStateException("Unknown dependency type " + metaData.type);
			}
		}

		// Inject the process interfaces
		for (int i = 0; i < this.processMetaData.length; i++) {
			ProcessMetaData metaData = this.processMetaData[i];

			// Create the process interface implementation
			Object implementation = metaData.createProcessInterfaceImplementation(this);

			// Inject the process interface
			metaData.field.set(this.object, implementation);
		}
	}

	@Override
	public Object getObject() {
		return this.object;
	}

}