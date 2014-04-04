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
package net.officefloor.plugin.servlet.container;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getServletNames() {
		throw new UnsupportedOperationException(
				"ServletContext.getServletNames deprecated as of version 2.1");
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getServlets() {
		throw new UnsupportedOperationException(
				"ServletContext.getServlets deprecated as of version 2.0");
	}

	@Override
	public void log(Exception exception, String msg) {
		throw new UnsupportedOperationException(
				"ServletContext.log deprecated as of version 2.1");
	}

	/*
	 * ------------------ Servlet 3.x methods ----------------------
	 */

	@Override
	public Dynamic addFilter(String arg0, String arg1) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Dynamic addFilter(String arg0, Filter arg1) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public void addListener(String arg0) {
		UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public <T extends EventListener> void addListener(T arg0) {
		UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public void addListener(Class<? extends EventListener> arg0) {
		UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			String arg1) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Servlet arg1) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Class<? extends Servlet> arg1) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> arg0)
			throws ServletException {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> arg0)
			throws ServletException {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> arg0)
			throws ServletException {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public void declareRoles(String... arg0) {
		UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public ClassLoader getClassLoader() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public FilterRegistration getFilterRegistration(String arg0) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public ServletRegistration getServletRegistration(String arg0) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public boolean setInitParameter(String arg0, String arg1) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
		UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public String getVirtualServerName() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

}