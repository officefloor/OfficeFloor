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
package net.officefloor.plugin.work.http.html.form;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Wraps the HTTP path providing the various segment details.
 *
 * @author Daniel Sagenschneider
 */
public class HtmlForm {

	/**
	 * Parse the query to obtain the {@link HtmlFormParameter} instances.
	 *
	 * @param query
	 *            HTTP query segment.
	 * @return {@link HtmlFormParameter} instances.
	 */
	public static List<HtmlFormParameter> parseParameters(String query) {

		// Create the buffers to contain the values
		CharacterBuffer name = new CharacterBuffer(32);
		CharacterBuffer value = new CharacterBuffer(256);

		// Flags for parsing
		boolean isPath = true;
		boolean isNameNotValue = true;

		// Iterate over the query extracting the parameters
		List<HtmlFormParameter> parameters = new LinkedList<HtmlFormParameter>();
		for (int i = 0; i < query.length(); i++) {
			char character = query.charAt(i);

			switch (character) {
			case '?':
				// No longer path, into query string
				isPath = false;
				break;

			case '=':
				// Flag to now obtain value
				isNameNotValue = false;
				break;

			case '&':
			case ';':
				// Add the parameter (if have name)
				if (name.length() > 0) {
					String nameText = name.toString();
					String valueText = value.toString();
					parameters.add(new HtmlFormParameter(nameText, valueText));
				}

				// Clear the name and value for next parameter
				name.clear();
				value.clear();

				// Flag to now obtain name
				isNameNotValue = true;
				break;

			case '+':
				// Plus in query string is a space
				if (!isPath) {
					character = ' ';
				}
				// Carry onto append character
			default:
				// Append to either name or value
				if (isNameNotValue) {
					name.append(character);
				} else {
					value.append(character);
				}
				break;
			}
		}

		// Add the final parameter (if available)
		if (name.length() > 0) {
			String nameText = name.toString();
			String valueText = value.toString();
			parameters.add(new HtmlFormParameter(nameText, valueText));
		}

		// Return the parameters
		return parameters;
	}

	/**
	 * Path.
	 */
	private final String path;

	/**
	 * Listing of {@link HtmlFormParameter} instances.
	 */
	private final List<HtmlFormParameter> parameters;

	/**
	 * Fragment.
	 */
	private final String fragment;

	/**
	 * Initiate from a GET {@link HttpRequest}.
	 *
	 * @param httpGetPath
	 *            HTTP GET path.
	 * @throws URISyntaxException
	 *             If invalid HTTP path.
	 */
	public HtmlForm(String httpGetPath) throws URISyntaxException {
		this(httpGetPath, null);
	}

	/**
	 * Initiate from a POST {@link HttpRequest}.
	 *
	 * @param httpPostPath
	 *            HTTP POST path.
	 * @param httpPostBody
	 *            HTTP POST body.
	 * @throws URISyntaxException
	 *             If invalid HTTP path or HTML FORM body.
	 */
	public HtmlForm(String httpPostPath, String httpPostBody)
			throws URISyntaxException {

		// Create the URI to decode the path
		// TODO consider using own decoding rather than URI
		URI uri = new URI(httpPostPath);

		// Obtain the path
		this.path = uri.getPath();

		// Obtain the parameters
		String query = uri.getQuery();
		this.parameters = parseParameters((query == null ? "" : query));

		// Obtain the fragment
		this.fragment = uri.getFragment();

		// Determine if append body
		if (httpPostBody != null) {
			List<HtmlFormParameter> bodyParameters = parseParameters(httpPostBody);
			this.parameters.addAll(bodyParameters);
		}
	}

	/**
	 * Obtains the path.
	 *
	 * @return Path.
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Obtains the {@link HtmlFormParameter} instances.
	 *
	 * @return {@link HtmlFormParameter} instances.
	 */
	public List<HtmlFormParameter> getParameters() {
		return this.parameters;
	}

	/**
	 * Obtains the fragment.
	 *
	 * @return Fragment.
	 */
	public String getFragment() {
		return this.fragment;
	}

}
