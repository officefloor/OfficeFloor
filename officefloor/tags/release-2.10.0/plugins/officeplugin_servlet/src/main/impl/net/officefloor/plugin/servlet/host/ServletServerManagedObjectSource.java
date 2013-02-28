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
package net.officefloor.plugin.servlet.host;

import java.util.logging.Level;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.log.Logger;
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
		AbstractManagedObjectSource<ServletServerManagedObjectSource.Dependencies, None>
		implements Logger {

	/**
	 * Dependencies.
	 */
	public static enum Dependencies {
		HTTP_APPLICATION_LOCATION
	}

	/**
	 * {@link java.util.logging.Logger}.
	 */
	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ServletServer.class.getName());

	/**
	 * Property name to specify the class path prefix for locating resources.
	 */
	public static final String PROPERTY_CLASS_PATH_PREFIX = "class.path.prefix";

	/**
	 * {@link ResourceLocator}.
	 */
	private ResourceLocator resourceLocator;

	/*
	 * ======================= Logger ======================================
	 */

	@Override
	public void log(String message) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, message);
		}
	}

	@Override
	public void log(String message, Throwable failure) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, message, failure);
		}
	}

	/*
	 * =========================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Create the resource locator
		String classPathPrefix = mosContext.getProperty(
				PROPERTY_CLASS_PATH_PREFIX,
				WebAutoWireApplication.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX);
		ClassLoader classLoader = mosContext.getClassLoader();
		this.resourceLocator = new ClassPathResourceLocator(classPathPrefix,
				classLoader);

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
		return new ServletServerManagedObject(this.resourceLocator, this);
	}

	/**
	 * {@link ServletServer} {@link ManagedObject}.
	 */
	private static class ServletServerManagedObject implements
			CoordinatingManagedObject<Dependencies>, ServletServer {

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
		 * @param resourceLocator
		 *            {@link ResourceLocator}.
		 * @param logger
		 *            {@link Logger}.
		 */
		public ServletServerManagedObject(ResourceLocator resourceLocator,
				Logger logger) {
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
			return this.location.getDomain();
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