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
import javax.sql.DataSource;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.jndi.object.JndiObjectManagedObjectSource.JndiObjectDependency;

/**
 * Tests the {@link JndiObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiObjectManagedObjectSourceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(JndiObjectManagedObjectSource.class,
				JndiObjectManagedObjectSource.PROPERTY_JNDI_NAME, "JNDI Name",
				JndiObjectManagedObjectSource.PROPERTY_OBJECT_TYPE, "Object Type");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(DataSource.class);
		type.addDependency(JndiObjectDependency.CONTEXT, Context.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, JndiObjectManagedObjectSource.class,
				JndiObjectManagedObjectSource.PROPERTY_JNDI_NAME, "java:comp/env/jdbc/DataSourceName",
				JndiObjectManagedObjectSource.PROPERTY_OBJECT_TYPE, DataSource.class.getName());
	}

	/**
	 * Tests sourcing an Object from JNDI.
	 */
	public void testSourcingObjectFromJndi() throws Throwable {

		// Objects for testing
		final Context context = this.createMock(Context.class);
		final String jndiName = "java:/comp/env/jdbc/DataSourceName";
		final DataSource object = this.createMock(DataSource.class);

		// Record obtaining the Object from JNDI
		this.recordReturn(context, context.lookup(jndiName), object);

		// Test
		this.replayMockObjects();

		// Obtain the JNDI Context Managed Object Source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(JndiObjectManagedObjectSource.PROPERTY_JNDI_NAME, jndiName);
		loader.addProperty(JndiObjectManagedObjectSource.PROPERTY_OBJECT_TYPE, DataSource.class.getName());
		JndiObjectManagedObjectSource mos = loader.loadManagedObjectSource(JndiObjectManagedObjectSource.class);

		// Obtain the Managed Object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(JndiObjectDependency.CONTEXT, context);
		ManagedObject mo = user.sourceManagedObject(mos);

		// Obtain the Object
		Object retrievedObject = mo.getObject();

		// Verify functionality
		this.verifyMockObjects();

		// Ensure correct retrieved Object
		assertEquals("Incorrect retrieved Object", object, retrievedObject);
	}

}