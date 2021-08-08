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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;

/**
 * {@link ManagedObjectSource} for the {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpAuthenticationManagedObjectSource<A, C>
		extends AbstractManagedObjectSource<HttpAuthenticationManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		AUTHENTICATION
	}

	/**
	 * Custom authentication type.
	 */
	private final Class<A> authenticationType;

	/**
	 * {@link HttpAuthenticationFactory}.
	 */
	private final HttpAuthenticationFactory<A, C> httpAuthenticationFactory;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 */
	public HttpAuthenticationManagedObjectSource(HttpSecurityType<A, ?, C, ?, ?> httpSecurityType) {
		this.authenticationType = httpSecurityType.getAuthenticationType();
		this.httpAuthenticationFactory = httpSecurityType.getHttpAuthenticationFactory();
	}

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		context.setObjectClass(HttpAuthentication.class);
		context.setManagedObjectClass(HttpAuthenticationManagedObject.class);
		context.addDependency(Dependencies.AUTHENTICATION, this.authenticationType);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpAuthenticationManagedObject();
	}

	/**
	 * {@link HttpAuthentication} {@link ManagedObject}.
	 */
	private class HttpAuthenticationManagedObject implements CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link HttpAuthentication}.
		 */
		private HttpAuthentication<C> httpAuthentication;

		/*
		 * ====================== ManagedObject =====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the custom authentication
			@SuppressWarnings("unchecked")
			A authentication = (A) registry.getObject(Dependencies.AUTHENTICATION);

			// Adapt the authentication
			this.httpAuthentication = HttpAuthenticationManagedObjectSource.this.httpAuthenticationFactory
					.createHttpAuthentication(authentication);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.httpAuthentication;
		}
	}

}
