/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.host.source;

import java.io.File;
import java.io.FileNotFoundException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.host.ServletServerImpl;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.log.StdoutLogger;
import net.officefloor.plugin.servlet.resource.FileSystemResourceLocator;
import net.officefloor.plugin.servlet.resource.ResourceLocator;

/**
 * {@link ManagedObjectSource} for a {@link ServletServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * Property name to specify the server name.
	 */
	public static final String PROPERTY_SERVER_NAME = "server.name";

	/**
	 * Property name to specify the server port.
	 */
	public static final String PROPERTY_SERVER_PORT = "server.port";

	/**
	 * Property name to specify the context path.
	 */
	public static final String PROPERTY_CONTEXT_PATH = "context.path";

	/**
	 * Property name to specify the resource path root.
	 */
	public static final String PROPERTY_RESOURCE_PATH_ROOT = "resource.path.root";

	/**
	 * {@link ServletServer}.
	 */
	private ServletServer servletServer;

	/*
	 * =========================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVER_NAME, "Server Name");
		context.addProperty(PROPERTY_SERVER_PORT, "Server Port");
		context.addProperty(PROPERTY_CONTEXT_PATH, "Context Path");
		context.addProperty(PROPERTY_RESOURCE_PATH_ROOT, "Resource Path Root");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain servlet server configuration
		String serverName = mosContext.getProperty(PROPERTY_SERVER_NAME);
		int serverPort = Integer.parseInt(mosContext.getProperty(
				PROPERTY_SERVER_PORT, "80"));
		String contextPath = mosContext.getProperty(PROPERTY_CONTEXT_PATH);

		// Create the resource locator
		File resourcePathRoot = new File(mosContext
				.getProperty(PROPERTY_RESOURCE_PATH_ROOT));
		if (!resourcePathRoot.isDirectory()) {
			throw new FileNotFoundException(
					"Resource path root is not a directory: "
							+ resourcePathRoot.getPath());
		}
		ResourceLocator resourceLocator = new FileSystemResourceLocator(
				resourcePathRoot);

		// Create the logger
		Logger logger = new StdoutLogger();

		// Create the servlet server
		this.servletServer = new ServletServerImpl(serverName, serverPort,
				contextPath, resourceLocator, logger);

		// Specify meta-data
		context.setObjectClass(ServletServer.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================= ManagedObject =============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.servletServer;
	}

}