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

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link ManagedObjectSource} for the {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObjectSource extends
		AbstractManagedObjectSource<HttpAuthenticationManagedObjectSource.Dependencies, HttpAuthenticationManagedObjectSource.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_SESSION
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		AUTHENTICATE, LOGOUT
	}

	/**
	 * {@link HttpSecuritySource}.
	 */
	@SuppressWarnings("rawtypes")
	private final HttpSecurity httpSecurity;

	/**
	 * {@link HttpAccessControlFactory}.
	 */
	@SuppressWarnings("rawtypes")
	private final HttpAccessControlFactory httpAccessControlFactory;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 * @param httpAccessControlFactory
	 *            {@link HttpAccessControlFactory}.
	 */
	public HttpAuthenticationManagedObjectSource(HttpSecurity<?, ?, ?, ?, ?> httpSecurity,
			HttpAccessControlFactory<?> httpAccessControlFactory) {
		this.httpSecurity = httpSecurity;
		this.httpAccessControlFactory = httpAccessControlFactory;
	}

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, Flows> context) throws Exception {

		// Provide the meta-data
		context.setObjectClass(HttpAuthentication.class);
		context.setManagedObjectClass(HttpAuthenticationManagedObject.class);
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
		context.addDependency(Dependencies.HTTP_SESSION, HttpSession.class);
		context.addFlow(Flows.AUTHENTICATE, FunctionAuthenticateContext.class);
		context.addFlow(Flows.LOGOUT, FunctionLogoutContext.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
		this.executeContext = context;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpAuthenticationManagedObject(this.httpSecurity, this.httpAccessControlFactory,
				this.executeContext);
	}

}