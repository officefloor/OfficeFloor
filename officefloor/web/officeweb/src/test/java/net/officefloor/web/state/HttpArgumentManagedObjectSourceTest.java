/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.state;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.state.HttpArgumentManagedObjectSource.HttpArgumentDependencies;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource.HttpRequestStateDependencies;

/**
 * Tests the {@link HttpArgumentManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpArgumentManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(new HttpArgumentManagedObjectSource("test", null));
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(String.class);
		type.addDependency(HttpArgumentDependencies.HTTP_REQUEST_STATE, HttpRequestState.class, null);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, new HttpArgumentManagedObjectSource("test", null));
	}

	/**
	 * Match any {@link HttpValueLocation}.
	 */
	public void testMatchAnyLocation() throws Throwable {
		this.doLoadValueTest(null, "value");
	}

	/**
	 * Match specific {@link HttpValueLocation}.
	 */
	public void testMatchSpecificLocation() throws Throwable {
		this.doLoadValueTest(HttpValueLocation.PATH, "value");
	}

	/**
	 * Ensure not match if not {@link HttpValueLocation} match.
	 */
	public void testNotMatchByLocation() throws Throwable {
		this.doLoadValueTest(HttpValueLocation.QUERY, null);
	}

	/**
	 * Undertakes loading the value.
	 * 
	 * @param matchValueLocation
	 *            {@link HttpValueLocation} to match on.
	 * @param expectedValue
	 *            Expected value.
	 */
	public void doLoadValueTest(HttpValueLocation matchValueLocation, String expectedValue) throws Throwable {

		// Create the server HTTP connection
		ServerHttpConnection connection = MockHttpServer.mockConnection(MockHttpServer.mockRequest("/"));

		// Create the request state
		HttpRequestState requestState = this.createHttpRequestState(connection);
		HttpRequestStateManagedObjectSource
				.initialiseHttpRequestState(new HttpArgument("param", "value", HttpValueLocation.PATH), requestState);

		// Load the managed object source
		HttpArgumentManagedObjectSource source = new HttpArgumentManagedObjectSource("param", matchValueLocation);

		// Instantiate and obtain the object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpArgumentDependencies.HTTP_REQUEST_STATE, requestState);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the value
		String value = (String) managedObject.getObject();
		assertEquals("Incorrect value", expectedValue, value);
	}

	/**
	 * Creates the {@link HttpRequestState}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return {@link HttpRequestState}.
	 */
	private HttpRequestState createHttpRequestState(ServerHttpConnection connection) throws Throwable {
		HttpRequestStateManagedObjectSource mos = new HttpRequestStateManagedObjectSource(new HttpArgumentParser[0]);
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION, connection);
		return (HttpRequestState) user.sourceManagedObject(mos);
	}

}
