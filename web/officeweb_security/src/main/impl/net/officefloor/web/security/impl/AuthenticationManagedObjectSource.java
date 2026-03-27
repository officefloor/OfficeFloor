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
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedObjectSource} for the authentication object.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class AuthenticationManagedObjectSource<A, AC extends Serializable, C>
		extends AbstractManagedObjectSource<AuthenticationManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		AUTHENTICATION_CONTEXT
	}

	/**
	 * Name of the {@link HttpSecurity}.
	 */
	private final String name;

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<A, AC, C, ?, ?> security;

	/**
	 * {@link HttpSecurityType}.
	 */
	private final HttpSecurityType<A, AC, C, ?, ?> securityType;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of the {@link HttpSecurity}.
	 * @param security
	 *            {@link HttpSecurity}.
	 * @param securityType
	 *            {@link HttpSecurityType}.
	 */
	public AuthenticationManagedObjectSource(String name, HttpSecurity<A, AC, C, ?, ?> security,
			HttpSecurityType<A, AC, C, ?, ?> securityType) {
		this.name = name;
		this.security = security;
		this.securityType = securityType;
	}

	/*
	 * ====================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		context.setObjectClass(this.securityType.getAuthenticationType());
		context.setManagedObjectClass(AuthenticationManagedObject.class);
		context.addDependency(Dependencies.AUTHENTICATION_CONTEXT, AuthenticationContext.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new AuthenticationManagedObject();
	}

	/**
	 * {@link ManagedObject} for the authentication object.
	 */
	private final class AuthenticationManagedObject implements CoordinatingManagedObject<Dependencies> {

		/**
		 * Authentication.
		 */
		private A authentication;

		/*
		 * ====================== ManagedObject ========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the authentication context
			@SuppressWarnings("unchecked")
			AuthenticationContext<AC, C> context = (AuthenticationContext<AC, C>) registry
					.getObject(Dependencies.AUTHENTICATION_CONTEXT);

			// Create the authentication
			this.authentication = AuthenticationManagedObjectSource.this.security.createAuthentication(context);
			if (this.authentication == null) {
				throw new IllegalStateException("Failed to create authentication object from HttpSecurity "
						+ AuthenticationManagedObjectSource.this.name);
			}
		}

		@Override
		public Object getObject() throws Throwable {
			return this.authentication;
		}
	}

}
