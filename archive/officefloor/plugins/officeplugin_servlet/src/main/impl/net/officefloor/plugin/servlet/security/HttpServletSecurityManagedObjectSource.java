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
package net.officefloor.plugin.servlet.security;

import java.security.Principal;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpAuthenticateRequest;
import net.officefloor.plugin.web.http.security.HttpAuthentication;
import net.officefloor.plugin.web.http.security.HttpSecurity;

/**
 * {@link ManagedObjectSource} for the {@link HttpServletSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletSecurityManagedObjectSource
		extends
		AbstractManagedObjectSource<HttpServletSecurityManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_AUTHENTICATION
	}

	/*
	 * ======================= ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {

		// Specify the meta-data
		context.setObjectClass(HttpServletSecurity.class);
		context.setManagedObjectClass(HttpSecurityManagedObject.class);

		// Add the dependency
		context.addDependency(Dependencies.HTTP_AUTHENTICATION,
				HttpAuthentication.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSecurityManagedObject<Object, Object>();
	}

	/**
	 * {@link ManagedObject} for the HTTP security.
	 */
	public static class HttpSecurityManagedObject<S, C> implements
			AsynchronousManagedObject, CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * {@link HttpAuthentication}.
		 */
		private HttpAuthentication<S, C> authentication;

		/**
		 * Flags authentication complete.
		 */
		private synchronized void flagAuthenticationComplete() {
			this.listener.notifyComplete();
		}

		/*
		 * ==================== ManagedObject =========================
		 */

		@Override
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		@SuppressWarnings("unchecked")
		public synchronized void loadObjects(
				ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the HTTP authentication
			this.authentication = (HttpAuthentication<S, C>) registry
					.getObject(Dependencies.HTTP_AUTHENTICATION);

			// Flag started authenticate
			this.listener.notifyStarted();

			// Trigger authentication
			this.authentication
					.authenticate(new HttpAuthenticateRequestImpl<S, C>(this));
		}

		@Override
		public Object getObject() throws Throwable {

			// Obtain the security
			HttpSecurity security = (HttpSecurity) this.authentication
					.getHttpSecurity();
			if (security == null) {
				return null; // not authenticated
			}

			// Wrap and return with HTTP Servlet Security
			return new HttpServletSecurityImpl(security);
		}
	}

	/**
	 * {@link HttpAuthenticateRequest} implementation.
	 */
	private static class HttpAuthenticateRequestImpl<S, C> implements
			HttpAuthenticateRequest<C> {

		/**
		 * {@link HttpSecurityManagedObject}.
		 */
		private final HttpSecurityManagedObject<S, C> managedObject;

		/**
		 * Initiate.
		 * 
		 * @param managedObject
		 *            {@link HttpSecurityManagedObject}.
		 */
		public HttpAuthenticateRequestImpl(
				HttpSecurityManagedObject<S, C> managedObject) {
			this.managedObject = managedObject;
		}

		/*
		 * ==================== HttpAuthenticateRequest ===============
		 */

		@Override
		public C getCredentials() {
			// Never credentials for loading
			return null;
		}

		@Override
		public void authenticationComplete() {
			// Indicate authentication complete
			this.managedObject.flagAuthenticationComplete();
		}
	}

	/**
	 * {@link HttpServletSecurity} implementation.
	 */
	private static class HttpServletSecurityImpl implements HttpServletSecurity {

		/**
		 * {@link HttpSecurity} delegate.
		 */
		private final HttpSecurity security;

		/**
		 * Initiate.
		 * 
		 * @param security
		 *            {@link HttpSecurity}.
		 */
		public HttpServletSecurityImpl(HttpSecurity security) {
			this.security = security;
		}

		/*
		 * ==================== HttpServletSecurity ======================
		 */

		@Override
		public String getAuthenticationScheme() {
			return (this.security == null ? null : this.security
					.getAuthenticationScheme());
		}

		@Override
		public Principal getUserPrincipal() {
			return (this.security == null ? null : this.security
					.getUserPrincipal());
		}

		@Override
		public String getRemoteUser() {
			return (this.security == null ? null : this.security
					.getRemoteUser());
		}

		@Override
		public boolean isUserInRole(String role) {
			return (this.security == null ? null : this.security
					.isUserInRole(role));
		}
	}

}