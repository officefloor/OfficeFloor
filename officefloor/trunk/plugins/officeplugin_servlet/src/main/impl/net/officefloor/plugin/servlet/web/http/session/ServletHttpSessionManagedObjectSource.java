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
package net.officefloor.plugin.servlet.web.http.session;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.bridge.ServletBridge;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;

/**
 * {@link ManagedObjectSource} providing a {@link HttpSession} implementation
 * backed by a {@link ServletBridge}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpSessionManagedObjectSource
		extends
		AbstractManagedObjectSource<ServletHttpSessionManagedObjectSource.DependencyKeys, None> {

	/**
	 * Dependency keys for the {@link ServletHttpSessionManagedObjectSource}.
	 */
	public static enum DependencyKeys {
		SERVLET_BRIDGE
	}

	/**
	 * {@link Clock}.
	 */
	private static final Clock CLOCK = new Clock() {
		@Override
		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}
	};

	/**
	 * {@link HttpSession} token name.
	 */
	private String sessionTokenName;

	/*
	 * ========================== ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the token name
		this.sessionTokenName = mosContext.getProperty(
				HttpSessionManagedObjectSource.PROPERTY_SESSION_ID_COOKIE_NAME,
				HttpSessionManagedObjectSource.DEFAULT_SESSION_ID_COOKIE_NAME);

		// Load the meta-data
		context.setObjectClass(HttpSession.class);
		context.setManagedObjectClass(ServletHttpSessionManagedObject.class);
		context.addDependency(DependencyKeys.SERVLET_BRIDGE,
				ServletBridge.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServletHttpSessionManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link ServletHttpSession}.
	 */
	public class ServletHttpSessionManagedObject implements
			CoordinatingManagedObject<DependencyKeys> {

		/**
		 * {@link ServletHttpSession}.
		 */
		private ServletHttpSession session;

		/*
		 * ======================= ManagedObject =======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry)
				throws Throwable {

			// Obtain the Servlet bridge
			ServletBridge bridge = (ServletBridge) registry
					.getObject(DependencyKeys.SERVLET_BRIDGE);

			// Create the HTTP session
			this.session = new ServletHttpSession(bridge.getRequest()
					.getSession(), CLOCK,
					ServletHttpSessionManagedObjectSource.this.sessionTokenName);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.session;
		}
	}

}