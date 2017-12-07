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
package net.officefloor.web.security.impl;

import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource.Dependencies;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource.Flows;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticateCallback;
import net.officefloor.web.spi.security.HttpLogoutRequest;
import net.officefloor.web.spi.security.HttpRatifyContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link HttpAuthentication} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObject<A, AC, C> implements ProcessAwareManagedObject, AsynchronousManagedObject,
		CoordinatingManagedObject<Dependencies>, HttpAuthentication<C>, HttpRatifyContext<AC> {

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<A, AC, C, ?, ?> httpSecurity;

	/**
	 * {@link HttpAccessControlFactory}.
	 */
	private final HttpAccessControlFactory<AC> httpAccessControlFactory;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private ProcessAwareContext processAwareContext;

	/**
	 * {@link AsynchronousContext}.
	 */
	private AsynchronousContext asynchronousContext;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private HttpSession session;

	/**
	 * Access control.
	 */
	private AC accessControl = null;

	/**
	 * {@link HttpAccessControl}.
	 */
	private HttpAccessControl httpAccessControl = null;

	/**
	 * Failure.
	 */
	private Throwable failure = null;

	/**
	 * Initiate.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 * @param httpAccessControlFactory
	 *            {@link HttpAccessControlFactory}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	public HttpAuthenticationManagedObject(HttpSecurity<A, AC, C, ?, ?> httpSecurity,
			HttpAccessControlFactory<AC> httpAccessControlFactory, ManagedObjectExecuteContext<Flows> executeContext) {
		this.httpSecurity = httpSecurity;
		this.httpAccessControlFactory = httpAccessControlFactory;
		this.executeContext = executeContext;
	}

	/*
	 * ================== ManagedObject =========================
	 */

	@Override
	public void setProcessAwareContext(ProcessAwareContext context) {
		this.processAwareContext = context;
	}

	@Override
	public void setAsynchronousContext(AsynchronousContext asynchronousContext) {
		this.asynchronousContext = asynchronousContext;
	}

	@Override
	public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

		// Obtain the dependencies
		this.connection = (ServerHttpConnection) registry.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		this.session = (HttpSession) registry.getObject(Dependencies.HTTP_SESSION);

		// Undertake authentication
		this.processAwareContext.run(() -> {
			this.authenticate(null, null);
			return null;
		});
	}

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/*
	 * ================ HttpAuthentication ======================
	 */

	@Override
	public boolean isAuthenticated() throws HttpException {
		HttpAccessControl accessControl = this.getAccessControl();
		return (accessControl != null);
	}

	@Override
	public void authenticate(C credentials, HttpAuthenticateCallback authenticationCallback) {
		this.processAwareContext.run(() -> {

			// Unless undertaking request, ensure complete request
			boolean isCompleteRequest = true;
			try {

				// Determine if possible to authenticate
				boolean isPossibleToAuthenticate = this.httpSecurity.ratify(credentials, new HttpRatifyContext<AC>() {

					@Override
					public void setAccessControl(AC accessControl) {
						HttpAuthenticationManagedObject.this.processAwareContext.run(() -> {
							HttpAuthenticationManagedObject.this.accessControl = accessControl;
							return null;
						});
					}

					@Override
					public ServerHttpConnection getConnection() {
						return HttpAuthenticationManagedObject.this.processAwareContext
								.run(() -> HttpAuthenticationManagedObject.this.connection);
					}

					@Override
					public HttpSession getSession() {
						return HttpAuthenticationManagedObject.this.processAwareContext
								.run(() -> HttpAuthenticationManagedObject.this.session);
					}
				});

				// Determine if already authenticated
				if (this.accessControl != null) {
					return null;
				}

				// Determine if may authenticate
				if (isPossibleToAuthenticate) {

					// New managed object (avoid overwrite asynchronous context)
					// (Not used for execution but need to provide an instance)
					HttpAuthenticationManagedObject<A, AC, C> executeManagedObject = new HttpAuthenticationManagedObject<>(
							null, null, null);

					// Attempt authentication
					this.asynchronousContext.start(null);
					this.executeContext.invokeProcess(Flows.AUTHENTICATE,
							new FunctionAuthenticateContextImpl(credentials, authenticationCallback),
							executeManagedObject, 0, null);
					isCompleteRequest = false; // context will complete
				}

			} finally {
				// Complete authentication request (if not triggered)
				if ((isCompleteRequest) && (authenticationCallback != null)) {
					authenticationCallback.authenticationComplete();
				}
			}

			// Void return
			return null;
		});
	}

	@Override
	public HttpAccessControl getAccessControl() throws HttpException {
		return this.processAwareContext.run(() -> {

			// Lazy load the access control
			if (this.httpAccessControl == null) {

				// Determine if failure
				if (this.failure != null) {
					// Propagate the failure
					if (this.failure instanceof HttpException) {
						throw (HttpException) this.failure;
					} else if (this.failure instanceof RuntimeException) {
						throw (RuntimeException) this.failure;
					} else if (this.failure instanceof Error) {
						throw (Error) this.failure;
					} else {
						// Propagate failure
						throw new HttpException(this.failure);
					}
				}

				// Ensure have access control
				if (this.accessControl == null) {
					/*
					 * Ratify to attempt to obtain the security. Invoking ratify
					 * allows for loading the security from HTTP session.
					 */
					this.httpSecurity.ratify(null, this);
				}

				// Determine if have access control
				if (accessControl == null) {
					return null; // must have custom access control
				}

				// Create the HTTP access control
				this.httpAccessControl = this.httpAccessControlFactory.createHttpAccessControl(accessControl);
			}

			// Return the access control
			return this.httpAccessControl;
		});
	}

	@Override
	public void logout(HttpLogoutRequest logoutRequest) {
		this.processAwareContext.run(() -> {

			// Clear the access control
			this.accessControl = null;
			this.httpAccessControl = null;

			// New managed object (stop overwrite of asynchronous listener)
			// (Not used for execution but need to provide an instance)
			HttpAuthenticationManagedObject<A, AC, C> executeManagedObject = new HttpAuthenticationManagedObject<>(null,
					null, null);

			// Trigger logout
			this.executeContext.invokeProcess(Flows.LOGOUT,
					new FunctionLogoutContextImpl(logoutRequest, this.connection, this.session), executeManagedObject,
					0, null);

			// Void return
			return null;
		});
	}

	/*
	 * ======================== HttpRatifyContext =============================
	 */

	@Override
	public void setAccessControl(AC accessControl) {
		this.accessControl = accessControl;
	}

	@Override
	public ServerHttpConnection getConnection() {
		return this.connection;
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	/**
	 * {@link FunctionAuthenticateContext} implementation.
	 */
	private class FunctionAuthenticateContextImpl implements FunctionAuthenticateContext<AC, C> {

		/**
		 * Credentials.
		 */
		private final C credentials;

		/**
		 * {@link HttpAuthenticateCallback}.
		 */
		private final HttpAuthenticateCallback callback;

		/**
		 * Initiate.
		 * 
		 * @param credentials
		 *            Credentials.
		 * @param callback
		 *            {@link HttpAuthenticateCallback}.
		 */
		public FunctionAuthenticateContextImpl(C credentials, HttpAuthenticateCallback callback) {
			this.credentials = credentials;
			this.callback = callback;
		}

		/*
		 * ============== FunctionAuthenticateContext ==================
		 */

		@Override
		public C getCredentials() {
			return this.credentials;
		}

		@Override
		public ServerHttpConnection getConnection() {
			return HttpAuthenticationManagedObject.this.processAwareContext
					.run(() -> HttpAuthenticationManagedObject.this.connection);
		}

		@Override
		public HttpSession getSession() {
			return HttpAuthenticationManagedObject.this.processAwareContext
					.run(() -> HttpAuthenticationManagedObject.this.session);
		}

		@Override
		public void setAccessControl(AC accessControl) {
			HttpAuthenticationManagedObject.this.processAwareContext.run(() -> {

				// Specify the security
				HttpAuthenticationManagedObject.this.accessControl = accessControl;

				// Flag authentication complete
				HttpAuthenticationManagedObject.this.asynchronousContext.complete(null);

				// Void return
				return null;
			});

			// Flag authentication complete
			if (this.callback != null) {
				this.callback.authenticationComplete();
			}

		}

		@Override
		public void setFailure(Throwable failure) {
			HttpAuthenticationManagedObject.this.processAwareContext.run(() -> {

				// Specify the failure
				HttpAuthenticationManagedObject.this.failure = failure;

				// Flag authentication complete
				HttpAuthenticationManagedObject.this.asynchronousContext.complete(null);

				// Void return
				return null;
			});

			// Flag authentication complete
			if (this.callback != null) {
				this.callback.authenticationComplete();
			}
		}
	}

	/**
	 * {@link FunctionLogoutContext} implementation.
	 */
	private static class FunctionLogoutContextImpl implements FunctionLogoutContext {

		/**
		 * {@link HttpLogoutRequest}.
		 */
		private final HttpLogoutRequest request;

		/**
		 * {@link ServerHttpConnection}.
		 */
		private final ServerHttpConnection connection;

		/**
		 * {@link HttpSession}.
		 */
		private final HttpSession session;

		/**
		 * Initiate.
		 * 
		 * @param request
		 *            {@link HttpLogoutRequest}.
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param session
		 *            {@link HttpSession}.
		 */
		public FunctionLogoutContextImpl(HttpLogoutRequest request, ServerHttpConnection connection,
				HttpSession session) {
			this.request = request;
			this.connection = connection;
			this.session = session;
		}

		/*
		 * ================== FunctionLogoutContext ===================
		 */

		@Override
		public ServerHttpConnection getConnection() {
			return this.connection;

		}

		@Override
		public HttpSession getSession() {
			return this.session;
		}

		@Override
		public HttpLogoutRequest getHttpLogoutRequest() {
			return this.request;
		}
	}

}