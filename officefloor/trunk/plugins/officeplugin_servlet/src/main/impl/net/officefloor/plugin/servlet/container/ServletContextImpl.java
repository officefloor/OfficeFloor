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
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * {@link ServletContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContextImpl implements ServletContext {

	/**
	 * {@link OfficeServletContext} to delegate functionality to within the
	 * context of the {@link Office}.
	 */
	private final OfficeServletContext context;

	/**
	 * {@link Office} for context of this {@link ServletContext}.
	 */
	private final Office office;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeServletContext} to delegate functionality to
	 *            within the context of the {@link Office}.
	 * @param office
	 *            {@link Office} for context of this {@link ServletContext}.
	 */
	public ServletContextImpl(OfficeServletContext context, Office office) {
		this.context = context;
		this.office = office;
	}

	/*
	 * ==================== ServletContext ======================
	 */

	@Override
	public String getContextPath() {
		return this.context.getContextPath(this.office);
	}

	@Override
	public ServletContext getContext(String uripath) {
		return this.context.getContext(this.office, uripath);
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
		return this.context.getMimeType(this.office, file);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Set getResourcePaths(String path) {
		return this.context.getResourcePaths(this.office, path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return this.context.getResource(this.office, path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return this.context.getResourceAsStream(this.office, path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return this.context.getRequestDispatcher(this.office, path);
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return this.context.getNamedDispatcher(this.office, name);
	}

	@Override
	public void log(String msg) {
		this.context.log(this.office, msg);
	}

	@Override
	public void log(String message, Throwable throwable) {
		this.context.log(this.office, message, throwable);
	}

	@Override
	public String getRealPath(String path) {
		return this.context.getRealPath(this.office, path);
	}

	@Override
	public String getServerInfo() {
		return this.context.getServerInfo(this.office);
	}

	@Override
	public String getInitParameter(String name) {
		return this.context.getInitParameter(this.office, name);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		return this.context.getInitParameterNames(this.office);
	}

	@Override
	public void setAttribute(String name, Object object) {
		this.context.setAttribute(this.office, name, object);
	}

	@Override
	public Object getAttribute(String name) {
		return this.context.getAttribute(this.office, name);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return this.context.getAttributeNames(this.office);
	}

	@Override
	public void removeAttribute(String name) {
		this.context.removeAttribute(this.office, name);
	}

	@Override
	public String getServletContextName() {
		return this.context.getServletContextName(this.office);
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
	@SuppressWarnings("rawtypes")
	public Enumeration getServletNames() {
		throw new UnsupportedOperationException(
				"ServletContext.getServletNames deprecated as of version 2.1");
	}

	@Override
	@SuppressWarnings("rawtypes")
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