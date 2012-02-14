/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.session.object.source;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.plugin.web.http.session.object.HttpSessionObject;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectManagedObjectSource;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectRetrieverManagedObject;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectRetrieverManagedObjectSource;
import net.officefloor.plugin.web.http.session.object.source.HttpSessionObjectRetrieverManagedObjectSource.HttpSessionObjectRetrieverDependencies;

/**
 * Tests the {@link HttpSessionObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectRetrieverManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						HttpSessionObjectRetrieverManagedObjectSource.class,
						HttpSessionObjectRetrieverManagedObjectSource.PROPERTY_TYPE_NAME,
						HttpSessionObjectRetrieverManagedObjectSource.PROPERTY_TYPE_NAME);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockType.class);
		type.addDependency(
				HttpSessionObjectRetrieverDependencies.HTTP_SESSION_OBJECT
						.name(), HttpSessionObject.class, null,
				HttpSessionObjectRetrieverDependencies.HTTP_SESSION_OBJECT
						.ordinal(),
				HttpSessionObjectRetrieverDependencies.HTTP_SESSION_OBJECT);

		// Validate the managed object type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSessionObjectRetrieverManagedObjectSource.class,
						HttpSessionObjectRetrieverManagedObjectSource.PROPERTY_TYPE_NAME,
						MockType.class.getName());
	}

	/**
	 * Ensure can load the {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testLoad() throws Throwable {

		final MockType SESSION_OBJECT = new MockType() {
		};

		ObjectRegistry<HttpSessionObjectRetrieverDependencies> objectRegistry = this
				.createMock(ObjectRegistry.class);
		HttpSessionObject<?> sessionObject = this
				.createMock(HttpSessionObject.class);

		// Record
		this.recordReturn(
				objectRegistry,
				objectRegistry
						.getObject(HttpSessionObjectRetrieverDependencies.HTTP_SESSION_OBJECT),
				sessionObject);
		this.recordReturn(sessionObject, sessionObject.getSessionObject(),
				SESSION_OBJECT);

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				HttpSessionObjectRetrieverManagedObjectSource.PROPERTY_TYPE_NAME,
				MockType.class.getName());
		HttpSessionObjectRetrieverManagedObjectSource mos = loader
				.loadManagedObjectSource(HttpSessionObjectRetrieverManagedObjectSource.class);

		// Obtain the managed object (ensure correct)
		ManagedObject mo = mos.getManagedObject();
		assertTrue("Incorrect managed object type",
				(mo instanceof HttpSessionObjectRetrieverManagedObject));

		// Obtain the object (to ensure correct class)
		HttpSessionObjectRetrieverManagedObject sorMo = (HttpSessionObjectRetrieverManagedObject) mo;
		sorMo.loadObjects(objectRegistry);
		Object object = mo.getObject();
		assertEquals("Incorrect object", SESSION_OBJECT, object);

		this.verifyMockObjects();
	}

	/**
	 * Mock type for testing.
	 */
	public interface MockType {
	}

}