/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.jndi.object;

import javax.naming.Context;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * <p>
 * {@link ManagedObjectSource} that provides an Object via a JNDI lookup.
 * <p>
 * This allows use of JNDI Objects such as a {@link javax.sql.DataSource} or
 * {@link javax.jms.Queue}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiObjectManagedObjectSource
		extends
		AbstractManagedObjectSource<JndiObjectManagedObjectSource.JndiObjectDependency, None> {

	/**
	 * Name of property providing the JNDI name of the Object to return.
	 */
	public static final String PROPERTY_JNDI_NAME = "jndi.name";

	/**
	 * Name of property providing the type of Object being returned.
	 */
	public static final String PROPERTY_OBJECT_TYPE = "object.type";

	/**
	 * Dependencies.
	 */
	public static enum JndiObjectDependency {
		CONTEXT
	}

	/**
	 * JNDI name.
	 */
	private String jndiName;

	/*
	 * ====================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_JNDI_NAME, "JNDI Name");
		context.addProperty(PROPERTY_OBJECT_TYPE, "Object Type");
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<JndiObjectDependency, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the JNDI name
		this.jndiName = mosContext.getProperty(PROPERTY_JNDI_NAME);

		// Obtain the Object Type
		String objectTypeName = mosContext.getProperty(PROPERTY_OBJECT_TYPE);
		Class<?> objectType = mosContext.loadClass(objectTypeName);

		// Load the meta-data
		context.setManagedObjectClass(JndiObjectManagedObject.class);
		context.setObjectClass(objectType);

		// Specify depends on Context
		context.addDependency(JndiObjectDependency.CONTEXT, Context.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JndiObjectManagedObject();
	}

	/**
	 * {@link ManagedObject} to return the JNDI Object.
	 */
	private class JndiObjectManagedObject implements
			CoordinatingManagedObject<JndiObjectDependency> {

		/**
		 * {@link Context} to obtain the Object from.
		 */
		private Context context;

		/*
		 * =================== CoordinatingManagedObject ==================
		 */

		@Override
		public void loadObjects(ObjectRegistry<JndiObjectDependency> registry)
				throws Throwable {
			// Obtain the Context
			this.context = (Context) registry
					.getObject(JndiObjectDependency.CONTEXT);
		}

		@Override
		public Object getObject() throws Throwable {

			// Obtain the Object from JNDI Context
			Object object = this.context
					.lookup(JndiObjectManagedObjectSource.this.jndiName);

			// Return the Object
			return object;
		}
	}

}