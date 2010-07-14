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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.cookie.HttpCookie;
import net.officefloor.plugin.socket.server.http.cookie.HttpCookieUtil;

/**
 * {@link HttpServletRequest} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletRequestImpl implements HttpServletRequest {

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request;

	/**
	 * Cached {@link HttpHeader} instances.
	 */
	private List<HttpHeader> httpHeaders = null;

	/**
	 * Initiate.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 */
	public HttpServletRequestImpl(HttpRequest request) {
		this.request = request;
	}

	/*
	 * ======================== HttpServletRequest ===========================
	 */

	@Override
	public String getAuthType() {
		return null; // consider supporting
	}

	@Override
	public String getContextPath() {
		return "/"; // consider providing context path
	}

	@Override
	public Cookie[] getCookies() {

		// Extract the cookies
		List<HttpCookie> cookies = HttpCookieUtil
				.extractHttpCookies(this.request);

		// Create appropriate listing of cookies
		List<Cookie> list = new LinkedList<Cookie>();
		for (HttpCookie cookie : cookies) {
			list.add(new Cookie(cookie.getName(), cookie.getValue()));
		}

		// Return the cookies
		return list.toArray(new Cookie[0]);
	}

	@Override
	public long getDateHeader(String name) {
		// TODO implement HttpServletRequest.getDateHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getDateHeader");
	}

	@Override
	public String getHeader(String name) {

		// Lazy obtain the HTTP headers
		if (this.httpHeaders == null) {
			this.httpHeaders = this.request.getHeaders();
		}

		// Find the HTTP header
		for (HttpHeader header : this.httpHeaders) {
			if (header.getName().equals(name)) {
				// Found HTTP header so return its value
				return header.getValue();
			}
		}

		// As not found, no header
		return null;
	}

	@Override
	public Enumeration getHeaderNames() {
		// TODO implement HttpServletRequest.getHeaderNames
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getHeaderNames");
	}

	@Override
	public Enumeration getHeaders(String name) {
		// TODO implement HttpServletRequest.getHeaders
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getHeaders");
	}

	@Override
	public int getIntHeader(String name) {
		// TODO implement HttpServletRequest.getIntHeader
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getIntHeader");
	}

	@Override
	public String getMethod() {
		return this.request.getMethod();
	}

	@Override
	public String getPathInfo() {
		// TODO implement HttpServletRequest.getPathInfo
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getPathInfo");
	}

	@Override
	public String getPathTranslated() {
		// TODO implement HttpServletRequest.getPathTranslated
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getPathTranslated");
	}

	@Override
	public String getQueryString() {
		// TODO implement HttpServletRequest.getQueryString
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getQueryString");
	}

	@Override
	public String getRemoteUser() {
		// TODO implement HttpServletRequest.getRemoteUser
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getRemoteUser");
	}

	@Override
	public String getRequestURI() {
		// TODO implement HttpServletRequest.getRequestURI
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getRequestURI");
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO implement HttpServletRequest.getRequestURL
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getRequestURL");
	}

	@Override
	public String getRequestedSessionId() {
		// TODO implement HttpServletRequest.getRequestedSessionId
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getRequestedSessionId");
	}

	@Override
	public String getServletPath() {
		// TODO implement HttpServletRequest.getServletPath
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getServletPath");
	}

	@Override
	public HttpSession getSession() {
		// TODO implement HttpServletRequest.getSession
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getSession");
	}

	@Override
	public HttpSession getSession(boolean create) {
		// TODO implement HttpServletRequest.getSession
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getSession");
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO implement HttpServletRequest.getUserPrincipal
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.getUserPrincipal");
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO implement HttpServletRequest.isRequestedSessionIdFromCookie
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.isRequestedSessionIdFromCookie");
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO implement HttpServletRequest.isRequestedSessionIdFromURL
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.isRequestedSessionIdFromURL");
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO implement HttpServletRequest.isRequestedSessionIdFromUrl
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.isRequestedSessionIdFromUrl");
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO implement HttpServletRequest.isRequestedSessionIdValid
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.isRequestedSessionIdValid");
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO implement HttpServletRequest.isUserInRole
		throw new UnsupportedOperationException(
				"TODO implement HttpServletRequest.isUserInRole");
	}

	@Override
	public Object getAttribute(String name) {
		// TODO implement ServletRequest.getAttribute
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getAttribute");
	}

	@Override
	public Enumeration getAttributeNames() {
		// TODO implement ServletRequest.getAttributeNames
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getAttributeNames");
	}

	@Override
	public String getCharacterEncoding() {
		// TODO implement ServletRequest.getCharacterEncoding
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getCharacterEncoding");
	}

	@Override
	public int getContentLength() {
		// TODO implement ServletRequest.getContentLength
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getContentLength");
	}

	@Override
	public String getContentType() {
		// TODO implement ServletRequest.getContentType
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getContentType");
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO implement ServletRequest.getInputStream
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getInputStream");
	}

	@Override
	public String getLocalAddr() {
		// TODO implement ServletRequest.getLocalAddr
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getLocalAddr");
	}

	@Override
	public String getLocalName() {
		// TODO implement ServletRequest.getLocalName
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getLocalName");
	}

	@Override
	public int getLocalPort() {
		// TODO implement ServletRequest.getLocalPort
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getLocalPort");
	}

	@Override
	public Locale getLocale() {
		// TODO implement ServletRequest.getLocale
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getLocale");
	}

	@Override
	public Enumeration getLocales() {
		// TODO implement ServletRequest.getLocales
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getLocales");
	}

	@Override
	public String getParameter(String name) {
		// TODO implement ServletRequest.getParameter
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getParameter");
	}

	@Override
	public Map getParameterMap() {
		// TODO implement ServletRequest.getParameterMap
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getParameterMap");
	}

	@Override
	public Enumeration getParameterNames() {
		// TODO implement ServletRequest.getParameterNames
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getParameterNames");
	}

	@Override
	public String[] getParameterValues(String name) {
		// TODO implement ServletRequest.getParameterValues
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getParameterValues");
	}

	@Override
	public String getProtocol() {
		// TODO implement ServletRequest.getProtocol
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getProtocol");
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO implement ServletRequest.getReader
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getReader");
	}

	@Override
	public String getRealPath(String path) {
		// TODO implement ServletRequest.getRealPath
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getRealPath");
	}

	@Override
	public String getRemoteAddr() {
		// TODO implement ServletRequest.getRemoteAddr
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getRemoteAddr");
	}

	@Override
	public String getRemoteHost() {
		// TODO implement ServletRequest.getRemoteHost
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getRemoteHost");
	}

	@Override
	public int getRemotePort() {
		// TODO implement ServletRequest.getRemotePort
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getRemotePort");
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO implement ServletRequest.getRequestDispatcher
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getRequestDispatcher");
	}

	@Override
	public String getScheme() {
		// TODO implement ServletRequest.getScheme
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getScheme");
	}

	@Override
	public String getServerName() {
		// TODO implement ServletRequest.getServerName
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getServerName");
	}

	@Override
	public int getServerPort() {
		// TODO implement ServletRequest.getServerPort
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getServerPort");
	}

	@Override
	public boolean isSecure() {
		// TODO implement ServletRequest.isSecure
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.isSecure");
	}

	@Override
	public void removeAttribute(String name) {
		// TODO implement ServletRequest.removeAttribute
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.removeAttribute");
	}

	@Override
	public void setAttribute(String name, Object o) {
		// TODO implement ServletRequest.setAttribute
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.setAttribute");
	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		// TODO implement ServletRequest.setCharacterEncoding
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.setCharacterEncoding");
	}

}