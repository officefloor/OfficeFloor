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

package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} to log out.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectLogoutFunction<AC extends Serializable, O extends Enum<O>, F extends Enum<F>>
		extends StaticManagedFunction<Indexed, F> {

	/**
	 * Name of the {@link HttpSecurity}.
	 */
	private final String httpSecurityName;

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<?, AC, ?, O, F> httpSecurity;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityName Name of the {@link HttpSecurity}.
	 * @param httpSecurity     {@link HttpSecurity}.
	 */
	public ManagedObjectLogoutFunction(String httpSecurityName, HttpSecurity<?, AC, ?, O, F> httpSecurity) {
		this.httpSecurityName = httpSecurityName;
		this.httpSecurity = httpSecurity;
	}

	/*
	 * =================== ManagedFunction ======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void execute(ManagedFunctionContext<Indexed, F> context) throws Throwable {

		// Obtain the dependencies
		final FunctionLogoutContext<AC> logoutContext = (FunctionLogoutContext<AC>) context.getObject(0);

		try {
			// Logout
			this.httpSecurity.logout(new LogoutContextImpl(logoutContext, context));

			// Notify of successful logout
			logoutContext.accessControlChange(null, null);

		} catch (Throwable ex) {
			logoutContext.accessControlChange(null, ex);
		}
	}

	/**
	 * {@link LogoutContext} implementation.
	 */
	private class LogoutContextImpl implements LogoutContext<O, F> {

		/**
		 * {@link FunctionLogoutContext}.
		 */
		private FunctionLogoutContext<AC> logoutContext;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, F> context;

		/**
		 * Initiate.
		 * 
		 * @param logoutContext {@link FunctionLogoutContext}.
		 * @param context       {@link ManagedFunctionContext}.
		 */
		private LogoutContextImpl(FunctionLogoutContext<AC> logoutContext, ManagedFunctionContext<Indexed, F> context) {
			this.logoutContext = logoutContext;
			this.context = context;
		}

		/*
		 * ================== HttpLogoutContext ======================
		 */

		@Override
		public ServerHttpConnection getConnection() {
			return this.logoutContext.getConnection();
		}

		@Override
		public String getQualifiedAttributeName(String attributeName) {
			return AuthenticationContextManagedObjectSource
					.getQualifiedAttributeName(ManagedObjectLogoutFunction.this.httpSecurityName, attributeName);
		}

		@Override
		public HttpSession getSession() {
			return this.logoutContext.getSession();
		}

		@Override
		public HttpRequestState getRequestState() {
			return this.logoutContext.getRequestState();
		}

		@Override
		public Object getObject(O key) {
			// Obtain the index (offset by logout dependencies)
			int index = key.ordinal() + 1;
			return this.context.getObject(index);
		}

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			this.context.doFlow(key, parameter, callback);
		}
	}

}
