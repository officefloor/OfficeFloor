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

package net.officefloor.web.session;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockManagedObjectContext;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.session.spi.CreateHttpSessionOperation;
import net.officefloor.web.session.spi.HttpSessionIdGenerator;
import net.officefloor.web.session.spi.HttpSessionStore;

/**
 * Tests that {@link HttpSessionIdGenerator} and {@link HttpSessionStore} can be
 * provided via dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionDependencyTest extends OfficeFrameTestCase {

	/**
	 * Capture current time (for deterministic test).
	 */
	private static final Instant MOCK_CURRENT_TIME = Instant.now();

	/**
	 * Expire time.
	 */
	private static final Instant EXPIRE_TIME = MOCK_CURRENT_TIME.plus(2000, ChronoUnit.MILLIS);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final MockServerHttpConnection connection = MockHttpServer.mockConnection();

	/**
	 * Mock {@link AsynchronousContext}.
	 */
	private final AsynchronousContext asynchronousContext = this.createMock(AsynchronousContext.class);

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<Indexed> objectRegistry = this.createMock(ObjectRegistry.class);

	/**
	 * Mock {@link HttpSessionStore}.
	 */
	private final HttpSessionStore store = this.createMock(HttpSessionStore.class);

	/**
	 * Ensure can provide directly to {@link HttpSessionManagedObject}.
	 */
	public void testProvidedDirectly() throws Throwable {

		// Record obtaining the HTTP request
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(0), this.connection);

		// Record attempting to creating a new session.
		HttpSessionIdGenerator generator = (session) -> session.setSessionId("SESSION_ID");

		// Record obtaining the response
		this.store.createHttpSession(this.paramType(CreateHttpSessionOperation.class));
		this.recordVoid(this.store, (arguments) -> {
			CreateHttpSessionOperation operation = (CreateHttpSessionOperation) arguments[0];
			operation.sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, new HashMap<String, Serializable>(0));
			return true;
		});

		// Create the HTTP Session Managed Object
		this.replayMockObjects();
		HttpSessionManagedObject mo = new HttpSessionManagedObject("JSESSIONID", 0, -1, generator, -1, this.store);
		mo.setManagedObjectContext(new MockManagedObjectContext());
		mo.setAsynchronousContext(this.asynchronousContext);
		mo.loadObjects(this.objectRegistry);
		this.verifyMockObjects();

		// Verify created session
		MockHttpResponse response = connection.send(null);
		response.assertCookie(MockHttpServer.mockResponseCookie("JSESSIONID", "SESSION_ID").setExpires(EXPIRE_TIME));
	}

	/**
	 * Ensure can provide via dependency.
	 */
	public void testProvidedByDependency() throws Throwable {

		// Record obtaining the HTTP request
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(0), this.connection);

		// Record attempting to creating a new session.
		HttpSessionIdGenerator generator = (session) -> session.setSessionId("SESSION_ID");

		// Record obtaining as dependencies
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(1), generator);
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(2), this.store);

		// Record obtaining the response
		this.store.createHttpSession(this.paramType(CreateHttpSessionOperation.class));
		this.recordVoid(this.store, (arguments) -> {
			CreateHttpSessionOperation operation = (CreateHttpSessionOperation) arguments[0];
			operation.sessionCreated(MOCK_CURRENT_TIME, EXPIRE_TIME, new HashMap<String, Serializable>(0));
			return true;
		});

		// Create the HTTP Session Managed Object
		this.replayMockObjects();
		HttpSessionManagedObject mo = new HttpSessionManagedObject("JSESSIONID", 0, 1, null, 2, null);
		mo.setManagedObjectContext(new MockManagedObjectContext());
		mo.setAsynchronousContext(this.asynchronousContext);
		mo.loadObjects(this.objectRegistry);
		this.verifyMockObjects();

		// Verify created session
		MockHttpResponse response = this.connection.send(null);
		response.assertCookie(MockHttpServer.mockResponseCookie("JSESSIONID", "SESSION_ID").setExpires(EXPIRE_TIME));
	}

}
