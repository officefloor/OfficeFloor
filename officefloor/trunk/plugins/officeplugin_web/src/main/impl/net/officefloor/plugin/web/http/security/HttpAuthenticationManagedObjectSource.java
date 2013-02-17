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

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link ManagedObjectSource} for the {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObjectSource
		extends
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
	 * Name of {@link Property} providing the key to the
	 * {@link HttpSecuritySource} from the {@link HttpSecurityConfigurator}.
	 */
	public static final String PROPERTY_HTTP_SECURITY_SOURCE_KEY = "http.security.source.key";

	/**
	 * {@link HttpSecuritySource}.
	 */
	@SuppressWarnings("rawtypes")
	private HttpSecuritySource httpSecuritySource;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_HTTP_SECURITY_SOURCE_KEY,
				"HTTP Security Source Key");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, Flows> context)
			throws Exception {
		ManagedObjectSourceContext<Flows> mosContext = context
				.getManagedObjectSourceContext();

		// Retrieve the HTTP Security Source
		String key = mosContext.getProperty(PROPERTY_HTTP_SECURITY_SOURCE_KEY);
		this.httpSecuritySource = HttpSecurityConfigurator
				.getHttpSecuritySource(key).getHttpSecuritySource();

		// Provide the meta-data
		context.setObjectClass(HttpAuthentication.class);
		context.setManagedObjectClass(HttpAuthenticationManagedObject.class);
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class);
		context.addDependency(Dependencies.HTTP_SESSION, HttpSession.class);
		context.addFlow(Flows.AUTHENTICATE, TaskAuthenticateContext.class);
		context.addFlow(Flows.LOGOUT, TaskLogoutContext.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context)
			throws Exception {
		this.executeContext = context;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpAuthenticationManagedObject(this.httpSecuritySource,
				this.executeContext);
	}

}