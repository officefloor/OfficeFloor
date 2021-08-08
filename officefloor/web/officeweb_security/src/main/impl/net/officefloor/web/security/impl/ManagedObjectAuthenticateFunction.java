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
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} for
 * {@link AuthenticationContextManagedObjectSource} authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectAuthenticateFunction<AC extends Serializable, C, F extends Enum<F>>
		extends StaticManagedFunction<Indexed, F> {

	/**
	 * Name of the {@link HttpSecurity}.
	 */
	private final String httpSecurityName;

	/**
	 * {@link HttpSecurity}
	 */
	private final HttpSecurity<?, AC, C, ?, F> httpSecurity;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityName Name of the {@link HttpSecurity}.
	 * @param httpSecurity     {@link HttpSecurity}.
	 */
	public ManagedObjectAuthenticateFunction(String httpSecurityName, HttpSecurity<?, AC, C, ?, F> httpSecurity) {
		this.httpSecurityName = httpSecurityName;
		this.httpSecurity = httpSecurity;
	}

	/*
	 * ======================= ManagedFunction ================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void execute(ManagedFunctionContext<Indexed, F> context) throws Throwable {

		// Obtain the function authenticate context
		FunctionAuthenticateContext<AC, C> functionAuthenticateContext = (FunctionAuthenticateContext<AC, C>) context
				.getObject(0);

		// Obtain the credentials
		C credentials = functionAuthenticateContext.getCredentials();

		// Undertake authentication
		try {
			this.httpSecurity.authenticate(credentials,
					new AuthenticateContextImpl<>(functionAuthenticateContext, context));
		} catch (Throwable ex) {
			// Notify of failure in authentication
			functionAuthenticateContext.accessControlChange(null, ex);
		}
	}

	/**
	 * {@link AuthenticateContext} implementation.
	 */
	private class AuthenticateContextImpl<O extends Enum<O>> implements AuthenticateContext<AC, O, F> {

		/**
		 * {@link FunctionAuthenticateContext}.
		 */
		private final FunctionAuthenticateContext<AC, C> functionAuthenticateContext;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, F> functionContext;

		/**
		 * Initiate.
		 * 
		 * @param functionAuthenticateContext {@link FunctionAuthenticateContext}.
		 * @param functionContext             {@link ManagedFunctionContext}.
		 */
		public AuthenticateContextImpl(FunctionAuthenticateContext<AC, C> functionAuthenticateContext,
				ManagedFunctionContext<Indexed, F> functionContext) {
			this.functionAuthenticateContext = functionAuthenticateContext;
			this.functionContext = functionContext;
		}

		/*
		 * ================== HttpAuthenticateContext ========================
		 */

		@Override
		public ServerHttpConnection getConnection() {
			return this.functionAuthenticateContext.getConnection();
		}

		@Override
		public String getQualifiedAttributeName(String attributeName) {
			return AuthenticationContextManagedObjectSource
					.getQualifiedAttributeName(ManagedObjectAuthenticateFunction.this.httpSecurityName, attributeName);
		}

		@Override
		public HttpSession getSession() {
			return this.functionAuthenticateContext.getSession();
		}

		@Override
		public HttpRequestState getRequestState() {
			return this.functionAuthenticateContext.getRequestState();
		}

		@Override
		public Object getObject(O key) {
			// Offset for function dependency
			int index = key.ordinal() + 1;
			return this.functionContext.getObject(index);
		}

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			this.functionContext.doFlow(key, parameter, callback);
		}

		@Override
		public void accessControlChange(AC accessControl, Throwable escalation) {
			this.functionAuthenticateContext.accessControlChange(accessControl, escalation);
		}
	}

}
