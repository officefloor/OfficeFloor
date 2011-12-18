/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.parameters.source;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderDependencies;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersObjectManagedObjectSource;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersObjectManagedObjectSource.Dependencies;

/**
 * Tests the {@link HttpParametersObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersObjectManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				HttpParametersObjectManagedObjectSource.class,
				HttpParametersObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				"Class");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		ManagedObjectTypeBuilder mo = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		mo.setObjectClass(MockClass.class);
		mo.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class, null);
		ManagedObjectLoaderUtil.validateManagedObjectType(mo,
				HttpParametersObjectManagedObjectSource.class,
				HttpParametersObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockClass.class.getName());
	}

	/**
	 * Validates using to load parameters onto an Object.
	 */
	public void testLoad() throws Throwable {

		// Record loading the object
		ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		HttpRequest request = new HttpRequestImpl("GET", "/path?VALUE=value",
				"HTTP/1.1", null, null);
		this.recordReturn(connection, connection.getHttpRequest(), request);

		// Test
		this.replayMockObjects();
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				HttpParametersObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockClass.class.getName());
		HttpParametersObjectManagedObjectSource mos = loader
				.loadManagedObjectSource(HttpParametersObjectManagedObjectSource.class);
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(
				HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION,
				connection);
		ManagedObject mo = user.sourceManagedObject(mos);
		MockClass object = (MockClass) mo.getObject();
		assertEquals("Value should be loaded on object", "value", object.value);
		this.verifyMockObjects();
	}

	/**
	 * Class for testing.
	 */
	public static class MockClass {

		public String value = null;

		public void setValue(String value) {
			this.value = value;
		}
	}

}