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
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.LogoutRequest;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AccessControlListener;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.state.HttpRequestState;

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
	 * Obtains the qualified attribute name.
	 * 
	 * @param qualifier     {@link HttpSecurity} qualifier.
	 * @param attributeName Attribute name.
	 * @return Qualified attribute name.
	 */
	public static String getQualifiedAttributeName(String qualifier, String attributeName) {
		return HttpSecurity.class.getName() + "." + qualifier + "." + attributeName;
	}

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_SESSION, HTTP_REQUEST_STATE
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
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<A, AC, C, O, F> httpSecurity;

	/**
	 * {@link SafeManagedObjectService}.
	 */
	private volatile SafeManagedObjectService<Flows> servicer;

	/**
	 * Instantiate.
	 *
	 * @param qualifier    Qualifier for the {@link HttpSecurity}.
	 * @param httpSecurity {@link HttpSecurity}.
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
		context.addDependency(Dependencies.HTTP_REQUEST_STATE, HttpRequestState.class);

		// Configure flows
		context.addFlow(Flows.AUTHENTICATE, FunctionAuthenticateContext.class);
		context.addFlow(Flows.LOGOUT, FunctionLogoutContext.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
		this.servicer = new SafeManagedObjectService<>(context);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new AuthenticationContextManagedObject();
	}

	/**
	 * {@link AuthenticationContext} {@link ManagedObject}.
	 */
	private class AuthenticationContextManagedObject implements ContextAwareManagedObject, AsynchronousManagedObject,
			CoordinatingManagedObject<Dependencies>, AuthenticationContext<AC, C> {

		/**
		 * {@link AccessControlListener} instances. Typically 2 listeners (
		 * {@link HttpAuthentication} and {@link HttpAccessControl} ).
		 */
		private final List<AccessControlListener<? super AC>> listeners = new ArrayList<>(2);

		/**
		 * {@link ManagedObjectContext}.
		 */
		private ManagedObjectContext managedObjectContext;

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
		 * {@link HttpRequestState}.
		 */
		private HttpRequestState requestState;

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
		 * @param accessControl Access control.
		 * @param escalation    {@link Throwable}.
		 */
		private void loadAccessControl(AC accessControl, Throwable escalation) {
			this.managedObjectContext.run(() -> {

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
		 * @param authenticateRequest Optional {@link AuthenticateRequest}. May be
		 *                            <code>null</code>.
		 * @param logoutRequest       Optional {@link LogoutRequest}. May be
		 *                            <code>null</code>.
		 */
		private void unsafeNotifyChange(AuthenticateRequest authenticateRequest, LogoutRequest logoutRequest) {

			// Notify the registered listeners
			for (int i = 0; i < this.listeners.size(); i++) {
				AccessControlListener<? super AC> listener = this.listeners.get(i);
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
		 * @param escalation          Possible {@link Escalation}.
		 * @param authenticateRequest Optional {@link AuthenticateRequest}. May be
		 *                            <code>null</code>.
		 * @param logoutRequest       Optional {@link LogoutRequest}. May be
		 *                            <code>null</code>.
		 */
		private void safeNotifyChange(Throwable escalation, AuthenticateRequest authenticateRequest,
				LogoutRequest logoutRequest) {
			this.managedObjectContext.run(() -> {

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
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.managedObjectContext = context;
		}

		@Override
		public void setAsynchronousContext(AsynchronousContext context) {
			this.asynchronousContext = context;
		}

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {
			this.connection = (ServerHttpConnection) registry.getObject(Dependencies.SERVER_HTTP_CONNECTION);
			this.session = (HttpSession) registry.getObject(Dependencies.HTTP_SESSION);
			this.requestState = (HttpRequestState) registry.getObject(Dependencies.HTTP_REQUEST_STATE);
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
		public void register(AccessControlListener<? super AC> accessControlListener) {
			this.managedObjectContext.run(() -> this.listeners.add(accessControlListener));
		}

		@Override
		public void authenticate(C credentials, AuthenticateRequest authenticateRequest) {
			this.managedObjectContext.run(() -> {

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
							public String getQualifiedAttributeName(String attributeName) {
								return AuthenticationContextManagedObjectSource.getQualifiedAttributeName(
										AuthenticationContextManagedObjectSource.this.qualifier, attributeName);
							}

							@Override
							public HttpSession getSession() {
								return AuthenticationContextManagedObject.this.session;
							}

							@Override
							public HttpRequestState getRequestState() {
								return AuthenticationContextManagedObject.this.requestState;
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
					AuthenticationContextManagedObjectSource.this.servicer.invokeProcess(Flows.AUTHENTICATE,
							new FunctionAuthenticateContextImpl(this.connection, this.session, this.requestState,
									credentials),
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
			this.managedObjectContext.run(() -> {

				// Determine if already logged out
				if ((this.accessControl == null) && (this.escalation == null)) {
					this.unsafeNotifyChange(null, logoutRequest);
					return null;
				}

				// New managed object (stop overwrite async listener)
				AuthenticationContextManagedObject executeManagedObject = new AuthenticationContextManagedObject();

				// Trigger logout
				this.asynchronousContext.start(null);
				AuthenticationContextManagedObjectSource.this.servicer.invokeProcess(Flows.LOGOUT,
						new FunctionLogoutContextImpl(this.connection, this.session, this.requestState),
						executeManagedObject, 0, (failure) -> this.safeNotifyChange(failure, null, logoutRequest));

				// Void return
				return null;
			});
		}

		@Override
		public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
			return this.managedObjectContext.run(operation);
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
			 * {@link HttpRequestState}.
			 */
			private final HttpRequestState requestState;

			/**
			 * Credentials.
			 */
			private final C credentials;

			/**
			 * Initiate.
			 * 
			 * @param connection   {@link ServerHttpConnection}.
			 * @param session      {@link HttpSession}.
			 * @param requestState {@link HttpRequestState}.
			 * @param credentials  Credentials.
			 */
			public FunctionAuthenticateContextImpl(ServerHttpConnection connection, HttpSession session,
					HttpRequestState requestState, C credentials) {
				this.connection = connection;
				this.session = session;
				this.requestState = requestState;
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
			public HttpRequestState getRequestState() {
				return this.requestState;
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
			 * {@link HttpRequestState}.
			 */
			private final HttpRequestState requestState;

			/**
			 * Initiate.
			 * 
			 * @param connection   {@link ServerHttpConnection}.
			 * @param session      {@link HttpSession}.
			 * @param requestState {@link HttpRequestState}.
			 */
			private FunctionLogoutContextImpl(ServerHttpConnection connection, HttpSession session,
					HttpRequestState requestState) {
				this.connection = connection;
				this.session = session;
				this.requestState = requestState;
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
			public HttpRequestState getRequestState() {
				return this.requestState;
			}

			@Override
			public void accessControlChange(AC accessControl, Throwable escalation) {
				AuthenticationContextManagedObject.this.loadAccessControl(accessControl, escalation);
			}
		}
	}

}
