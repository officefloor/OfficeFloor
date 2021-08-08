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

import java.security.Principal;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.LogoutRequest;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedObjectSource} for the default {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class DefaultHttpAuthenticationManagedObjectSource extends AbstractManagedObjectSource<Indexed, None> {

	/**
	 * {@link AcceptNegotiator}.
	 */
	private final AcceptNegotiator<int[]> negotiator;

	/**
	 * Names of the {@link HttpSecurity} instances.
	 */
	private final String[] httpSecurityNames;

	/**
	 * Instantiate.
	 * 
	 * @param negotiator        {@link AcceptNegotiator}.
	 * @param httpSecurityNames Names of the {@link HttpSecurity} instances.
	 */
	public DefaultHttpAuthenticationManagedObjectSource(AcceptNegotiator<int[]> negotiator,
			String[] httpSecurityNames) {
		this.negotiator = negotiator;
		this.httpSecurityNames = httpSecurityNames;
	}

	/*
	 * ==================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {
		context.setObjectClass(HttpAuthentication.class);
		context.setManagedObjectClass(DefaultHttpAuthenticationManagedObject.class);
		for (int i = 0; i < this.httpSecurityNames.length; i++) {
			context.addDependency(HttpAuthentication.class).setLabel(this.httpSecurityNames[i]);
		}
		context.addDependency(ServerHttpConnection.class).setLabel(ServerHttpConnection.class.getSimpleName());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new DefaultHttpAuthenticationManagedObject();
	}

	/**
	 * {@link ManagedObject} for the default {@link HttpAuthentication}.
	 */
	private class DefaultHttpAuthenticationManagedObject
			implements CoordinatingManagedObject<Indexed>, HttpAuthentication<Object> {

		/**
		 * {@link ObjectRegistry}.
		 */
		private ObjectRegistry<Indexed> objectRegistry;

		/**
		 * {@link HttpAuthentication} instances.
		 */
		private HttpAuthentication<?>[] httpAuthentications;

		/*
		 * ================= ManagedObject =============================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
			this.objectRegistry = registry;

			// Obtain the server HTTP connection
			ServerHttpConnection connection = (ServerHttpConnection) registry
					.getObject(DefaultHttpAuthenticationManagedObjectSource.this.httpSecurityNames.length);

			// Determine the HTTP authentications to use
			int[] httpAuthenticationIndexes = DefaultHttpAuthenticationManagedObjectSource.this.negotiator
					.getHandler(connection.getRequest());
			if (httpAuthenticationIndexes == null) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
						"No " + HttpAuthentication.class.getSimpleName() + " negotiated for default "
								+ HttpAuthentication.class.getSimpleName());
			}

			// Load the HTTP authentications
			this.httpAuthentications = new HttpAuthentication[httpAuthenticationIndexes.length];
			for (int i = 0; i < httpAuthenticationIndexes.length; i++) {
				int httpAuthenticationIndex = httpAuthenticationIndexes[i];
				this.httpAuthentications[i] = (HttpAuthentication<?>) registry.getObject(httpAuthenticationIndex);
			}
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ================= HttpAuthentication =========================
		 */

		@Override
		public boolean isAuthenticated() throws HttpException {

			// Determine if any are authenticated
			for (HttpAuthentication<?> authentication : this.httpAuthentications) {
				if (authentication.isAuthenticated()) {
					return true;
				}
			}

			// As here, not authenticated
			return false;
		}

		@Override
		public Class<Object> getCredentialsType() {
			return Object.class;
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void authenticate(Object credentials, AuthenticateRequest authenticationRequest) {

			// Determine if provided credentials
			boolean isAuthenticating = false;
			if (credentials == null) {

				// Attempt authentication on all
				for (HttpAuthentication authentication : this.httpAuthentications) {
					authentication.authenticate(null, authenticationRequest);
					isAuthenticating = true;
				}

			} else {
				// Trigger the authentication
				for (HttpAuthentication authentication : this.httpAuthentications) {

					// Obtain the credentials type
					Class<?> credentialsType = authentication.getCredentialsType();
					if (credentialsType == null) {
						// Undertake authentication
						authentication.authenticate(null, authenticationRequest);
						isAuthenticating = true;

					} else if (credentialsType.isAssignableFrom(credentials.getClass())) {
						// Undertake authentication
						authentication.authenticate(credentials, authenticationRequest);
						isAuthenticating = true;
					}
				}
			}

			// Ensure authentication was triggered
			if (!isAuthenticating) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
						"No matching " + HttpSecurity.class.getSimpleName() + " for credentials of type "
								+ (credentials == null ? "null" : credentials.getClass().getName()));
			}
		}

		@Override
		public HttpAccessControl getAccessControl() throws AuthenticationRequiredException, HttpException {
			return new DefaultHttpAccessControl(this.httpAuthentications);
		}

		@Override
		public void logout(LogoutRequest logoutRequest) {
			// Log out from all security
			for (int i = 0; i < DefaultHttpAuthenticationManagedObjectSource.this.httpSecurityNames.length; i++) {
				HttpAuthentication<?> authentication = (HttpAuthentication<?>) this.objectRegistry.getObject(i);
				authentication.logout(null);
			}
		}
	}

	/**
	 * Default {@link HttpAccessControl}.
	 */
	private static class DefaultHttpAccessControl implements HttpAccessControl {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link HttpAccessControl} instances.
		 */
		private final HttpAccessControl[] httpAccessControls;

		/**
		 * Instantiate.
		 * 
		 * @param httpAuthentications {@link HttpAuthentication} instances available for
		 *                            the {@link HttpRequest}.
		 */
		private DefaultHttpAccessControl(HttpAuthentication<?>[] httpAuthentications) {

			// Load the available HTTP access controls
			this.httpAccessControls = new HttpAccessControl[httpAuthentications.length];
			boolean isHttpAccessControlAvailable = false;
			for (int i = 0; i < httpAuthentications.length; i++) {
				HttpAuthentication<?> authentication = httpAuthentications[i];
				if (authentication.isAuthenticated()) {
					this.httpAccessControls[i] = authentication.getAccessControl();
					if (this.httpAccessControls[i] != null) {
						isHttpAccessControlAvailable = true;
					}
				}
			}

			// Ensure have at least one HTTP access control
			if (!isHttpAccessControlAvailable) {
				throw new AuthenticationRequiredException();
			}
		}

		/*
		 * =============== HttpAccessControl ====================
		 */

		@Override
		public String getAuthenticationScheme() {

			// Use the first available authentication scheme
			for (HttpAccessControl accessControl : this.httpAccessControls) {
				if (accessControl != null) {
					return accessControl.getAuthenticationScheme();
				}
			}

			// As here, invalid as should always have access control
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
					"No " + HttpAccessControl.class.getSimpleName() + " available");
		}

		@Override
		public Principal getPrincipal() {

			// Use the first available authentication scheme
			for (HttpAccessControl accessControl : this.httpAccessControls) {
				if (accessControl != null) {
					return accessControl.getPrincipal();
				}
			}

			// As here, invalid as should always have access control
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
					"No " + HttpAccessControl.class.getSimpleName() + " available");
		}

		@Override
		public boolean inRole(String role) {

			// Determine in any access control role
			for (HttpAccessControl accessControl : this.httpAccessControls) {
				if (accessControl != null) {
					if (accessControl.inRole(role)) {
						return true;
					}
				}
			}

			// As here, not in the role
			return false;
		}
	}

}
