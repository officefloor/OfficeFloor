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
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpAuthenticateCallback;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link ManagedObjectSource} for the {@link HttpSecuritySource} (or its
 * equivalent application specific interface).
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAccessControlManagedObjectSource
		extends AbstractManagedObjectSource<HttpAccessControlManagedObjectSource.Dependencies, None> {

	/**
	 * Name of {@link Property} for the access control type.
	 */
	public static final String PROPERTY_ACCESS_CONTROL_TYPE = "access.control.type";

	/**
	 * <p>
	 * Name of {@link Property} indicating whether to escalate
	 * {@link HttpAuthenticationRequiredException} if not authenticated.
	 * <p>
	 * By default, it will escalate. Specifying <code>false</code> for this
	 * property will allow a <code>null</code> {@link HttpAccessControl} to be
	 * provided.
	 */
	public static final String PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED = "http.security.escalate.authentication.required";

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_AUTHENTICATION
	}

	/**
	 * Indicates if escalate on <code>null</code> access control.
	 */
	private boolean isEscalateNullAccessControl;

	/*
	 * ======================= ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_ACCESS_CONTROL_TYPE, "Access Control Type");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the access control type
		String accessControlTypeName = mosContext.getProperty(PROPERTY_ACCESS_CONTROL_TYPE);
		Class<?> accessControlType = mosContext.loadClass(accessControlTypeName);

		// Determine if allowing null access control
		this.isEscalateNullAccessControl = Boolean.parseBoolean(
				mosContext.getProperty(PROPERTY_IS_ESCALATE_AUTHENTICATION_REQUIRED, String.valueOf(true)));

		// Specify the meta-data
		context.setObjectClass(accessControlType);
		context.setManagedObjectClass(HttpAccessControlManagedObject.class);

		// Add the dependency
		context.addDependency(Dependencies.HTTP_AUTHENTICATION, HttpAuthentication.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpAccessControlManagedObject<Object, Object>(this.isEscalateNullAccessControl);
	}

	/**
	 * {@link ManagedObject} for the {@link HttpAccessControl}.
	 */
	public static class HttpAccessControlManagedObject<AC, C>
			implements AsynchronousManagedObject, CoordinatingManagedObject<Dependencies> {

		/**
		 * Indicates if escalate <code>null</code> access control.
		 */
		private final boolean isEscalateNullAccessControl;

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
		 * @param isEscalateNullAccessControl
		 *            Indicates if escalate <code>null</code> access control.
		 */
		public HttpAccessControlManagedObject(boolean isEscalateNullAccessControl) {
			this.isEscalateNullAccessControl = isEscalateNullAccessControl;
		}

		/**
		 * Flags authentication complete.
		 */
		private void flagAuthenticationComplete() {
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
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the HTTP authentication
			this.authentication = (HttpAuthentication<C>) registry.getObject(Dependencies.HTTP_AUTHENTICATION);

			// Flag started authenticate
			this.asynchronousContext.start(null);

			// Trigger authentication
			this.authentication.authenticate(null, new HttpAuthenticateRequestImpl<>(this));
		}

		@Override
		public Object getObject() throws Throwable {

			// Obtain the access control
			HttpAccessControl accessControl = this.authentication.getAccessControl();

			// Ensure have the access control if escalate on null access control
			if ((accessControl == null) && (this.isEscalateNullAccessControl)) {
				throw new HttpAuthenticationRequiredException();
			}

			// Return the access control
			return accessControl;
		}
	}

	/**
	 * {@link HttpAuthenticateCallback} implementation.
	 */
	private static class HttpAuthenticateRequestImpl<AC, C> implements HttpAuthenticateCallback {

		/**
		 * {@link HttpAccessControlManagedObject}.
		 */
		private final HttpAccessControlManagedObject<AC, C> managedObject;

		/**
		 * Initiate.
		 * 
		 * @param managedObject
		 *            {@link HttpAccessControlManagedObject}.
		 */
		public HttpAuthenticateRequestImpl(HttpAccessControlManagedObject<AC, C> managedObject) {
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