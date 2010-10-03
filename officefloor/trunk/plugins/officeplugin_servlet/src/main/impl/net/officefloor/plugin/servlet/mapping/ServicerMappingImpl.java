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
package net.officefloor.plugin.servlet.mapping;

import java.util.Map;

/**
 * {@link ServicerMapping} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServicerMappingImpl implements ServicerMapping {

	/**
	 * {@link Servicer}.
	 */
	private final Servicer servicer;

	/**
	 * {@link Servicer} path.
	 */
	private final String servicerPath;

	/**
	 * Path info.
	 */
	private final String pathInfo;

	/**
	 * Query string.
	 */
	private final String queryString;

	/**
	 * Parameters.
	 */
	private final Map<String, String> parameters;

	/**
	 * Initiate.
	 * 
	 * @param servicer
	 *            {@link Servicer}.
	 * @param servicerPath
	 *            {@link Servicer} Path.
	 * @param pathInfo
	 *            Path info.
	 * @param queryString
	 *            Query string.
	 * @param parameters
	 *            Parameters.
	 */
	public ServicerMappingImpl(Servicer servicer, String servicerPath,
			String pathInfo, String queryString, Map<String, String> parameters) {
		this.servicer = servicer;
		this.servicerPath = servicerPath;
		this.pathInfo = pathInfo;
		this.queryString = queryString;
		this.parameters = parameters;
	}

	/*
	 * ======================= ServicerMapping ======================
	 */

	@Override
	public Servicer getServicer() {
		return this.servicer;
	}

	@Override
	public String getServicerPath() {
		return this.servicerPath;
	}

	@Override
	public String getPathInfo() {
		return this.pathInfo;
	}

	@Override
	public String getQueryString() {
		return this.queryString;
	}

	@Override
	public String getParameter(String name) {
		return this.parameters.get(name);
	}

}