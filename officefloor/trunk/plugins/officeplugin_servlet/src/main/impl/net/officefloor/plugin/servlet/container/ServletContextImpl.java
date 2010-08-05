/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.officefloor.plugin.servlet.dispatch.RequestDispatcherFactory;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.resource.ResourceLocator;

/**
 * {@link ServletContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContextImpl implements ServletContext {

	/**
	 * {@link ServletContext} name.
	 */
	private final String servletContextName;

	/**
	 * Context path.
	 */
	private final String contextPath;

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters;

	/**
	 * {@link ContextAttributes}.
	 */
	private final ContextAttributes attributes;

	/**
	 * Mapping of file extension to MIME type.
	 */
	private final Map<String, String> fileExtensionToMimeType;

	/**
	 * {@link ResourceLocator}.
	 */
	private final ResourceLocator resourceLocator;

	/**
	 * {@link RequestDispatcherFactory}.
	 */
	private final RequestDispatcherFactory requestDispatcherFactory;

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * Real path prefix.
	 */
	private final String realPathPrefix;

	/**
	 * Initiate.
	 * 
	 * @param serverName
	 *            Server name.
	 * @param serverPort
	 *            Server port.
	 * @param servletContextName
	 *            {@link ServletContext} name.
	 * @param contextPath
	 *            Context path.
	 * @param initParameters
	 *            Init parameters.
	 * @param attributes
	 *            {@link ContextAttributes}.
	 * @param fileExtensionToMimeType
	 *            Mapping of file extension to MIME type.
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 * @param requestDispatcherFactory
	 *            {@link RequestDispatcherFactory}.
	 * @param logger
	 *            {@link Logger}.
	 */
	public ServletContextImpl(String serverName, int serverPort,
			String servletContextName, String contextPath,
			Map<String, String> initParameters, ContextAttributes attributes,
			Map<String, String> fileExtensionToMimeType,
			ResourceLocator resourceLocator,
			RequestDispatcherFactory requestDispatcherFactory, Logger logger) {
		this.servletContextName = servletContextName;
		this.initParameters = initParameters;
		this.attributes = attributes;
		this.contextPath = contextPath;
		this.fileExtensionToMimeType = fileExtensionToMimeType;
		this.resourceLocator = resourceLocator;
		this.requestDispatcherFactory = requestDispatcherFactory;
		this.logger = logger;

		// Create the real path prefix
		this.realPathPrefix = "http://" + serverName
				+ (serverPort == 80 ? "" : ":" + serverPort);
	}

	/*
	 * ==================== ServletContext ======================
	 */

	@Override
	public String getContextPath() {
		return this.contextPath;
	}

	@Override
	public ServletContext getContext(String uripath) {
		// Do not allow access to other contexts
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public int getMinorVersion() {
		return 5;
	}

	@Override
	public String getMimeType(String file) {

		// Determine if file extension
		int extensionIndex = file.lastIndexOf('.');
		if (extensionIndex < 0) {
			// No file extension, so no MIME type
			return null;
		}

		// Obtain the file extension (+1 to ignore separator)
		String fileExtension = file.substring(extensionIndex + 1);

		// Obtain the MIME type for file extension
		String mimeType = this.fileExtensionToMimeType.get(fileExtension
				.toLowerCase());

		// Return the MIME type
		return mimeType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set getResourcePaths(String path) {
		return this.resourceLocator.getResourceChildren(path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return this.resourceLocator.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return this.resourceLocator.getResourceAsStream(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return this.requestDispatcherFactory.createRequestDispatcher(path);
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return this.requestDispatcherFactory.createNamedDispatcher(name);
	}

	@Override
	public void log(String msg) {
		this.logger.log(msg);
	}

	@Override
	public void log(String message, Throwable throwable) {
		this.logger.log(message, throwable);
	}

	@Override
	public String getRealPath(String path) {

		// Obtain the path to context
		String realPath = this.realPathPrefix + this.contextPath;

		// Add path
		realPath = realPath + (path.startsWith("/") ? path : "/" + path);

		// Return the real path
		return realPath;
	}

	@Override
	public String getServerInfo() {
		return "OfficeFloor servlet plug-in/1.0";
	}

	@Override
	public String getInitParameter(String name) {
		return this.initParameters.get(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getInitParameterNames() {
		return new IteratorEnumeration<String>(this.initParameters.keySet()
				.iterator());
	}

	@Override
	public void setAttribute(String name, Object object) {
		this.attributes.setAttribute(name, object);
	}

	@Override
	public Object getAttribute(String name) {
		return this.attributes.getAttribute(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return new IteratorEnumeration<String>(this.attributes
				.getAttributeNames());
	}

	@Override
	public void removeAttribute(String name) {
		this.attributes.removeAttribute(name);
	}

	@Override
	public String getServletContextName() {
		return this.servletContextName;
	}

	/*
	 * ------------------ deprecated methods ----------------------
	 */

	@Override
	public Servlet getServlet(String name) throws ServletException {
		throw new UnsupportedOperationException(
				"ServletContext.getServlet deprecated as of version 2.1");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getServletNames() {
		throw new UnsupportedOperationException(
				"ServletContext.getServletNames deprecated as of version 2.1");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration getServlets() {
		throw new UnsupportedOperationException(
				"ServletContext.getServlets deprecated as of version 2.0");
	}

	@Override
	public void log(Exception exception, String msg) {
		throw new UnsupportedOperationException(
				"ServletContext.log deprecated as of version 2.1");
	}

}