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
package net.officefloor.web.session;

import java.io.Serializable;
import java.util.HashMap;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpResponseBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockProcessAwareContext;
import net.officefloor.web.session.spi.CreateHttpSessionOperation;
import net.officefloor.web.session.spi.HttpSessionIdGenerator;
import net.officefloor.web.session.spi.HttpSessionStore;
import net.officefloor.web.state.HttpCookie;

/**
 * Tests that {@link HttpSessionIdGenerator} and {@link HttpSessionStore} can be
 * provided via dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionDependencyTest extends OfficeFrameTestCase {

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
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);

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
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		this.recordReturn(this.connection, this.connection.getRequest(), request.build());
		HttpSessionIdGenerator generator = (session) -> session.setSessionId("SESSION_ID");

		// Record obtaining the response
		MockHttpResponseBuilder response = MockHttpServer.mockResponse();
		this.recordReturn(this.connection, this.connection.getResponse(), response);
		this.store.createHttpSession(null);
		this.control(this.store).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CreateHttpSessionOperation operation = (CreateHttpSessionOperation) actual[0];
				operation.sessionCreated(1000, 2000, new HashMap<String, Serializable>(0));
				return true;
			}
		});

		// Create the HTTP Session Managed Object
		this.replayMockObjects();
		HttpSessionManagedObject mo = new HttpSessionManagedObject("JSESSIONID", 0, -1, generator, -1, this.store);
		mo.setProcessAwareContext(new MockProcessAwareContext());
		mo.setAsynchronousContext(this.asynchronousContext);
		mo.loadObjects(this.objectRegistry);
		this.verifyMockObjects();

		// Verify created session
		MockHttpResponse mockResponse = response.build();
		assertEquals("Should have session",
				new HttpCookie("JSESSIONID", "SESSION_ID", 2000, null, "/").toHttpResponseHeaderValue(),
				mockResponse.getHeader("set-cookie").getValue());
	}

	/**
	 * Ensure can provide via dependency.
	 */
	public void testProvidedByDependency() throws Throwable {

		// Record obtaining the HTTP request
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(0), this.connection);

		// Record attempting to creating a new session.
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		this.recordReturn(this.connection, this.connection.getRequest(), request.build());
		HttpSessionIdGenerator generator = (session) -> session.setSessionId("SESSION_ID");

		// Record obtaining as dependencies
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(1), generator);
		this.recordReturn(this.objectRegistry, this.objectRegistry.getObject(2), this.store);

		// Record obtaining the response
		MockHttpResponseBuilder response = MockHttpServer.mockResponse();
		this.recordReturn(this.connection, this.connection.getResponse(), response);
		this.store.createHttpSession(null);
		this.control(this.store).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CreateHttpSessionOperation operation = (CreateHttpSessionOperation) actual[0];
				operation.sessionCreated(1000, 2000, new HashMap<String, Serializable>(0));
				return true;
			}
		});

		// Create the HTTP Session Managed Object
		this.replayMockObjects();
		HttpSessionManagedObject mo = new HttpSessionManagedObject("JSESSIONID", 0, 1, null, 2, null);
		mo.setProcessAwareContext(new MockProcessAwareContext());
		mo.setAsynchronousContext(this.asynchronousContext);
		mo.loadObjects(this.objectRegistry);
		this.verifyMockObjects();

		// Verify created session
		MockHttpResponse mockResponse = response.build();
		assertEquals("Should have session",
				new HttpCookie("JSESSIONID", "SESSION_ID", 2000, null, "/").toHttpResponseHeaderValue(),
				mockResponse.getHeader("set-cookie").getValue());
	}

}