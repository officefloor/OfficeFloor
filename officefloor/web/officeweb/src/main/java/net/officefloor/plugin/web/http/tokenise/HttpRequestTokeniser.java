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
package net.officefloor.plugin.web.http.tokenise;

import java.io.IOException;

import net.officefloor.server.http.HttpRequest;

/**
 * Tokenises the {@link HttpRequest} for the path, parameters, fragment.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestTokeniser {

	/**
	 * <p>
	 * Tokenises the {@link HttpRequest} for the path, parameters and fragment
	 * providing them to the {@link HttpRequestTokenHandler} to handle.
	 * <p>
	 * This encompasses the whole {@link HttpRequest} (e.g. on <code>POST</code>
	 * will also tokenise the body for parameters).
	 * 
	 * @param request
	 *            {@link HttpRequest} to be tokenised.
	 * @param handler
	 *            {@link HttpRequestTokenHandler} to handle the
	 *            {@link HttpRequest} tokens.
	 * @throws IOException
	 *             If fails to read data from {@link HttpRequest}.
	 * @throws HttpRequestTokeniseException
	 *             If fails to tokenise the {@link HttpRequest}.
	 */
	void tokeniseHttpRequest(HttpRequest request,
			HttpRequestTokenHandler handler) throws IOException,
			HttpRequestTokeniseException;

	/**
	 * Tokenises the request URI for the path, parameters and fragment.
	 * 
	 * @param requestURI
	 *            Request URI to be tokenised.
	 * @param handler
	 *            {@link HttpRequestTokenHandler} to handle the request URI
	 *            tokens.
	 * @throws HttpRequestTokeniseException
	 *             If fails to tokenise the request URI.
	 */
	void tokeniseRequestURI(String requestURI, HttpRequestTokenHandler handler)
			throws HttpRequestTokeniseException;

}