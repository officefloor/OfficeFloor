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
package net.officefloor.web.parameter;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Tests the {@link HttpParametersLoaderManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(HttpParametersLoaderManagedObjectSource.class,
				HttpParametersLoaderManagedObjectSource.PROPERTY_TYPE_NAME,
				HttpParametersLoaderManagedObjectSource.PROPERTY_TYPE_NAME);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		ManagedObjectTypeBuilder mo = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		mo.setObjectClass(MockType.class);
		mo.addDependency(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);
		mo.addDependency(HttpParametersLoaderDependencies.OBJECT, MockType.class, null);
		ManagedObjectLoaderUtil.validateManagedObjectType(mo, HttpParametersLoaderManagedObjectSource.class,
				HttpParametersLoaderManagedObjectSource.PROPERTY_TYPE_NAME, MockType.class.getName());
	}

	/**
	 * Validates using to load parameters onto an Object.
	 */
	public void testLoad() throws Throwable {

		// Record loading the object
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		HttpRequest request = MockHttpServer.mockRequest("/path?VALUE=value").build();
		this.recordReturn(connection, connection.getHttpRequest(), request);
		MockType mockType = this.createMock(MockType.class);
		mockType.setValue("value");

		// Test
		this.replayMockObjects();
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(HttpParametersLoaderManagedObjectSource.PROPERTY_TYPE_NAME, MockType.class.getName());
		HttpParametersLoaderManagedObjectSource mos = loader
				.loadManagedObjectSource(HttpParametersLoaderManagedObjectSource.class);
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION, connection);
		user.mapDependency(HttpParametersLoaderDependencies.OBJECT, mockType);
		ManagedObject mo = user.sourceManagedObject(mos);
		Object object = mo.getObject();
		assertEquals("Incorrect object (should provide dependency)", mockType, object);
		this.verifyMockObjects();
	}

	/**
	 * Type for testing.
	 */
	public static interface MockType {

		void setValue(String value);
	}

}