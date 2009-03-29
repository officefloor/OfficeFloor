/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} that manages an {@link Object} via reflection.
 * 
 * @author Daniel
 */
public class ClassManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, None> {

	/**
	 * {@link Class} of the {@link Object} being managed.
	 */
	private Class<?> objectClass;

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty("class");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the class name
		String className = mosContext.getProperty("class");

		// TODO load class and reflective determine the meta-data
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create an instance of the object
		Object object = this.objectClass.newInstance();

		// Return a managed object to manage the object
		return new ClassManagedObject(object);
	}

	/**
	 * {@link CoordinatingManagedObject} for dependency injecting the
	 * {@link Object}.
	 */
	private class ClassManagedObject implements
			CoordinatingManagedObject<Indexed> {

		/**
		 * {@link Object} being managed by reflection.
		 */
		private final Object object;

		/**
		 * Initiate.
		 * 
		 * @param object
		 *            {@link Object} being managed by reflection.
		 */
		public ClassManagedObject(Object object) {
			this.object = object;
		}

		/*
		 * ================= CoordinatingManagedObject ====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry)
				throws Throwable {
			// TODO load dependencies onto the object
		}

		@Override
		public Object getObject() throws Exception {
			return this.object;
		}
	}

}