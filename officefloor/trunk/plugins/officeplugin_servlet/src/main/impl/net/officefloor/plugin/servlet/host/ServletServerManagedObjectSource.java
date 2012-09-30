/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.servlet.host;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.log.StdoutLogger;
import net.officefloor.plugin.servlet.resource.ClassPathResourceLocator;
import net.officefloor.plugin.servlet.resource.ResourceLocator;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;

/**
 * {@link ManagedObjectSource} for a {@link ServletServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerManagedObjectSource
		extends
		AbstractManagedObjectSource<ServletServerManagedObjectSource.Dependencies, None> {

	/**
	 * Dependencies.
	 */
	public static enum Dependencies {
		HTTP_APPLICATION_LOCATION
	}

	/**
	 * Property name to specify the server name.
	 */
	public static final String PROPERTY_SERVER_NAME = "server.name";

	/**
	 * Property name to specify the class path prefix for locating resources.
	 */
	public static final String PROPERTY_CLASS_PATH_PREFIX = "class.path.prefix";

	/**
	 * Server name.
	 */
	private String serverName;

	/**
	 * {@link ResourceLocator}.
	 */
	private ResourceLocator resourceLocator;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/*
	 * =========================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVER_NAME, "Server Name");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain servlet server configuration
		this.serverName = mosContext.getProperty(PROPERTY_SERVER_NAME);

		// Create the resource locator
		String classPathPrefix = mosContext.getProperty(
				PROPERTY_CLASS_PATH_PREFIX,
				WebAutoWireApplication.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX);
		ClassLoader classLoader = mosContext.getClassLoader();
		this.resourceLocator = new ClassPathResourceLocator(classPathPrefix,
				classLoader);

		// Create the logger
		this.logger = new StdoutLogger();

		// Specify meta-data
		context.setObjectClass(ServletServer.class);
		context.setManagedObjectClass(ServletServerManagedObject.class);

		// Specify dependencies
		context.addDependency(Dependencies.HTTP_APPLICATION_LOCATION,
				HttpApplicationLocation.class);
	}

	/*
	 * ===================== ManagedObject =============================
	 */

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServletServerManagedObject(this.serverName,
				this.resourceLocator, this.logger);
	}

	/**
	 * {@link ServletServer} {@link ManagedObject}.
	 */
	private static class ServletServerManagedObject implements
			CoordinatingManagedObject<Dependencies>, ServletServer {

		/**
		 * Server name.
		 */
		private final String serverName;

		/**
		 * {@link ResourceLocator}.
		 */
		private final ResourceLocator resourceLocator;

		/**
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * {@link HttpApplicationLocation}.
		 */
		private HttpApplicationLocation location;

		/**
		 * Initiate.
		 * 
		 * @param serverName
		 *            Server name.
		 * @param resourceLocator
		 *            {@link ResourceLocator}.
		 * @param logger
		 *            {@link Logger}.
		 */
		public ServletServerManagedObject(String serverName,
				ResourceLocator resourceLocator, Logger logger) {
			this.serverName = serverName;
			this.resourceLocator = resourceLocator;
			this.logger = logger;
		}

		/*
		 * ================= CoordinatingManagedObject ======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {
			this.location = (HttpApplicationLocation) registry
					.getObject(Dependencies.HTTP_APPLICATION_LOCATION);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ======================= ServletServer ========================
		 */

		@Override
		public String getServerName() {
			return this.serverName;
		}

		@Override
		public int getServerPort() {
			return this.location.getHttpPort();
		}

		@Override
		public String getContextPath() {
			
			// Obtain the context path
			String contextPath = this.location.getContextPath();
			if (contextPath == null) {
				contextPath = "/";
			}
			
			// Return the context path
			return contextPath;
		}

		@Override
		public ResourceLocator getResourceLocator() {
			return this.resourceLocator;
		}

		@Override
		public Logger getLogger() {
			return this.logger;
		}
	}

}