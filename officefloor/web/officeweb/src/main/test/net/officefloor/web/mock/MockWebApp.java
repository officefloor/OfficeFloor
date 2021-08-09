/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.mock;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.HttpSessionManagedObjectSource;
import net.officefloor.web.state.HttpApplicationState;
import net.officefloor.web.state.HttpApplicationStateManagedObjectSource;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource.HttpRequestStateDependencies;

/**
 * Mock web application.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWebApp {

	/**
	 * Mocks the {@link HttpRequestState}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection} for the {@link HttpRequestState}.
	 * @return {@link HttpRequestState}.
	 */
	public static HttpRequestState mockRequestState(ServerHttpConnection connection) {
		try {

			// Load the source
			ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
			HttpRequestStateManagedObjectSource source = loader
					.loadManagedObjectSource(new HttpRequestStateManagedObjectSource(new HttpArgumentParser[0]));

			// Source the managed object
			ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
			user.mapDependency(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION, connection);
			ManagedObject managedObject = user.sourceManagedObject(source);

			// Obtain the HttpRequestState
			return (HttpRequestState) managedObject.getObject();

		} catch (Throwable ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Mocks the {@link HttpSession}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection} for the {@link HttpSession}.
	 * @return {@link HttpSession}.
	 */
	public static HttpSession mockSession(ServerHttpConnection connection) {
		try {

			// Load the source
			ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
			HttpSessionManagedObjectSource source = loader
					.loadManagedObjectSource(new HttpSessionManagedObjectSource());

			// Source the managed object
			ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
			user.mapDependency(0, connection);
			ManagedObject managedObject = user.sourceManagedObject(source);

			// Obtain the HttpSession
			return (HttpSession) managedObject.getObject();

		} catch (Throwable ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Mocks the {@link HttpApplicationState}.
	 * 
	 * @param contextPath
	 *            Context path for the application. May be <code>null</code>.
	 * @return {@link HttpApplicationState}.
	 */
	public static HttpApplicationState mockApplicationState(String contextPath) {
		try {

			// Load the source
			ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
			HttpApplicationStateManagedObjectSource source = loader
					.loadManagedObjectSource(new HttpApplicationStateManagedObjectSource(contextPath));

			// Source the managed object
			ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
			ManagedObject managedObject = user.sourceManagedObject(source);

			// Obtain the HttpApplicationState
			return (HttpApplicationState) managedObject.getObject();

		} catch (Throwable ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * All access via static methods.
	 */
	private MockWebApp() {
	}

}
