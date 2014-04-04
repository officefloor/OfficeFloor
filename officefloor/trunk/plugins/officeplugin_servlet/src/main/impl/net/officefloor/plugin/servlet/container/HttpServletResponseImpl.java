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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.cookie.HttpCookie;
import net.officefloor.plugin.web.http.cookie.HttpCookieUtil;

/**
 * {@link HttpServletResponse} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletResponseImpl implements HttpServletResponse {

	/**
	 * Content-Length header name.
	 */
	private static final String CONTENT_LENGTH = "Content-Length";

	/**
	 * Content-Type header name.
	 */
	private static final String CONTENT_TYPE = "Content-Type";

	/**
	 * Default character encoding.
	 */
	private static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

	/**
	 * {@link Pattern} to extract the {@link Charset} from the Content Type.
	 */
	private static final Pattern EXTRACT_CHARSET_PATTERN;

	/**
	 * {@link DateFormat} for adding a date header.
	 */
	private static final DateFormat DATE_HEADER_FORMATTER;

	/**
	 * Initiate static values.
	 */
	static {
		// Create the pattern to extract charset from content type
		final String extractCharsetRegExp = ".*;\\s*charset\\s*=\\s*([^;]+).*";
		EXTRACT_CHARSET_PATTERN = Pattern.compile(extractCharsetRegExp,
				Pattern.CASE_INSENSITIVE);

		// Create the date header formatter
		DATE_HEADER_FORMATTER = new SimpleDateFormat(
				HttpServletRequestImpl.RFC1123_HEADER_DATE_FORMAT);
		DATE_HEADER_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response;

	/**
	 * {@link HttpResponseServletOutputStream}.
	 */
	private final HttpResponseServletOutputStream outputStream;

	/**
	 * {@link Clock}.
	 */
	private final Clock clock;

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest request;

	/**
	 * {@link Locale}.
	 */
	private Locale locale;

	/**
	 * Flag indicating if the {@link ServletOutputStream} has been retrieved.
	 */
	private boolean isOutputStreamRetrieved = false;

	/**
	 * Flag indicating if the {@link PrintWriter} has been retrieved.
	 */
	private boolean isPrintWriterRetrieved = false;

	/**
	 * Character encoding for the {@link HttpResponse}.
	 */
	private Charset characterEncoding = Charset
			.forName(DEFAULT_CHARACTER_ENCODING);

	/**
	 * Initiate.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @param clock
	 *            {@link Clock}.
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param locale
	 *            {@link Locale} for the {@link HttpResponse}.
	 * @throws IOException
	 *             If fails to create.
	 */
	public HttpServletResponseImpl(HttpResponse response, Clock clock,
			HttpServletRequest request, Locale locale) throws IOException {
		this.response = response;
		this.outputStream = new HttpResponseServletOutputStream(
				this.response.getEntity());
		this.clock = clock;
		this.request = request;
		this.locale = locale;
	}

	/**
	 * Flushes the buffers. Typically this is invoked after servicing the
	 * {@link HttpRequest}.
	 * 
	 * @throws IOException
	 *             If fails to flush the buffers.
	 */
	void flushBuffers() throws IOException {
		this.outputStream.flush();
	}

	/*
	 * ======================= HttpServletResponse =======================
	 */

	@Override
	public void setStatus(int sc) {
		this.response.setStatus(sc);
	}

	/*
	 * ---------------------- response body methods ----------------------
	 */

	@Override
	public void setBufferSize(int size) {
		try {
			this.outputStream.setBufferSize(size);
		} catch (IOException ex) {
			// Failed reducing buffer size
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public int getBufferSize() {
		return this.outputStream.getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		this.outputStream.flush();
	}

	@Override
	public void resetBuffer() {
		this.outputStream.reset();
	}

	@Override
	public boolean isCommitted() {
		return this.outputStream.isCommitted();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {

		// Ensure print writer not already retrieved
		if (this.isPrintWriterRetrieved) {
			throw new IllegalStateException(
					"PrintWriter has already been retrieved");
		}

		// Flag output stream retrieved
		this.isOutputStreamRetrieved = true;

		// Return the output stream
		return this.outputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {

		// Ensure output stream not already retrieved
		if (this.isOutputStreamRetrieved) {
			throw new IllegalStateException(
					"OutputStream has already been retrieved");
		}

		// Flag print writer retrieved
		this.isPrintWriterRetrieved = true;

		// Return the print writer (that auto flushes)
		Writer writer = new OutputStreamWriter(this.outputStream,
				this.characterEncoding);
		return new HttpResponsePrintWriter(writer, this.outputStream);
	}

	/*
	 * -------------------- header methods ----------------------------
	 */

	@Override
	public void reset() {
		// Reset the body
		this.resetBuffer();

		// Remove the headers
		for (HttpHeader header : this.response.getHeaders()) {
			this.response.removeHeader(header);
		}
	}

	@Override
	public void setContentLength(int len) {
		this.response.removeHeaders(CONTENT_LENGTH);
		this.response.addHeader(CONTENT_LENGTH, String.valueOf(len));
	}

	@Override
	public void setContentType(String type) {

		// Ensure print writer not already retrieved
		if (this.isPrintWriterRetrieved) {
			throw new IllegalStateException(
					"PrintWriter has already been retrieved");
		}

		// Specifies the content type
		this.response.removeHeaders(CONTENT_TYPE);
		this.response.addHeader(CONTENT_TYPE, type);

		// Strip out the charset
		Matcher matcher = EXTRACT_CHARSET_PATTERN.matcher(type);
		if (matcher.matches()) {
			// Have charset so load to character encoding
			String charsetName = matcher.group(1);
			this.setCharacterEncoding(charsetName.trim());
		}
	}

	@Override
	public String getContentType() {
		HttpHeader header = this.response.getHeader(CONTENT_TYPE);
		return (header == null ? null : header.getValue());
	}

	@Override
	public void setCharacterEncoding(String charset) {

		// Ensure print writer not already retrieved
		if (this.isPrintWriterRetrieved) {
			throw new IllegalStateException(
					"PrintWriter has already been retrieved");
		}

		// Specify the charset
		this.characterEncoding = Charset.forName(charset);
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding.displayName();
	}

	@Override
	public void addCookie(Cookie cookie) {

		// Create the HTTP Cookie
		HttpCookie httpCookie = new HttpCookie(cookie.getName(),
				cookie.getValue());

		// Load domain (if provided)
		String domain = cookie.getDomain();
		if (domain != null) {
			httpCookie.setDomain(domain);
		}

		// Load expire time (if provided)
		int maxAge = cookie.getMaxAge();
		if (maxAge > 0) {
			long expireTime = this.clock.currentTimeMillis() + maxAge;
			httpCookie.setExpires(expireTime);
		}

		// Add the Cookie
		HttpCookieUtil.addHttpCookie(httpCookie, this.response);
	}

	@Override
	public boolean containsHeader(String name) {

		// Determine if contains header
		for (HttpHeader header : this.response.getHeaders()) {
			if (header.getName().equals(name)) {
				// Found header, so contains header
				return true;
			}
		}

		// As here did not find header so does not contain header
		return false;
	}

	@Override
	public void addHeader(String name, String value) {
		this.response.addHeader(name, value);
	}

	@Override
	public void setHeader(String name, String value) {
		this.response.removeHeaders(name);
		this.addHeader(name, value);
	}

	@Override
	public void addDateHeader(String name, long date) {
		this.response.addHeader(name,
				DATE_HEADER_FORMATTER.format(new Date(date)));
	}

	@Override
	public void setDateHeader(String name, long date) {
		this.response.removeHeaders(name);
		this.addDateHeader(name, date);
	}

	@Override
	public void addIntHeader(String name, int value) {
		this.response.addHeader(name, String.valueOf(value));
	}

	@Override
	public void setIntHeader(String name, int value) {
		this.response.removeHeaders(name);
		this.addIntHeader(name, value);
	}

	/*
	 * ---------------------- encode URL methods ----------------------
	 */

	@Override
	public String encodeURL(String url) {
		return url; // Session ID always on cookie
	}

	@Override
	public String encodeRedirectURL(String url) {
		return url; // Session ID always on cookie
	}

	/*
	 * ---------------------- send methods ----------------------
	 */

	@Override
	public void sendError(int sc, String msg) throws IOException {

		// Specify error status
		this.response.setStatus(sc, msg);

		// Write body with message
		PrintWriter writer = this.getWriter();
		writer.print("<html><body>");
		writer.print(msg);
		writer.write("</body></html>");
		writer.flush();

		// Trigger sending the response
		this.response.send();
	}

	@Override
	public void sendError(int sc) throws IOException {
		this.response.setStatus(sc);
		this.response.send();
	}

	@Override
	public void sendRedirect(String location) throws IOException {

		// Determine the redirect URL
		String redirectUrl = this.request.getScheme()
				+ "://"
				+ this.request.getServerName()
				+ ":"
				+ this.request.getServerPort()
				+ this.request.getContextPath()
				+ (location.startsWith("/") ? location : this.request
						.getServletPath() + "/" + location);

		// Specify details of redirect
		this.response.setStatus(HttpStatus.SC_TEMPORARY_REDIRECT);
		this.response.addHeader("Location", redirectUrl);

		// Send the redirect
		this.response.send();
	}

	/*
	 * ---------------------- locale methods ----------------------
	 */

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public void setLocale(Locale loc) {
		this.locale = loc;
	}

	/*
	 * ---------------------- deprecated methods ----------------------
	 */

	@Override
	public String encodeRedirectUrl(String url) {
		throw new UnsupportedOperationException(
				"HttpServletResponse.encodeRedirectUrl deprecated as of version 2.1");
	}

	@Override
	public String encodeUrl(String url) {
		throw new UnsupportedOperationException(
				"HttpServletResponse.encodeUrl deprecated as of version 2.1");
	}

	@Override
	public void setStatus(int sc, String sm) {
		throw new UnsupportedOperationException(
				"HttpServletResponse.setStatus deprecated as of version 2.1");
	}

	/*
	 * ------------------ Servlet 3.x methods ----------------------
	 */

	@Override
	public String getHeader(String arg0) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Collection<String> getHeaderNames() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public Collection<String> getHeaders(String arg0) {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public int getStatus() {
		return UnsupportedHttpServletMethodException.notSupported();
	}

	@Override
	public void setContentLengthLong(long len) {
		UnsupportedHttpServletMethodException.notSupported();
	}

}