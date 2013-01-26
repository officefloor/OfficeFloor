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
package net.officefloor.plugin.web.http.security;

import java.io.IOException;

import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource.Flows;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link HttpAuthentication} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObject<S, C> implements
		AsynchronousManagedObject, CoordinatingManagedObject<Dependencies>,
		HttpAuthentication<S, C> {

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<S, C, ?, ?> httpSecuritySource;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * {@link AsynchronousListener}.
	 */
	private AsynchronousListener listener;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private HttpSession session;

	/**
	 * Security.
	 */
	private S security = null;

	/**
	 * Failure.
	 */
	private Throwable failure = null;

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	public HttpAuthenticationManagedObject(
			HttpSecuritySource<S, C, ?, ?> httpSecuritySource,
			ManagedObjectExecuteContext<Flows> executeContext) {
		this.httpSecuritySource = httpSecuritySource;
		this.executeContext = executeContext;
	}

	/*
	 * ================== ManagedObject =========================
	 */

	@Override
	public void registerAsynchronousCompletionListener(
			AsynchronousListener listener) {
		this.listener = listener;
	}

	@Override
	public synchronized void loadObjects(ObjectRegistry<Dependencies> registry)
			throws Throwable {

		// Obtain the dependencies
		this.connection = (ServerHttpConnection) registry
				.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		this.session = (HttpSession) registry
				.getObject(Dependencies.HTTP_SESSION);

		// Undertake authentication
		this.authenticate(null);
	}

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/*
	 * ================ HttpAuthentication ======================
	 */

	@Override
	public synchronized void authenticate(
			HttpAuthenticateRequest<C> authenticationRequest) {

		// Obtain the credentials
		C credentials = null;
		if (authenticationRequest != null) {
			credentials = authenticationRequest.getCredentials();
		}

		// Determine if possible to authenticate
		HttpRatifyContextImpl context = new HttpRatifyContextImpl(credentials);
		boolean isPossibleToAuthenticate = this.httpSecuritySource
				.ratify(context);

		// Determine if already authenticated
		if (this.security != null) {
			return;
		}

		// Determine if may authenticate
		if (isPossibleToAuthenticate) {

			// Attempt authentication
			this.listener.notifyStarted();
			this.executeContext.invokeProcess(Flows.AUTHENTICATE,
					new TaskAuthenticateContextImpl(credentials,
							authenticationRequest), this, 0);
		}
	}

	@Override
	public synchronized S getHttpSecurity() throws IOException {

		// Determine if failure
		if (this.failure != null) {
			// Propagate the failure
			if (this.failure instanceof IOException) {
				throw (IOException) this.failure;
			} else if (this.failure instanceof RuntimeException) {
				throw (RuntimeException) this.failure;
			} else if (this.failure instanceof Error) {
				throw (Error) this.failure;
			} else {
				// Propagate failure
				throw new IllegalStateException("Authentication error: "
						+ this.failure.getMessage() + " ("
						+ this.failure.getClass().getName() + ")", this.failure);
			}
		}

		// Return the security
		return this.security;
	}

	/**
	 * {@link HttpRatifyContext} implementation.
	 */
	private class HttpRatifyContextImpl implements HttpRatifyContext<S, C> {

		/**
		 * Credentials.
		 */
		private final C credentials;

		/**
		 * Initiate.
		 * 
		 * @param credentials
		 *            Credentials.
		 */
		public HttpRatifyContextImpl(C credentials) {
			this.credentials = credentials;
		}

		/*
		 * ==================== HttpRatifyContext ======================
		 */

		@Override
		public C getCredentials() {
			return this.credentials;
		}

		@Override
		public ServerHttpConnection getConnection() {
			return HttpAuthenticationManagedObject.this.connection;
		}

		@Override
		public HttpSession getSession() {
			return HttpAuthenticationManagedObject.this.session;
		}

		@Override
		public void setHttpSecurity(S security) {
			HttpAuthenticationManagedObject.this.security = security;
		}
	}

	/**
	 * {@link TaskAuthenticateContext} implementation.
	 */
	private class TaskAuthenticateContextImpl implements
			TaskAuthenticateContext<S, C> {

		/**
		 * Credentials.
		 */
		private final C credentials;

		/**
		 * {@link HttpAuthenticateRequest}.
		 */
		private final HttpAuthenticateRequest<C> request;

		/**
		 * Initiate.
		 * 
		 * @param credentials
		 *            Credentials.
		 * @param request
		 *            {@link HttpAuthenticateRequest}.
		 */
		public TaskAuthenticateContextImpl(C credentials,
				HttpAuthenticateRequest<C> request) {
			this.credentials = credentials;
			this.request = request;
		}

		/*
		 * ============== TaskAuthenticateContext ==================
		 */

		@Override
		public C getCredentials() {
			return this.credentials;
		}

		@Override
		public ServerHttpConnection getConnection() {
			synchronized (HttpAuthenticationManagedObject.this) {
				return HttpAuthenticationManagedObject.this.connection;
			}
		}

		@Override
		public HttpSession getSession() {
			synchronized (HttpAuthenticationManagedObject.this) {
				return HttpAuthenticationManagedObject.this.session;
			}
		}

		@Override
		public void setHttpSecurity(S security) {
			synchronized (HttpAuthenticationManagedObject.this) {

				// Specify the security
				HttpAuthenticationManagedObject.this.security = security;

				// Flag authentication complete
				HttpAuthenticationManagedObject.this.listener.notifyComplete();
			}

			// Flag authentication complete
			if (this.request != null) {
				this.request.authenticationComplete();
			}
		}

		@Override
		public void setFailure(Throwable failure) {
			synchronized (HttpAuthenticationManagedObject.this) {

				// Specify the failure
				HttpAuthenticationManagedObject.this.failure = failure;

				// Flag authentication complete
				HttpAuthenticationManagedObject.this.listener.notifyComplete();
			}

			// Flag authentication complete
			if (this.request != null) {
				this.request.authenticationComplete();
			}
		}
	}

}