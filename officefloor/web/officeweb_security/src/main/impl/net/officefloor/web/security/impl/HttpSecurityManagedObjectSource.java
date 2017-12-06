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

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpAuthenticateCallback;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link ManagedObjectSource} for the {@link HttpSecuritySource} (or its
 * equivalent application specific interface).
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObjectSource
		extends AbstractManagedObjectSource<HttpSecurityManagedObjectSource.Dependencies, None> {

	/**
	 * Name of {@link Property} for the HTTP security type.
	 */
	public static final String PROPERTY_HTTP_SECURITY_TYPE = "http.security.type";

	/**
	 * <p>
	 * Name of {@link Property} indicating whether to escalate
	 * {@link HttpAuthenticationRequiredException} if not authenticated.
	 * <p>
	 * By default, it will escalate. Specifying <code>false</code> for this
	 * property will allow a <code>null</code> HTTP Security to be provided.
	 */
	public static final String PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED = "http.security.escalate.authentication.required";

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_AUTHENTICATION
	}

	/**
	 * Indicates if escalate on <code>null</code> HTTP Security.
	 */
	private boolean isEscalateNullHttpSecurity;

	/*
	 * ======================= ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_HTTP_SECURITY_TYPE, "HTTP Security Type");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the security type
		String securityTypeName = mosContext.getProperty(PROPERTY_HTTP_SECURITY_TYPE);
		Class<?> securityType = mosContext.loadClass(securityTypeName);

		// Determine if allowing null HTTP Security
		this.isEscalateNullHttpSecurity = Boolean.parseBoolean(
				mosContext.getProperty(PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED, String.valueOf(true)));

		// Specify the meta-data
		context.setObjectClass(securityType);
		context.setManagedObjectClass(HttpSecurityManagedObject.class);

		// Add the dependency
		context.addDependency(Dependencies.HTTP_AUTHENTICATION, HttpAuthentication.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSecurityManagedObject<Object, Object>(this.isEscalateNullHttpSecurity);
	}

	/**
	 * {@link ManagedObject} for the HTTP security.
	 */
	public static class HttpSecurityManagedObject<AC, C>
			implements AsynchronousManagedObject, CoordinatingManagedObject<Dependencies> {

		/**
		 * Indicates if escalate <code>null</code> HTTP Security.
		 */
		private final boolean isEscalateNullHttpSecurity;

		/**
		 * {@link AsynchronousContext}.
		 */
		private AsynchronousContext asynchronousContext;

		/**
		 * {@link HttpAuthentication}.
		 */
		private HttpAuthentication<C> authentication;

		/**
		 * Initiate.
		 * 
		 * @param isEscalateNullHttpSecurity
		 *            Indicates if escalate <code>null</code> HTTP Security.
		 */
		public HttpSecurityManagedObject(boolean isEscalateNullHttpSecurity) {
			this.isEscalateNullHttpSecurity = isEscalateNullHttpSecurity;
		}

		/**
		 * Flags authentication complete.
		 */
		private synchronized void flagAuthenticationComplete() {
			this.asynchronousContext.complete(null);
		}

		/*
		 * ==================== ManagedObject =========================
		 */

		@Override
		public void setAsynchronousContext(AsynchronousContext asynchronousContext) {
			this.asynchronousContext = asynchronousContext;
		}

		@Override
		@SuppressWarnings("unchecked")
		public synchronized void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the HTTP authentication
			this.authentication = (HttpAuthentication<C>) registry.getObject(Dependencies.HTTP_AUTHENTICATION);

			// Flag started authenticate
			this.asynchronousContext.start(null);

			// Trigger authentication
			this.authentication.authenticate(null, new HttpAuthenticateRequestImpl<>(this));
		}

		@Override
		public Object getObject() throws Throwable {

			// Obtain the security
			Object security = this.authentication.getAccessControl();

			// Ensure have the security if escalate on null HTTP Security
			if ((security == null) && (this.isEscalateNullHttpSecurity)) {
				throw new HttpAuthenticationRequiredException();
			}

			// Return the security
			return security;
		}
	}

	/**
	 * {@link HttpAuthenticateCallback} implementation.
	 */
	private static class HttpAuthenticateRequestImpl<AC, C> implements HttpAuthenticateCallback {

		/**
		 * {@link HttpSecurityManagedObject}.
		 */
		private final HttpSecurityManagedObject<AC, C> managedObject;

		/**
		 * Initiate.
		 * 
		 * @param managedObject
		 *            {@link HttpSecurityManagedObject}.
		 */
		public HttpAuthenticateRequestImpl(HttpSecurityManagedObject<AC, C> managedObject) {
			this.managedObject = managedObject;
		}

		/*
		 * ==================== HttpAuthenticateRequest ===============
		 */

		@Override
		public void authenticationComplete() {
			// Indicate authentication complete
			this.managedObject.flagAuthenticationComplete();
		}
	}

}