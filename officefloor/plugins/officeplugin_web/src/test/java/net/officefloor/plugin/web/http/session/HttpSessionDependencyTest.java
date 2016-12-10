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
package net.officefloor.plugin.web.http.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.cookie.HttpCookie;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObject;
import net.officefloor.plugin.web.http.session.spi.CreateHttpSessionOperation;
import net.officefloor.plugin.web.http.session.spi.FreshHttpSession;
import net.officefloor.plugin.web.http.session.spi.HttpSessionIdGenerator;
import net.officefloor.plugin.web.http.session.spi.HttpSessionStore;

import org.easymock.AbstractMatcher;

/**
 * Tests that {@link HttpSessionIdGenerator} and {@link HttpSessionStore} can be
 * provided via dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionDependencyTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link AsynchronousListener}.
	 */
	private final AsynchronousListener asynchronousListener = this
			.createMock(AsynchronousListener.class);

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<Indexed> objectRegistry = this
			.createMock(ObjectRegistry.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private final HttpRequest httpRequest = this.createMock(HttpRequest.class);

	/**
	 * Mock {@link HttpResponse}.
	 */
	private final HttpResponse httpResponse = this
			.createMock(HttpResponse.class);

	/**
	 * Mock {@link HttpSessionIdGenerator}.
	 */
	private final HttpSessionIdGenerator generator = this
			.createMock(HttpSessionIdGenerator.class);

	/**
	 * Mock {@link HttpSessionStore}.
	 */
	private final HttpSessionStore store = this
			.createMock(HttpSessionStore.class);

	/**
	 * Ensure can provide directly to {@link HttpSessionManagedObject}.
	 */
	public void testProvidedDirectly() throws Throwable {

		// Record obtaining the HTTP request
		this.recordReturn(this.objectRegistry,
				this.objectRegistry.getObject(0), this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.httpRequest);

		// Record attempting to creating a new session.
		// (Also ensures directly using the generator and store)
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(),
				new ArrayList<HttpHeader>(0));
		this.generator.generateSessionId(null);
		this.control(this.generator).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				FreshHttpSession session = (FreshHttpSession) actual[0];
				session.setSessionId("SESSION_ID");
				return true;
			}
		});
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.httpResponse);
		this.recordReturn(this.httpResponse, this.httpResponse.getHeaders(),
				new HttpHeader[0]);
		this.recordReturn(this.httpResponse, this.httpResponse.addHeader(
				"set-cookie", new HttpCookie("JSESSIONID", "SESSION_ID", 2000,
						null, "/").toHttpResponseHeaderValue()), this
				.createMock(HttpHeader.class));
		this.store.createHttpSession(null);
		this.control(this.store).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CreateHttpSessionOperation operation = (CreateHttpSessionOperation) actual[0];
				operation.sessionCreated(1000, 2000,
						new HashMap<String, Serializable>(0));
				return true;
			}
		});

		// Create the HTTP Session Managed Object
		this.replayMockObjects();
		HttpSessionManagedObject mo = new HttpSessionManagedObject(
				"JSESSIONID", 0, -1, this.generator, -1, this.store);
		mo.registerAsynchronousCompletionListener(this.asynchronousListener);
		mo.loadObjects(this.objectRegistry);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can provide via dependency.
	 */
	public void testProvidedByDependency() throws Throwable {

		// Record obtaining the HTTP request
		this.recordReturn(this.objectRegistry,
				this.objectRegistry.getObject(0), this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.httpRequest);

		// Record obtaining as dependencies
		this.recordReturn(this.objectRegistry,
				this.objectRegistry.getObject(1), this.generator);
		this.recordReturn(this.objectRegistry,
				this.objectRegistry.getObject(2), this.store);

		// Record attempting to creating a new session.
		// (Also ensures using the dependency generator and store)
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(),
				new ArrayList<HttpHeader>(0));
		this.generator.generateSessionId(null);
		this.control(this.generator).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				FreshHttpSession session = (FreshHttpSession) actual[0];
				session.setSessionId("SESSION_ID");
				return true;
			}
		});
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.httpResponse);
		this.recordReturn(this.httpResponse, this.httpResponse.getHeaders(),
				new HttpHeader[0]);
		this.recordReturn(this.httpResponse, this.httpResponse.addHeader(
				"set-cookie", new HttpCookie("JSESSIONID", "SESSION_ID", 2000,
						null, "/").toHttpResponseHeaderValue()), this
				.createMock(HttpHeader.class));
		this.store.createHttpSession(null);
		this.control(this.store).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CreateHttpSessionOperation operation = (CreateHttpSessionOperation) actual[0];
				operation.sessionCreated(1000, 2000,
						new HashMap<String, Serializable>(0));
				return true;
			}
		});

		// Create the HTTP Session Managed Object
		this.replayMockObjects();
		HttpSessionManagedObject mo = new HttpSessionManagedObject(
				"JSESSIONID", 0, 1, null, 2, null);
		mo.registerAsynchronousCompletionListener(this.asynchronousListener);
		mo.loadObjects(this.objectRegistry);
		this.verifyMockObjects();
	}

}