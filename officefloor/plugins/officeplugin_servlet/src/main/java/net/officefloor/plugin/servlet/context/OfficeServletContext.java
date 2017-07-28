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
package net.officefloor.plugin.servlet.context;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.container.HttpServletContainer;
import net.officefloor.plugin.servlet.filter.FilterChainFactory;
import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * <p>
 * Provides {@link ServletContext} functionality specific to an {@link Office}.
 * <p>
 * All methods defined on this interface come from {@link ServletContext} with
 * the addition of an {@link Office} parameter to identify the context
 * {@link Office}.
 * <p>
 * Deprecated methods will typically be excluded.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeServletContext {

	/**
	 * <p>
	 * Obtains the {@link FilterChainFactory} for the {@link Office}.
	 * <p>
	 * This is a non {@link ServletContext} method and is provided as filtering
	 * is applied across an application. It is expected that the
	 * {@link HttpServletContainer} will make use of this
	 * {@link FilterChainFactory} for filtering {@link Servlet} servicing.
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return {@link FilterChainFactory}.
	 * @throws ServletException
	 *             {@link Filter} instances are lazy loaded and initialising may
	 *             fail for new {@link Filter} instances.
	 */
	FilterChainFactory getFilterChainFactory(Office office)
			throws ServletException;

	/**
	 * <p>
	 * Obtains the {@link ServletFunctionReference} to the {@link Servlet}
	 * {@link ManagedFunction} corresponding the the input path.
	 * <p>
	 * This is a non {@link ServletContext} method and is provided to allow
	 * routing to a {@link Servlet} for servicing the {@link HttpRequest}.
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path to be mapped to a {@link Servlet} {@link ManagedFunction}.
	 * @return {@link ServletFunctionReference} for the path or <code>null</code> if
	 *         path does not map to a {@link Servlet} {@link ManagedFunction}.
	 */
	ServletFunctionReference mapPath(Office office, String path);

	/**
	 * @see ServletContext#getContextPath()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Context path.
	 */
	String getContextPath(Office office);

	/**
	 * @see ServletContext#getContext(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param uripath
	 *            URI path.
	 * @return {@link ServletContext}.
	 */
	ServletContext getContext(Office office, String uripath);

	/**
	 * @see ServletContext#getMimeType(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param file
	 *            File name.
	 * @return Mime type.
	 */
	String getMimeType(Office office, String file);

	/**
	 * @see ServletContext#getResourcePaths(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return Resource paths.
	 */
	@SuppressWarnings("rawtypes")
	Set getResourcePaths(Office office, String path);

	/**
	 * @see ServletContext#getResource(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return {@link URL}.
	 * @throws MalformedURLException
	 *             If malformed {@link URL}.
	 */
	URL getResource(Office office, String path) throws MalformedURLException;

	/**
	 * @see ServletContext#getResourceAsStream(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return {@link InputStream}.
	 */
	InputStream getResourceAsStream(Office office, String path);

	/**
	 * @see ServletContext#getRequestDispatcher(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return {@link RequestDispatcher}.
	 */
	RequestDispatcher getRequestDispatcher(Office office, String path);

	/**
	 * @see ServletContext#getNamedDispatcher(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 * @return {@link RequestDispatcher}.
	 */
	RequestDispatcher getNamedDispatcher(Office office, String name);

	/**
	 * @see ServletContext#log(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param msg
	 *            Message.
	 */
	void log(Office office, String msg);

	/**
	 * @see ServletContext#log(String, Throwable)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param message
	 *            Message.
	 * @param throwable
	 *            Cause.
	 */
	void log(Office office, String message, Throwable throwable);

	/**
	 * @see ServletContext#getRealPath(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return Real path.
	 */
	String getRealPath(Office office, String path);

	/**
	 * @see ServletContext#getServerInfo()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Server information.
	 */
	String getServerInfo(Office office);

	/**
	 * @see ServletContext#getInitParameter(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 * @return Initialisation parameter value.
	 */
	String getInitParameter(Office office, String name);

	/**
	 * @see ServletContext#getInitParameterNames()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Initialisation parameter names.
	 */
	@SuppressWarnings("rawtypes")
	Enumeration getInitParameterNames(Office office);

	/**
	 * @see ServletContext#getAttribute(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 * @return Attribute value.
	 */
	Object getAttribute(Office office, String name);

	/**
	 * @see ServletContext#getAttributeNames()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Attribute names.
	 */
	@SuppressWarnings("rawtypes")
	Enumeration getAttributeNames(Office office);

	/**
	 * @see ServletContext#setAttribute(String, Object)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 * @param object
	 *            Attribute value.
	 */
	void setAttribute(Office office, String name, Object object);

	/**
	 * @see ServletContext#removeAttribute(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 */
	void removeAttribute(Office office, String name);

	/**
	 * @see ServletContext#getServletContextName()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return {@link ServletContext} name.
	 */
	String getServletContextName(Office office);

}