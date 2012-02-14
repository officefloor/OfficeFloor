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

import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.resource.ResourceLocator;

/**
 * {@link ServletServer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerImpl implements ServletServer {

	/**
	 * Server name.
	 */
	private final String serverName;

	/**
	 * Server port.
	 */
	private final int serverPort;

	/**
	 * Context path.
	 */
	private final String contextPath;

	/**
	 * {@link ResourceLocator}.
	 */
	private final ResourceLocator resourceLocator;

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * Initiate.
	 * 
	 * @param serverName
	 *            Server name.
	 * @param serverPort
	 *            Server port.
	 * @param contextPath
	 *            Context path.
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 * @param logger
	 *            {@link Logger}.
	 */
	public ServletServerImpl(String serverName, int serverPort,
			String contextPath, ResourceLocator resourceLocator, Logger logger) {
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.contextPath = contextPath;
		this.resourceLocator = resourceLocator;
		this.logger = logger;
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
		return this.serverPort;
	}

	@Override
	public String getContextPath() {
		return this.contextPath;
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