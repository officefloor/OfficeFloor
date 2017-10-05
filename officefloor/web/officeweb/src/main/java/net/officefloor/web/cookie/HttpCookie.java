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
package net.officefloor.web.cookie;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;

/**
 * HTTP Cookie.
 *
 * @author Daniel Sagenschneider
 */
public class HttpCookie {

	/**
	 * {@link HttpHeaderName} for loading a cookie.
	 */
	public static final String COOKIE = "cookie";

	/**
	 * Header name specifying a {@link HttpCookie} on the {@link HttpResponse}.
	 */
	public static final HttpHeaderName SET_COOKIE = new HttpHeaderName("set-cookie");

	/**
	 * Format of the expire time for the {@link HttpHeader}.
	 */
	private final static String EXPIRE_FORMAT = "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'";

	/**
	 * {@link DateFormat} to format the expire time for the {@link HttpHeader}.
	 */
	private final static DateFormat expireFormatter;

	/**
	 * Initialise the formatter.
	 */
	static {
		// Create the formatter
		expireFormatter = new SimpleDateFormat(EXPIRE_FORMAT);
		expireFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Extracts all the {@link HttpCookie} instances from the
	 * {@link HttpRequest}.
	 *
	 * @param request
	 *            {@link HttpRequest}.
	 * @return {@link HttpCookie} instances on the {@link HttpRequest}.
	 */
	public static List<HttpCookie> extractHttpCookies(HttpRequest request) {
		List<HttpCookie> cookies = new LinkedList<HttpCookie>();
		for (HttpHeader header : request.getHttpHeaders().getHeaders(COOKIE)) {
			cookies.addAll(HttpCookie.parse(header.getValue()));
		}
		return cookies;
	}

	/**
	 * Extracts a specific {@link HttpCookie} from the {@link HttpRequest}.
	 *
	 * @param cookieName
	 *            Name of the {@link HttpCookie} to retrieve from the
	 *            {@link HttpRequest}.
	 * @param request
	 *            {@link HttpRequest}.
	 * @return {@link HttpCookie} extracted from the {@link HttpRequest} or
	 *         <code>null</code> if no {@link HttpCookie} by the name on the
	 *         {@link HttpRequest}.
	 */
	public static HttpCookie extractHttpCookie(String cookieName, HttpRequest request) {
		// Search for the cookie by the name
		for (HttpHeader header : request.getHttpHeaders().getHeaders(COOKIE)) {
			List<HttpCookie> cookies = HttpCookie.parse(header.getValue());
			for (HttpCookie cookie : cookies) {
				if (cookieName.equalsIgnoreCase(cookie.getName())) {
					// Found the cookie
					return cookie;
				}
			}
		}

		// As here, cookie not found
		return null;
	}

	/**
	 * Adds the {@link HttpCookie} to the {@link HttpResponse}.
	 *
	 * @param cookie
	 *            {@link HttpCookie} to be added.
	 * @param response
	 *            {@link HttpResponse} to have the {@link HttpCookie} added to
	 *            it as a {@link HttpHeader}.
	 * @return {@link HttpHeader} added to the {@link HttpResponse} containing
	 *         the {@link HttpCookie}.
	 */
	public static HttpHeader addHttpCookie(HttpCookie cookie, HttpResponse response) {

		// Obtain the value prefix of the cookie
		String cookieValuePrefix = cookie.getName().toLowerCase() + "=";

		// Obtain the headers
		HttpResponseHeaders headers = response.getHttpHeaders();

		// Remove any cookies by the name
		Iterator<HttpHeader> iterator = headers.getHeaders(SET_COOKIE.getName()).iterator();
		while (iterator.hasNext()) {
			HttpHeader header = iterator.next();
			
			// Determine if cookie by name
			if (header.getValue().toLowerCase().startsWith(cookieValuePrefix)) {
				// Remove the header containing cookie by same name
				iterator.remove();
			}
		}

		// Add the header for the cookie
		HttpHeader header = headers.addHeader(SET_COOKIE, cookie.toHttpResponseHeaderValue());

		// Return the header
		return header;
	}

	/**
	 * Parses the {@link HttpCookie} instances from the {@link HttpHeader}
	 * value.
	 *
	 * @param httpHeaderValue
	 *            Value of the {@link HttpHeader}.
	 * @return Listing of {@link HttpCookie} instances.
	 */
	private static List<HttpCookie> parse(String httpHeaderValue) {

		// Split to possible obtain multiple cookies
		List<String> cookieTexts = splitQuotedText(httpHeaderValue, '"', ',');

		// Create the listing of HTTP Cookies
		List<HttpCookie> cookies = new LinkedList<HttpCookie>();
		for (String cookieText : cookieTexts) {

			// Split out the attributes and name/value
			List<String> attributeTexts = splitQuotedText(cookieText, '"', ';');

			// Look for the cookie name/value
			for (String attributeText : attributeTexts) {

				// Ignore attributes
				if (attributeText.trim().startsWith("$")) {
					continue;
				}

				// Not attribute so must be name/value
				int splitIndex = attributeText.indexOf('=');
				if (splitIndex < 0) {
					// Not name/value
					continue;
				}

				// Obtain the name/values (+1 to ignore '=')
				String name = attributeText.substring(0, splitIndex);
				name = name.trim();
				String value = attributeText.substring(splitIndex + 1);
				value = value.trim();

				// Remove possible quotes around value
				if (value.startsWith("\"")) {
					int lastQuoteIndex = value.lastIndexOf('"');
					if (lastQuoteIndex > 0) {
						// Remove surrounding quotes
						value = value.substring(1, lastQuoteIndex);
					}
				}

				// Add the cookie
				cookies.add(new HttpCookie(name, value));
			}
		}

		// Return the cookies
		return cookies;
	}

	/**
	 * Splits the quoted text by the split character.
	 *
	 * @param quotedText
	 *            Quoted text.
	 * @param quoteCharacter
	 *            Quote character.
	 * @param splitCharacter
	 *            Split character.
	 * @return Listing of split text.
	 */
	private static List<String> splitQuotedText(String quotedText, char quoteCharacter, char splitCharacter) {

		// Create listing of cookies in header value
		List<String> cookies = new LinkedList<String>();

		// Iterate over value splitting out each cookie text
		int numberOfQuotes = 0;
		int splitPos = 0;
		for (int i = 0; i < quotedText.length(); i++) {
			char character = quotedText.charAt(i);

			// Determine if quote
			if (character == quoteCharacter) {
				// Increment and continue as not separator
				numberOfQuotes++;
				continue;
			}

			// If separator character not in quote, then split
			if ((character == splitCharacter) && (numberOfQuotes % 2 == 0)) {
				// Split out text of cookie
				cookies.add(quotedText.substring(splitPos, i));

				// Specify location after comma for next split
				splitPos = i + 1;
			}
		}

		// Add last cookie text (ensuring not end with a comma)
		if (splitPos < quotedText.length()) {
			cookies.add(quotedText.substring(splitPos));
		}

		// Return the split text of cookies
		return cookies;
	}

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Time this {@link HttpCookie} expires. -1 indicates not to provide expire
	 * time.
	 */
	private long expireTime = -1;

	/**
	 * Path.
	 */
	private String path = null;

	/**
	 * Domain.
	 */
	private String domain = null;

	/**
	 * Initiate.
	 *
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public HttpCookie(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Initiate.
	 *
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 * @param expireTime
	 *            Time that the {@link HttpCookie} will be expired.
	 * @param domain
	 *            Domain.
	 * @param path
	 *            Path.
	 */
	public HttpCookie(String name, String value, long expireTime, String domain, String path) {
		this(name, value);
		this.expireTime = expireTime;
		this.domain = domain;
		this.path = path;
	}

	/**
	 * Obtains the name.
	 *
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the value.
	 *
	 * @return Value.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Specifies the time this {@link HttpCookie} expires.
	 *
	 * @param expireTime
	 *            Time this {@link HttpCookie} expires.
	 */
	public void setExpires(long expireTime) {
		this.expireTime = expireTime;
	}

	/**
	 * Specifies the domain.
	 *
	 * @param domain
	 *            Domain.
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Specifies the path.
	 *
	 * @param path
	 *            Path.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Obtains the {@link HttpResponse} {@link HttpHeader} value for this
	 * {@link HttpCookie}.
	 *
	 * @return {@link HttpResponse} {@link HttpHeader} value for this
	 *         {@link HttpCookie}.
	 */
	public String toHttpResponseHeaderValue() {

		// Construct details of the cookie
		StringBuilder headerValue = new StringBuilder();
		headerValue.append(this.name);
		headerValue.append("=\"");
		headerValue.append(this.value);
		headerValue.append("\"");
		if (this.expireTime >= 0) {
			headerValue.append("; expires=");
			String expireText = expireFormatter.format(new Date(this.expireTime));
			headerValue.append(expireText);
		}
		if (this.path != null) {
			headerValue.append("; path=");
			headerValue.append(this.path);
		}
		if (this.domain != null) {
			headerValue.append("; domain=");
			headerValue.append(this.domain);
		}

		// Return details of the cookie
		return headerValue.toString();
	}

	/*
	 * ============================ Object ================================
	 */

	@Override
	public String toString() {
		return this.toHttpResponseHeaderValue();
	}

}