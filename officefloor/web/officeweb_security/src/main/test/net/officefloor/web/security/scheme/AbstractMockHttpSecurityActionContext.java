/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.scheme;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.security.impl.AuthenticationContextManagedObjectSource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpSecurityActionContext;
import net.officefloor.web.spi.security.HttpSecurityApplicationContext;
import net.officefloor.web.state.HttpRequestState;

/**
 * Abstract mock {@link HttpSecurityActionContext} and
 * {@link HttpSecurityApplicationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractMockHttpSecurityActionContext<O extends Enum<O>, F extends Enum<F>>
		implements HttpSecurityActionContext, HttpSecurityApplicationContext<O, F> {

	/**
	 * {@link ServerHttpConnection}.
	 */
	protected final ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	protected final HttpSession session;

	/**
	 * {@link HttpRequestState}.
	 */
	protected final HttpRequestState requestState;

	/**
	 * Dependencies.
	 */
	protected final Map<O, Object> dependencies = new HashMap<O, Object>();

	/**
	 * {@link Flow} handlers.
	 */
	protected final Map<F, BiConsumer<Object, FlowCallback>> flows = new HashMap<>();

	/**
	 * Initiate.
	 */
	public AbstractMockHttpSecurityActionContext() {
		this(MockHttpServer.mockConnection());
	}

	/**
	 * Initiate.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 */
	public AbstractMockHttpSecurityActionContext(ServerHttpConnection connection) {
		this.connection = connection;
		this.session = MockWebApp.mockSession(this.connection);
		this.requestState = MockWebApp.mockRequestState(this.connection);
	}

	/**
	 * Registers a dependency.
	 * 
	 * @param key        Key for dependency.
	 * @param dependency Dependency object.
	 */
	public void registerObject(O key, Object dependency) {
		this.dependencies.put(key, dependency);
	}

	/**
	 * Registers a {@link Flow} handler.
	 * 
	 * @param key     {@link Flow} key.
	 * @param handler {@link Flow} handler.
	 */
	public void registerFlow(F key, BiConsumer<Object, FlowCallback> handler) {
		this.flows.put(key, handler);
	}

	/*
	 * ==================== HttpLogoutContext =========================
	 */

	@Override
	public ServerHttpConnection getConnection() {
		return this.connection;
	}

	@Override
	public String getQualifiedAttributeName(String attributeName) {
		return AuthenticationContextManagedObjectSource.getQualifiedAttributeName("mock", attributeName);
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public HttpRequestState getRequestState() {
		return this.requestState;
	}

	@Override
	public Object getObject(O key) {
		return this.dependencies.get(key);
	}

	@Override
	public void doFlow(F key, Object parameter, FlowCallback callback) {
		BiConsumer<Object, FlowCallback> handler = this.flows.get(key);
		handler.accept(parameter, callback);
	}

}
