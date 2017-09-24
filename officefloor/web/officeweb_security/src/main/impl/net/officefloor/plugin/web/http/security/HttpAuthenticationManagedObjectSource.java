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

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.ServerHttpConnection;

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
	private HttpSecuritySource httpSecuritySource;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * {@link HttpSecurityConfiguration}.
	 */
	private final HttpSecurityConfiguration<?, ?, ?, ?> httpSecurityConfiguration;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityConfiguration
	 *            {@link HttpSecurityConfiguration}.
	 */
	public HttpAuthenticationManagedObjectSource(HttpSecurityConfiguration<?, ?, ?, ?> httpSecurityConfiguration) {
		this.httpSecurityConfiguration = httpSecurityConfiguration;
	}

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, Flows> context) throws Exception {

		// Retrieve the HTTP Security Source
		this.httpSecuritySource = this.httpSecurityConfiguration.getHttpSecuritySource();

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
		return new HttpAuthenticationManagedObject(this.httpSecuritySource, this.executeContext);
	}

}