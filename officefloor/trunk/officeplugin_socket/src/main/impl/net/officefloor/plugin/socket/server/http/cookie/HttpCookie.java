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
package net.officefloor.plugin.socket.server.http.cookie;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;

/**
 * HTTP Cookie.
 *
 * @author Daniel Sagenschneider
 */
public class HttpCookie {

	/**
	 * Parses the {@link HttpCookie} instances from the {@link HttpHeader}
	 * value.
	 *
	 * @param httpHeaderValue
	 *            Value of the {@link HttpHeader}.
	 * @return Listing of {@link HttpCookie} instances.
	 */
	public static List<HttpCookie> parse(String httpHeaderValue) {

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
	private static List<String> splitQuotedText(String quotedText,
			char quoteCharacter, char splitCharacter) {

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
}