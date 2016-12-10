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
package net.officefloor.plugin.servlet.bridge.spi;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Servicer for bridging the servicing of the {@link HttpServletRequest} within
 * the {@link Servlet} container by an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletServiceBridger<S> {

	/**
	 * Obtains the instance identifier for this {@link ServletServiceBridger}.
	 * 
	 * @return Instance identifier for this {@link ServletServiceBridger}.
	 */
	String getInstanceIdentifier();

	/**
	 * Indicates whether to use the {@link AsyncContext}.
	 * 
	 * @return <code>true</code> to use the {@link AsyncContext}.
	 */
	boolean isUseAsyncContext();

	/**
	 * Obtains the object types for the {@link Servlet}.
	 * 
	 * @return Object types for the {@link Servlet}.
	 */
	Class<?>[] getObjectTypes();

	/**
	 * Services the {@link Servlet}.
	 * 
	 * @param servlet
	 *            {@link Servlet}.
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param response
	 *            {@link HttpServletResponse}.
	 * @param context
	 *            {@link ServletContext}.
	 * @throws IOException
	 *             As per {@link Servlet} API.
	 * @throws ServletException
	 *             As per {@link Servlet} API.
	 */
	void service(S servlet, HttpServletRequest request,
			HttpServletResponse response, ServletContext context)
			throws IOException, ServletException;

}