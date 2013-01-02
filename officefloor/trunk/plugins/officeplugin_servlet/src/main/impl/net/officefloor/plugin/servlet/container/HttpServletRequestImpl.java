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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.cookie.HttpCookie;
import net.officefloor.plugin.web.http.cookie.HttpCookieUtil;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokenHandler;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniser;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;

/**
 * {@link HttpServletRequest} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletRequestImpl implements HttpServletRequest,
		ServletRequestForwarder {

	/**
	 * RFC 1123 header date format.
	 */
	public static final String RFC1123_HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

	/**
	 * RFC 1036 header date format.
	 */
	private static final String RFC1036_HEADER_DATE_FORMAT = "EEEE, dd-MMM-yy HH:mm:ss zzz";

	/**
	 * ANSI C header date format.
	 */
	private static final String ANSI_C_HEADER_DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy";

	/**
	 * Header date formats.
	 */
	private static final String[] HEADER_DATE_FORMATS = {
			RFC1123_HEADER_DATE_FORMAT, RFC1036_HEADER_DATE_FORMAT,
			ANSI_C_HEADER_DATE_FORMAT };

	/**
	 * {@link TimeZone} for date header.
	 */
	private static TimeZone DATE_HEADER_TIMEZONE = TimeZone.getTimeZone("GMT");

	/**
	 * Start {@link Date} for the date header.
	 */
	private static final Date DATE_HEADER_TWO_DIGIT_YEAR_START;

	/**
	 * Initiate date header two digit start year.
	 */
	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, Calendar.JANUARY, 1, 0, 0);
		DATE_HEADER_TWO_DIGIT_YEAR_START = calendar.getTime();
	}

	/**
	 * Name of the {@link HttpHeader} containing the host name.
	 */
	private static final String HEADER_HOST = "Host";

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request;

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity security;

	/**
	 * Name of identifier (e.g. cookie or parameter name) providing the session
	 * Id.
	 */
	private final String sessionIdIdentifierName;

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext servletContext;

	/**
	 * Default {@link Locale} if not specified on {@link HttpRequest}.
	 */
	private final Locale defaultLocale;

	/**
	 * {@link TaskContext}.
	 */
	private final TaskContext<?, ?, ?> taskContext;

	/**
	 * Request URI.
	 */
	private final String requestUri;

	/**
	 * Path.
	 */
	private String path = "";

	/**
	 * {@link HttpParameter} instances for the {@link HttpRequest}.
	 */
	private final List<HttpParameter> httpParameters = new LinkedList<HttpParameter>();

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState attributes;

	/**
	 * Query string.
	 */
	private String queryString = "";

	/**
	 * Cached {@link HttpHeader} instances.
	 */
	private List<HttpHeader> httpHeaders = null;

	/**
	 * Cached {@link HttpParameter} map.
	 */
	private Map<String, String[]> parameterMap = null;

	/**
	 * {@link ServletInputStream} for the {@link HttpRequest} body.
	 */
	private ServletInputStream inputStream = null;

	/**
	 * {@link BufferedReader} for the {@link HttpRequest} body.
	 */
	private BufferedReader reader = null;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param requestAttributes
	 *            {@link HttpRequestState}.
	 * @param security
	 *            {@link HttpSecurity}.
	 * @param sessionIdIdentifierName
	 *            Name of identifier (e.g. cookie or parameter name) providing
	 *            the session Id.
	 * @param session
	 *            {@link HttpSession}.
	 * @param servletContext
	 *            {@link ServletContext}.
	 * @param defaultLocale
	 *            Default {@link Locale} if not specified.
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws IOException
	 *             If fails to read data from the {@link HttpRequest}.
	 * @throws HttpRequestTokeniseException
	 *             If fails to tokenise the {@link HttpRequest}.
	 */
	public HttpServletRequestImpl(ServerHttpConnection connection,
			HttpRequestState requestAttributes, HttpSecurity security,
			String sessionIdIdentifierName, HttpSession session,
			ServletContext servletContext, Locale defaultLocale,
			TaskContext<?, ?, ?> taskContext) throws IOException,
			HttpRequestTokeniseException {

		// Initiate state
		this.connection = connection;
		this.request = this.connection.getHttpRequest();
		this.attributes = requestAttributes;
		this.security = security;
		this.sessionIdIdentifierName = sessionIdIdentifierName;
		this.session = session;
		this.servletContext = servletContext;
		this.defaultLocale = defaultLocale;
		this.taskContext = taskContext;

		// Tokenise the HTTP request
		HttpRequestTokeniser tokeniser = new HttpRequestTokeniserImpl();
		tokeniser.tokeniseHttpRequest(this.request,
				new HttpRequestTokenHandler() {

					@Override
					public void handlePath(String path)
							throws HttpRequestTokeniseException {
						HttpServletRequestImpl.this.path = path;
					}

					@Override
					public void handleHttpParameter(String name, String value)
							throws HttpRequestTokeniseException {
						HttpServletRequestImpl.this.httpParameters
								.add(new HttpParameter(name, value));
					}

					@Override
					public void handleQueryString(String queryString)
							throws HttpRequestTokeniseException {
						HttpServletRequestImpl.this.queryString = queryString;
					}

					@Override
					public void handleFragment(String fragment)
							throws HttpRequestTokeniseException {
						// Ignore fragment
					}
				});

		// Request URI is path with context
		this.requestUri = this.path;

		// Remove context from path (if begins with context)
		String contextPath = this.servletContext.getContextPath();
		if ((!"/".equals(contextPath)) && (this.path.startsWith(contextPath))) {
			this.path = this.path.substring(contextPath.length());
		}
	}

	/**
	 * Obtains the {@link HttpHeader} instances.
	 * 
	 * @return {@link HttpHeader} instances.
	 */
	private List<HttpHeader> getHttpHeaders() {

		// Lazy obtain the HTTP headers
		if (this.httpHeaders == null) {
			this.httpHeaders = this.request.getHeaders();
		}

		// Return the HTTP headers
		return this.httpHeaders;
	}

	/**
	 * Obtains the {@link HttpHeader} value by case insensitive name.
	 * 
	 * @param name
	 *            Case insensitive name of the {@link HttpHeader}.
	 * @return Value for the {@link HttpHeader}. <code>null</code> if can not
	 *         find the {@link HttpHeader}.
	 */
	private String getCaseInsensitiveHttpHeader(String name) {

		// Obtain the HTTP headers
		List<HttpHeader> headers = this.getHttpHeaders();

		// Find case insensitive HTTP header
		for (HttpHeader header : headers) {
			if (header.getName().equalsIgnoreCase(name)) {
				return header.getValue(); // Found header
			}
		}

		// As here not found HTTP header
		return null;
	}

	/*
	 * ====================== ServletRequestForwarder ========================
	 */

	@Override
	public void forward(String workName, String taskName, Object parameter)
			throws ServletException {
		try {
			// Forward
			this.taskContext.doFlow(workName, taskName, parameter);

		} catch (UnknownWorkException ex) {
			throw new ServletException(ex);
		} catch (UnknownTaskException ex) {
			throw new ServletException(ex);
		} catch (InvalidParameterTypeException ex) {
			throw new ServletException(ex);
		}
	}

	/*
	 * ======================== HttpServletRequest ===========================
	 */

	/*
	 * ---------------------- Context Based Methods -----------------------
	 */

	@Override
	public String getContextPath() {
		return this.servletContext.getContextPath();
	}

	/*
	 * ------------------- Path and Body Based Methods ---------------------
	 */

	@Override
	public String getMethod() {
		return this.request.getMethod();
	}

	@Override
	public String getServletPath() {
		return this.path; // always full path
	}

	@Override
	public String getPathInfo() {
		return null; // never path info as always in servlet path
	}

	@Override
	public String getPathTranslated() {
		// Servlet matching has this always null.
		// Not sure of case when this returns value.
		return null;
	}

	@Override
	public String getQueryString() {
		return this.queryString;
	}

	@Override
	public String getRequestURI() {
		return this.requestUri;
	}

	@Override
	public StringBuffer getRequestURL() {

		// Host
		String host = null;

		// Determine if require host (path is relative)
		if (this.requestUri.startsWith("/")) {
			host = this.getCaseInsensitiveHttpHeader(HEADER_HOST);
		}

		// Create and load the request URL
		StringBuffer requestUrl;
		if (host == null) {
			requestUrl = new StringBuffer(this.requestUri);
		} else {
			requestUrl = new StringBuffer(host.length() + this.path.length());
			requestUrl.append(host);
			requestUrl.append(this.requestUri);
		}

		// Return the request URL
		return requestUrl;
	}

	@Override
	public int getContentLength() {

		// Obtain the content length value
		String contentLength = this
				.getCaseInsensitiveHttpHeader("Content-Length");
		if (contentLength == null) {
			return -1; // Unknown value
		}

		// Obtain the content length
		try {
			return Integer.parseInt(contentLength);
		} catch (NumberFormatException ex) {
			return -1; // Unknown value
		}
	}

	@Override
	public String getParameter(String name) {

		// Search the HTTP parameters for the value
		for (HttpParameter parameter : this.httpParameters) {
			if (parameter.name.equals(name)) {
				return parameter.value; // Value found
			}
		}

		// As here, parameter not found
		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getParameterMap() {

		// Lazy load the parameter map
		if (this.parameterMap == null) {

			// Create and load the parameters
			this.parameterMap = new HashMap<String, String[]>();
			for (HttpParameter parameter : this.httpParameters) {
				String[] values = this.parameterMap.get(parameter.name);
				if (values == null) {
					// First value for name
					values = new String[] { parameter.value };
				} else {
					// Additional value for name
					String[] tmp = new String[values.length + 1];
					System.arraycopy(values, 0, tmp, 0, values.length);
					tmp[values.length] = parameter.value;
					values = tmp;
				}
				this.parameterMap.put(parameter.name, values);
			}

			// Make parameter map unmodifiable
			this.parameterMap = Collections.unmodifiableMap(this.parameterMap);
		}

		// Return the parameter map
		return this.parameterMap;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getParameterNames() {

		// Create the unique set of names
		List<String> names = new ArrayList<String>(this.httpParameters.size());
		for (HttpParameter parameter : this.httpParameters) {
			String name = parameter.name;
			if (!names.contains(name)) {
				// Add the unique name
				names.add(name);
			}
		}

		// Return the enumeration
		return new IteratorEnumeration<String>(names.iterator());
	}

	@Override
	@SuppressWarnings("unchecked")
	public String[] getParameterValues(String name) {
		Map<String, String[]> map = this.getParameterMap();
		return map.get(name);
	}

	@Override
	public String getProtocol() {
		return this.request.getVersion();
	}

	@Override
	public BufferedReader getReader() throws IOException {

		// Ensure not already obtained input stream
		if (this.inputStream != null) {
			throw new IllegalStateException(
					"InputStream already provided so can not provide Reader");
		}

		// Lazy create the reader
		if (this.reader == null) {
			this.reader = new BufferedReader(new InputStreamReader(
					this.request.getEntity()));
		}

		// Return the reader
		return this.reader;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {

		// Ensure not already obtained reader
		if (this.reader != null) {
			throw new IllegalStateException(
					"Reader already provided so can not provide InputStream");
		}

		// Lazy create the input stream
		if (this.inputStream == null) {
			this.inputStream = new HttpRequestServletInputStream(
					this.request.getEntity());
		}

		// Return the input stream
		return this.inputStream;
	}

	@Override
	public String getScheme() {
		// Return based on whether secure
		return (this.connection.isSecure() ? "https" : "http");
	}

	/*
	 * ------------------------ Header Based Methods -------------------------
	 */

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
		String value = this.getHeader(name);
		if (value != null) {

			// Have value so attempt to parse
			SimpleDateFormat dateParser = null;
			for (String dateFormat : HEADER_DATE_FORMATS) {

				// Configure the parser
				if (dateParser == null) {
					// Create new data parser
					dateParser = new SimpleDateFormat(dateFormat, Locale.US);
					dateParser.setTimeZone(DATE_HEADER_TIMEZONE);
					dateParser
							.set2DigitYearStart(DATE_HEADER_TWO_DIGIT_YEAR_START);
				} else {
					// Use the next date format
					dateParser.applyPattern(dateFormat);
				}

				// Parse the date
				try {
					long date = dateParser.parse(value).getTime();
					return date;
				} catch (Exception ex) {
					// Ignore parsing failure and try next format
				}
			}

			// Can not parse so throw failure
			throw new IllegalArgumentException(
					"Can not parse header date value '" + value + "'");
		}

		// No value so return as per Servlet API
		return -1;
	}

	@Override
	public String getHeader(String name) {

		// Obtain the headers
		List<HttpHeader> headers = this.getHttpHeaders();

		// Find the HTTP header
		for (HttpHeader header : headers) {
			if (header.getName().equals(name)) {
				// Found HTTP header so return its value
				return header.getValue();
			}
		}

		// As not found, no header
		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getHeaderNames() {

		// Obtain the headers
		List<HttpHeader> headers = this.getHttpHeaders();

		// Create the unique set of headers
		List<String> names = new ArrayList<String>(headers.size());
		for (HttpHeader header : headers) {
			String name = header.getName();
			if (!names.contains(name)) {
				// Add the unique name
				names.add(name);
			}
		}

		// Return the enumeration
		return new IteratorEnumeration<String>(names.iterator());
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getHeaders(String name) {

		// Obtain the headers
		List<HttpHeader> headers = this.getHttpHeaders();

		// Create the listing of values for header name
		List<String> values = new ArrayList<String>(2);
		for (HttpHeader header : headers) {
			String headerName = header.getName();
			if (name.equals(headerName)) {
				// Include the value
				String value = header.getValue();
				values.add(value);
			}
		}

		// Return the enumeration
		return new IteratorEnumeration<String>(values.iterator());
	}

	@Override
	public int getIntHeader(String name) {
		String value = this.getHeader(name);
		if (value != null) {
			// Return the integer value
			return Integer.parseInt(value);
		} else {
			// No value so return as Servlet API
			return -1;
		}
	}

	@Override
	public String getServerName() {

		// Obtain the server name from headers
		String hostHeader = this.getCaseInsensitiveHttpHeader(HEADER_HOST);
		if (hostHeader != null) {
			// Extract host name from header
			String host;
			int separatorIndex = hostHeader.indexOf(':');
			if (separatorIndex >= 0) {
				host = hostHeader.substring(0, separatorIndex);
			} else {
				// No separator so full header value
				host = hostHeader;
			}

			// Send if have host
			host = host.trim();
			if (host.length() > 0) {
				// Return the host name as server name
				return host;
			}
		}

		// As here, did not obtain from header so use connection details
		return this.getLocalAddr();
	}

	@Override
	public int getServerPort() {

		// Attempt to obtain port from headers
		String hostHeader = this.getCaseInsensitiveHttpHeader(HEADER_HOST);
		if (hostHeader != null) {
			// Extract port from header (only if available)
			int separatorIndex = hostHeader.indexOf(':');
			if (separatorIndex >= 0) {
				// Obtain port details (+1 to ignore separator)
				String port = hostHeader.substring(separatorIndex + 1);

				// Translate to integer
				try {
					return Integer.parseInt(port);
				} catch (NumberFormatException ex) {
					// Ignore and carry on to use connection
				}
			}
		}

		// As here, did not obtain from header so use connection details
		return this.getLocalPort();
	}

	/*
	 * --------------------- Session Based Methods ----------------------
	 */

	/**
	 * Flag indicating if the session id attempt to be retrieved from the
	 * {@link HttpRequest}.
	 */
	private boolean isSessionIdLoaded = false;

	/**
	 * Flags if session id obtained from a {@link Cookie}.
	 */
	private boolean isSessionIdFromCookie = false;

	/**
	 * Flags if session id obtained from the URL.
	 */
	private boolean isSessionIdFromUrl = false;

	/**
	 * Session Id from the {@link HttpRequest}.
	 */
	private String requestSessionId;

	/**
	 * Lazy loads the session Id tracking where it was obtained from.
	 */
	private void lazyLoadSessionId() {

		// Determine if already loaded session id
		if (this.isSessionIdLoaded) {
			return; // already loaded
		}

		try {
			// Attempt to obtain session id from cookie
			Cookie[] cookies = this.getCookies();
			for (Cookie cookie : cookies) {
				if (this.sessionIdIdentifierName.equalsIgnoreCase(cookie
						.getName())) {
					// Found session id on cookie
					this.isSessionIdFromCookie = true;
					this.requestSessionId = cookie.getValue();
					return; // found
				}
			}

			// Not on cookie, so try a parameter
			this.requestSessionId = this
					.getParameter(this.sessionIdIdentifierName);
			this.isSessionIdFromUrl = (this.requestSessionId != null);

		} finally {
			// Loaded session Id
			this.isSessionIdLoaded = true;
		}
	}

	@Override
	public String getRequestedSessionId() {
		this.lazyLoadSessionId();
		return this.requestSessionId;
	}

	@Override
	public javax.servlet.http.HttpSession getSession() {
		return this.session;
	}

	@Override
	public javax.servlet.http.HttpSession getSession(boolean create) {
		return this.session;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		this.lazyLoadSessionId();
		return this.isSessionIdFromCookie;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		this.lazyLoadSessionId();
		return this.isSessionIdFromUrl;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		this.lazyLoadSessionId();

		// Determine if have request session id
		if (this.requestSessionId == null) {
			return false; // none provided so not valid
		}

		// Valid if session id's match
		String sessionId = this.session.getId();
		return (sessionId.equals(this.requestSessionId));
	}

	/*
	 * -------------------- Context Based Methods --------------------------
	 */

	@Override
	public Object getAttribute(String name) {

		// Determine if after Servlet Request Forwarder
		if (ServletRequestForwarder.ATTRIBUTE_FORWARDER.equals(name)) {
			return this; // ServletRequestForwarder
		}

		// Obtain the request attribute
		return this.attributes.getAttribute(name);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getAttributeNames() {
		return new IteratorEnumeration<String>(
				this.attributes.getAttributeNames());
	}

	@Override
	public void removeAttribute(String name) {
		this.attributes.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		this.attributes.setAttribute(name, (Serializable) object);
	}

	/*
	 * -------------------- Encoding Based Methods --------------------------
	 */

	@Override
	public String getCharacterEncoding() {
		// TODO implement ServletRequest.getCharacterEncoding
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.getCharacterEncoding");
	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		// TODO implement ServletRequest.setCharacterEncoding
		throw new UnsupportedOperationException(
				"TODO implement ServletRequest.setCharacterEncoding");
	}

	@Override
	public String getContentType() {
		return this.getCaseInsensitiveHttpHeader("Content-Type");
	}

	@Override
	public Locale getLocale() {
		return this.defaultLocale;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getLocales() {
		return new IteratorEnumeration<Locale>(Arrays
				.asList(this.defaultLocale).iterator());
	}

	/*
	 * -------------------- Transport Based Methods --------------------------
	 */

	@Override
	public String getLocalAddr() {
		InetSocketAddress localAddress = this.connection.getLocalAddress();
		return localAddress.getAddress().getHostAddress();
	}

	@Override
	public String getLocalName() {
		InetSocketAddress localAddress = this.connection.getLocalAddress();
		return localAddress.getHostName();
	}

	@Override
	public int getLocalPort() {
		InetSocketAddress localAddress = this.connection.getLocalAddress();
		return localAddress.getPort();
	}

	@Override
	public String getRemoteAddr() {
		InetSocketAddress remoteAddress = this.connection.getRemoteAddress();
		return remoteAddress.getAddress().getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		InetSocketAddress remoteAddress = this.connection.getRemoteAddress();
		return remoteAddress.getHostName();
	}

	@Override
	public int getRemotePort() {
		InetSocketAddress remoteAddress = this.connection.getRemoteAddress();
		return remoteAddress.getPort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {

		// Determine path (absolute or relative)
		String dispatcherPath = (path.startsWith("/") ? path : this.path + "/"
				+ path);

		// Obtain the request dispatcher
		return this.servletContext.getRequestDispatcher(dispatcherPath);
	}

	@Override
	public boolean isSecure() {
		return this.connection.isSecure();
	}

	/*
	 * ---------------------- Security Based Methods -----------------------
	 */

	@Override
	public String getAuthType() {
		// Handle based on whether secured
		return (this.security == null ? null : this.security
				.getAuthenticationScheme());
	}

	@Override
	public Principal getUserPrincipal() {
		// Handle based on whether secured
		return (this.security == null ? null : this.security.getUserPrincipal());
	}

	@Override
	public String getRemoteUser() {
		// Handle based on whether secured
		return (this.security == null ? null : this.security.getRemoteUser());
	}

	@Override
	public boolean isUserInRole(String role) {
		// Handle based on whether secured
		return (this.security == null ? false : this.security
				.isUserInRole(role));
	}

	/*
	 * -------------------- Deprecated Methods --------------------------
	 */

	@Override
	public String getRealPath(String path) {
		throw new UnsupportedOperationException(
				"ServletRequest.getRealPath(path) deprecated as of version 2.1");
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		throw new UnsupportedOperationException(
				"HttpServletRequest.isRequestedSessionIdFromUrl deprecated as of version 2.1");
	}

	/**
	 * HTTP parameter.
	 */
	private static class HttpParameter {

		/**
		 * Name.
		 */
		public final String name;

		/**
		 * Value.
		 */
		public final String value;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		public HttpParameter(String name, String value) {
			this.name = name;
			this.value = value;
		}
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