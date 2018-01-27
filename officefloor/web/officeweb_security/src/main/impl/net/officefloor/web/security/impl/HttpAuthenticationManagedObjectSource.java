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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;

/**
 * {@link ManagedObjectSource} for the {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
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