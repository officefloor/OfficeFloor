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
package net.officefloor.plugin.servlet.context;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import net.officefloor.frame.api.manage.Office;

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
	 * @see ServletContext#getContextPath()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Context path.
	 */
	public String getContextPath(Office office);

	/**
	 * @see ServletContext#getContext(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param uripath
	 *            URI path.
	 * @return {@link ServletContext}.
	 */
	public ServletContext getContext(Office office, String uripath);

	/**
	 * @see ServletContext#getMimeType(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param file
	 *            File name.
	 * @return Mime type.
	 */
	public String getMimeType(Office office, String file);

	/**
	 * @see ServletContext#getResourcePaths(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return Resource paths.
	 */
	@SuppressWarnings("unchecked")
	public Set getResourcePaths(Office office, String path);

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
	public URL getResource(Office office, String path)
			throws MalformedURLException;

	/**
	 * @see ServletContext#getResourceAsStream(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return {@link InputStream}.
	 */
	public InputStream getResourceAsStream(Office office, String path);

	/**
	 * @see ServletContext#getRequestDispatcher(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return {@link RequestDispatcher}.
	 */
	public RequestDispatcher getRequestDispatcher(Office office, String path);

	/**
	 * @see ServletContext#getNamedDispatcher(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 * @return {@link RequestDispatcher}.
	 */
	public RequestDispatcher getNamedDispatcher(Office office, String name);

	/**
	 * @see ServletContext#log(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param msg
	 *            Message.
	 */
	public void log(Office office, String msg);

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
	public void log(Office office, String message, Throwable throwable);

	/**
	 * @see ServletContext#getRealPath(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param path
	 *            Path.
	 * @return Real path.
	 */
	public String getRealPath(Office office, String path);

	/**
	 * @see ServletContext#getServerInfo()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Server information.
	 */
	public String getServerInfo(Office office);

	/**
	 * @see ServletContext#getInitParameter(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 * @return Initialisation parameter value.
	 */
	public String getInitParameter(Office office, String name);

	/**
	 * @see ServletContext#getInitParameterNames()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Initialisation parameter names.
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getInitParameterNames(Office office);

	/**
	 * @see ServletContext#getAttribute(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 * @return Attribute value.
	 */
	public Object getAttribute(Office office, String name);

	/**
	 * @see ServletContext#getAttributeNames()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return Attribute names.
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames(Office office);

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
	public void setAttribute(Office office, String name, Object object);

	/**
	 * @see ServletContext#removeAttribute(String)
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param name
	 *            Name.
	 */
	public void removeAttribute(Office office, String name);

	/**
	 * @see ServletContext#getServletContextName()
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return {@link ServletContext} name.
	 */
	public String getServletContextName(Office office);

}