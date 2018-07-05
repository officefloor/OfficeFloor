/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.LogoutRequest;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AccessControlListener;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.RatifyContext;

/**
 * {@link ManagedObjectSource} for the custom authentication.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class AuthenticationContextManagedObjectSource<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
		extends
		AbstractManagedObjectSource<AuthenticationContextManagedObjectSource.Dependencies, AuthenticationContextManagedObjectSource.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_SESSION
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		AUTHENTICATE, LOGOUT
	}

	/**
	 * Qualifier for the {@link HttpSecurity}.
	 */
	private final String qualifier;

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecurity<A, AC, C, O, F> httpSecurity;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * Instantiate.
	 *
	 * @param qualifier
	 *            Qualifier for the {@link HttpSecurity}.
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 */
	public AuthenticationContextManagedObjectSource(String qualifier, HttpSecurity<A, AC, C, O, F> httpSecurity) {
		this.qualifier = qualifier;
		this.httpSecurity = httpSecurity;
	}

	/*
	 * ==================== ManagedObjectSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, Flows> context) throws Exception {

		// Provide the meta-data
		context.setObjectClass(AuthenticationContext.class);
		context.setManagedObjectClass(AuthenticationContextManagedObject.class);

		// Provide the dependencies
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
		context.addDependency(Dependencies.HTTP_SESSION, HttpSession.class);

		// Configure flows
		context.addFlow(Flows.AUTHENTICATE, FunctionAuthenticateContext.class);
		context.addFlow(Flows.LOGOUT, FunctionLogoutContext.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
		this.executeContext = context;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new AuthenticationContextManagedObject();
	}

	/**
	 * {@link AuthenticationContext} {@link ManagedObject}.
	 */
	private class AuthenticationContextManagedObject implements ProcessAwareManagedObject, AsynchronousManagedObject,
			CoordinatingManagedObject<Dependencies>, AuthenticationContext<AC, C> {

		/**
		 * {@link AccessControlListener} instances. Typically 2 listeners (
		 * {@link HttpAuthentication} and {@link HttpAccessControl} ).
		 */
		private final List<AccessControlListener<AC>> listeners = new ArrayList<>(2);

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
		 * Cached access control.
		 */
		private AC accessControl = null;

		/**
		 * Cached {@link Escalation}.
		 */
		private Throwable escalation = null;

		/**
		 * Loads the access control.
		 *
		 * @param accessControl
		 *            Access control.
		 * @param escalation
		 *            {@link Throwable}.
		 */
		private void loadAccessControl(AC accessControl, Throwable escalation) {
			this.processAwareContext.run(() -> {

				// Change state
				this.accessControl = accessControl;
				this.escalation = escalation;

				// Void return
				return null;
			});
		}

		/**
		 * Notifies the change.
		 * 
		 * @param authenticateRequest
		 *            Optional {@link AuthenticateRequest}. May be <code>null</code>.
		 * @param logoutRequest
		 *            Optional {@link LogoutRequest}. May be <code>null</code>.
		 */
		private void unsafeNotifyChange(AuthenticateRequest authenticateRequest, LogoutRequest logoutRequest) {

			// Notify the registered listeners
			for (int i = 0; i < this.listeners.size(); i++) {
				AccessControlListener<AC> listener = this.listeners.get(i);
				listener.accessControlChange(this.accessControl, this.escalation);
			}

			// Notify passed in requests
			if (authenticateRequest != null) {
				authenticateRequest.authenticateComplete(this.escalation);
			}
			if (logoutRequest != null) {
				logoutRequest.logoutComplete(this.escalation);
			}
		}

		/**
		 * Safely notifies the change.
		 * 
		 * @param escalation
		 *            Possible {@link Escalation}.
		 * @param authenticateRequest
		 *            Optional {@link AuthenticateRequest}. May be <code>null</code>.
		 * @param logoutRequest
		 *            Optional {@link LogoutRequest}. May be <code>null</code>.
		 */
		private void safeNotifyChange(Throwable escalation, AuthenticateRequest authenticateRequest,
				LogoutRequest logoutRequest) {
			this.processAwareContext.run(() -> {

				// Notify of failure
				if (escalation != null) {
					this.accessControl = null;
					this.escalation = escalation;
				}

				// Notify of the change
				this.unsafeNotifyChange(authenticateRequest, logoutRequest);

				// Indicate complete
				this.asynchronousContext.complete(null);

				// Void return
				return null;
			});
		}

		/*
		 * =================== ManagedObject =====================
		 */

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
			this.processAwareContext = context;
		}

		@Override
		public void setAsynchronousContext(AsynchronousContext context) {
			this.asynchronousContext = context;
		}

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {
			this.connection = (ServerHttpConnection) registry.getObject(Dependencies.SERVER_HTTP_CONNECTION);
			this.session = (HttpSession) registry.getObject(Dependencies.HTTP_SESSION);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ================ AuthenticationContext =================
		 */

		@Override
		public String getQualifier() {
			return AuthenticationContextManagedObjectSource.this.qualifier;
		}

		@Override
		public void register(AccessControlListener<AC> accessControlListener) {
			this.processAwareContext.run(() -> this.listeners.add(accessControlListener));
		}

		@Override
		public void authenticate(C credentials, AuthenticateRequest authenticateRequest) {
			this.processAwareContext.run(() -> {

				// Determine if cached authentication
				if ((this.accessControl != null) || (this.escalation != null)) {
					this.unsafeNotifyChange(authenticateRequest, null);
					return null;
				}

				// Determine if possible to authenticate
				boolean isPossibleToAuthenticate = AuthenticationContextManagedObjectSource.this.httpSecurity
						.ratify(credentials, new RatifyContext<AC>() {

							@Override
							public ServerHttpConnection getConnection() {
								return AuthenticationContextManagedObject.this.connection;
							}

							@Override
							public HttpSession getSession() {
								return AuthenticationContextManagedObject.this.session;
							}

							@Override
							public void accessControlChange(AC accessControl, Throwable escalation) {
								AuthenticationContextManagedObject.this.accessControl = accessControl;
								AuthenticationContextManagedObject.this.escalation = escalation;
							}
						});

				// Determine if already authenticated
				if ((this.accessControl != null) || (this.escalation != null)) {
					this.unsafeNotifyChange(authenticateRequest, null);
					return null;
				}

				// Determine if may authenticate
				if (isPossibleToAuthenticate) {

					// New (avoid overwrite asynchronous context)
					AuthenticationContextManagedObject executeManagedObject = new AuthenticationContextManagedObject();

					// Attempt authentication
					this.asynchronousContext.start(null);
					AuthenticationContextManagedObjectSource.this.executeContext.invokeProcess(Flows.AUTHENTICATE,
							new FunctionAuthenticateContextImpl(this.connection, this.session, credentials),
							executeManagedObject, 0,
							(failure) -> this.safeNotifyChange(failure, authenticateRequest, null));
					return null;
				}

				// As here, unable to authenticate (indicate complete)
				if (authenticateRequest != null) {
					authenticateRequest.authenticateComplete(null);
				}

				// Void return
				return null;
			});
		}

		@Override
		public void logout(LogoutRequest logoutRequest) {
			this.processAwareContext.run(() -> {

				// Determine if already logged out
				if ((this.accessControl == null) && (this.escalation == null)) {
					this.unsafeNotifyChange(null, logoutRequest);
					return null;
				}

				// New managed object (stop overwrite async listener)
				AuthenticationContextManagedObject executeManagedObject = new AuthenticationContextManagedObject();

				// Trigger logout
				this.asynchronousContext.start(null);
				AuthenticationContextManagedObjectSource.this.executeContext.invokeProcess(Flows.LOGOUT,
						new FunctionLogoutContextImpl(this.connection, this.session), executeManagedObject, 0,
						(failure) -> this.safeNotifyChange(failure, null, logoutRequest));

				// Void return
				return null;
			});
		}

		@Override
		public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
			return this.processAwareContext.run(operation);
		}

		/**
		 * {@link FunctionAuthenticateContext} implementation.
		 */
		private class FunctionAuthenticateContextImpl implements FunctionAuthenticateContext<AC, C> {

			/**
			 * {@link ServerHttpConnection}.
			 */
			private final ServerHttpConnection connection;

			/**
			 * {@link HttpSession}.
			 */
			private final HttpSession session;

			/**
			 * Credentials.
			 */
			private final C credentials;

			/**
			 * Initiate.
			 * 
			 * @param connection
			 *            {@link ServerHttpConnection}.
			 * @param session
			 *            {@link HttpSession}.
			 * @param credentials
			 *            Credentials.
			 */
			public FunctionAuthenticateContextImpl(ServerHttpConnection connection, HttpSession session,
					C credentials) {
				this.connection = connection;
				this.session = session;
				this.credentials = credentials;
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
				return this.connection;
			}

			@Override
			public HttpSession getSession() {
				return this.session;
			}

			@Override
			public void accessControlChange(AC accessControl, Throwable escalation) {
				AuthenticationContextManagedObject.this.loadAccessControl(accessControl, escalation);
			}
		}

		/**
		 * {@link FunctionLogoutContext} implementation.
		 */
		private class FunctionLogoutContextImpl implements FunctionLogoutContext<AC> {

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
			 * @param connection
			 *            {@link ServerHttpConnection}.
			 * @param session
			 *            {@link HttpSession}.
			 */
			private FunctionLogoutContextImpl(ServerHttpConnection connection, HttpSession session) {
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
			public void accessControlChange(AC accessControl, Throwable escalation) {
				AuthenticationContextManagedObject.this.loadAccessControl(accessControl, escalation);
			}
		}
	}

}