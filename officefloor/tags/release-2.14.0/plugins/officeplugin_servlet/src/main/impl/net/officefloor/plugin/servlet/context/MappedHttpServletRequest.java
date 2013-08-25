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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import net.officefloor.plugin.servlet.container.IteratorEnumeration;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;

/**
 * {@link RequestDispatcher} {@link HttpServletRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class MappedHttpServletRequest implements HttpServletRequest {

	/**
	 * {@link ServicerMapping}.
	 */
	private final ServicerMapping mapping;

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest delegate;

	/**
	 * Initiate.
	 * 
	 * @param mapping
	 *            {@link ServicerMapping}.
	 * @param delegate
	 *            {@link HttpServletRequest}.
	 */
	public MappedHttpServletRequest(ServicerMapping mapping,
			HttpServletRequest delegate) {
		this.mapping = mapping;
		this.delegate = delegate;
	}

	/*
	 * ======================= HttpServletRequest =========================
	 */

	@Override
	public String getServletPath() {
		return this.mapping.getServletPath();
	}

	@Override
	public String getPathInfo() {
		return this.mapping.getPathInfo();
	}

	@Override
	public String getQueryString() {
		String queryString = this.mapping.getQueryString();
		return (queryString == null ? this.delegate.getQueryString()
				: queryString);
	}

	@Override
	public String getParameter(String name) {
		String value = this.mapping.getParameter(name);
		return (value == null ? this.delegate.getParameter(name) : value);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Enumeration getParameterNames() {

		// Create the set of names
		Set<String> names = new HashSet<String>();

		// Load the mapper names
		Enumeration<String> mapperNames = this.mapping.getParameterNames();
		while (mapperNames.hasMoreElements()) {
			names.add(mapperNames.nextElement());
		}

		// Load the delegate names
		Enumeration<String> delegateNames = this.delegate.getParameterNames();
		while (delegateNames.hasMoreElements()) {
			names.add(delegateNames.nextElement());
		}

		// Return the enumeration over names
		return new IteratorEnumeration<String>(names.iterator());
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = this.mapping.getParameterValues(name);
		return (values == null ? this.delegate.getParameterValues(name)
				: values);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getParameterMap() {

		// Create the map to be loaded
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();

		// Load the delegate first (to be overwritten by mapping)
		parameterMap.putAll(this.delegate.getParameterMap());

		// Override with mapping parameters
		parameterMap.putAll(this.mapping.getParameterMap());

		// Return unmodifiable parameter map
		return Collections.unmodifiableMap(parameterMap);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {

		// Determine path (absolute or relative)
		String dispatcherPath = (path.startsWith("/") ? path : this.mapping
				.getServletPath() + "/" + path);

		// Obtain and return the request dispatcher
		return this.delegate.getRequestDispatcher(dispatcherPath);
	}

	/*
	 * --------------------- Delegate Methods ----------------------------
	 */

	/*
	 * ---------------------- Context Based Methods -----------------------
	 */

	@Override
	public String getContextPath() {
		return this.delegate.getContextPath();
	}

	@Override
	public String getMethod() {
		return this.delegate.getMethod();
	}

	@Override
	public String getPathTranslated() {
		return this.delegate.getPathTranslated();
	}

	@Override
	public String getRequestURI() {
		return this.delegate.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return this.delegate.getRequestURL();
	}

	@Override
	public int getContentLength() {
		return this.delegate.getContentLength();
	}

	@Override
	public String getProtocol() {
		return this.delegate.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return this.delegate.getReader();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return this.delegate.getInputStream();
	}

	@Override
	public String getScheme() {
		return this.delegate.getScheme();
	}

	/*
	 * ------------------------ Header Based Methods -------------------------
	 */

	@Override
	public Cookie[] getCookies() {
		return this.delegate.getCookies();
	}

	@Override
	public long getDateHeader(String name) {
		return this.delegate.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return this.delegate.getHeader(name);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getHeaderNames() {
		return this.delegate.getHeaderNames();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getHeaders(String name) {
		return this.delegate.getHeaders(name);
	}

	@Override
	public int getIntHeader(String name) {
		return this.delegate.getIntHeader(name);
	}

	@Override
	public String getServerName() {
		return this.delegate.getServerName();
	}

	@Override
	public int getServerPort() {
		return this.delegate.getServerPort();
	}

	/*
	 * --------------------- Session Based Methods ----------------------
	 */

	@Override
	public String getRequestedSessionId() {
		return this.delegate.getRequestedSessionId();
	}

	@Override
	public HttpSession getSession() {
		return this.delegate.getSession();
	}

	@Override
	public HttpSession getSession(boolean create) {
		return this.delegate.getSession(create);
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return this.delegate.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return this.delegate.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return this.delegate.isRequestedSessionIdValid();
	}

	/*
	 * -------------------- Context Based Methods --------------------------
	 */

	@Override
	public Object getAttribute(String name) {
		return this.delegate.getAttribute(name);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getAttributeNames() {
		return this.delegate.getAttributeNames();
	}

	@Override
	public void removeAttribute(String name) {
		this.delegate.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		this.delegate.setAttribute(name, object);
	}

	/*
	 * -------------------- Encoding Based Methods --------------------------
	 */

	@Override
	public String getCharacterEncoding() {
		return this.delegate.getCharacterEncoding();
	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		this.delegate.setCharacterEncoding(env);
	}

	@Override
	public String getContentType() {
		return this.delegate.getContentType();
	}

	@Override
	public Locale getLocale() {
		return this.delegate.getLocale();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getLocales() {
		return this.delegate.getLocales();
	}

	/*
	 * -------------------- Transport Based Methods --------------------------
	 */

	@Override
	public String getLocalAddr() {
		return this.delegate.getLocalAddr();
	}

	@Override
	public String getLocalName() {
		return this.delegate.getLocalName();
	}

	@Override
	public int getLocalPort() {
		return this.delegate.getLocalPort();
	}

	@Override
	public String getRemoteAddr() {
		return this.delegate.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return this.delegate.getRemoteHost();
	}

	@Override
	public int getRemotePort() {
		return this.delegate.getRemotePort();
	}

	@Override
	public boolean isSecure() {
		return this.delegate.isSecure();
	}

	/*
	 * ---------------------- Security Based Methods -----------------------
	 */

	@Override
	public String getAuthType() {
		return this.delegate.getAuthType();
	}

	@Override
	public Principal getUserPrincipal() {
		return this.delegate.getUserPrincipal();
	}

	@Override
	public String getRemoteUser() {
		return this.delegate.getRemoteUser();
	}

	@Override
	public boolean isUserInRole(String role) {
		return this.delegate.isUserInRole(role);
	}

	/*
	 * -------------------- Deprecated Methods --------------------------
	 */

	@Override
	@SuppressWarnings("deprecation")
	public String getRealPath(String path) {
		return this.delegate.getRealPath(path);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isRequestedSessionIdFromUrl() {
		return this.delegate.isRequestedSessionIdFromUrl();
	}

	/*
	 * ------------------ Servlet 3.0 methods ----------------------
	 */

	@Override
	public AsyncContext getAsyncContext() {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public ServletContext getServletContext() {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

	@Override
	public void logout() throws ServletException {
		// TODO support Servlet 3.0 specification
		throw new UnsupportedOperationException(
				"TODO WoOF to support Servlet 3.0 specification");
	}

}