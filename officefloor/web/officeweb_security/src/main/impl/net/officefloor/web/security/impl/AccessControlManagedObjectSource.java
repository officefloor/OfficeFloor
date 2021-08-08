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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.spi.security.AccessControlListener;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedObjectSource} for the access control object.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class AccessControlManagedObjectSource<AC extends Serializable, C>
		extends AbstractManagedObjectSource<AccessControlManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		AUTHENTICATION_CONTEXT
	}

	/**
	 * Name of {@link HttpSecurity} for this access control.
	 */
	private final String httpSecurityName;

	/**
	 * Access control type.
	 */
	private final Class<AC> accessControlType;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityName
	 *            Name of {@link HttpSecurity} for this access control.
	 * @param accessControlType
	 *            Access control type.
	 */
	public AccessControlManagedObjectSource(String httpSecurityName, Class<AC> accessControlType) {
		this.httpSecurityName = httpSecurityName;
		this.accessControlType = accessControlType;
	}

	/*
	 * ======================= ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {

		// Obtain the access control type
		context.addManagedObjectExtension(HttpAccessControl.class, (managedObject) -> {
			try {
				return (HttpAccessControl) managedObject.getObject();
			} catch (Throwable e) {
				return null;
			}
		});

		// Specify the meta-data
		context.setObjectClass(this.accessControlType);
		context.setManagedObjectClass(AccessControlManagedObject.class);

		// Add the dependency
		context.addDependency(Dependencies.AUTHENTICATION_CONTEXT, AuthenticationContext.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new AccessControlManagedObject();
	}

	/**
	 * {@link ManagedObject} for the access control.
	 */
	public class AccessControlManagedObject
			implements AsynchronousManagedObject, CoordinatingManagedObject<Dependencies>, AccessControlListener<AC> {

		/**
		 * {@link AsynchronousContext}.
		 */
		private AsynchronousContext asynchronousContext;

		/**
		 * {@link AuthenticationContext}.
		 */
		private AuthenticationContext<AC, C> authenticationContext;

		/**
		 * Access control.
		 */
		private AC accessControl = null;

		/**
		 * {@link Escalation}.
		 */
		private Throwable escalation = null;

		/*
		 * ================= AccessControlListener ====================
		 */

		@Override
		public void accessControlChange(AC accessControl, Throwable escalation) {
			this.accessControl = accessControl;
			this.escalation = escalation;
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

			// Obtain the authentication context
			this.authenticationContext = (AuthenticationContext<AC, C>) registry
					.getObject(Dependencies.AUTHENTICATION_CONTEXT);

			// Register for the access control
			this.authenticationContext.register(this);

			// Trigger authentication
			this.asynchronousContext.start(null);
			this.authenticationContext.authenticate(null, (failure) -> {
				this.asynchronousContext.complete(null);
			});
		}

		@Override
		public Object getObject() throws Throwable {
			return this.authenticationContext.run(() -> {

				// Propagate any escalation
				if (this.escalation != null) {
					throw this.escalation;
				}

				// Ensure have the access control
				if (this.accessControl == null) {
					throw new AuthenticationRequiredException(AccessControlManagedObjectSource.this.httpSecurityName);
				}

				// Return the access control
				return this.accessControl;
			});
		}
	}

}
