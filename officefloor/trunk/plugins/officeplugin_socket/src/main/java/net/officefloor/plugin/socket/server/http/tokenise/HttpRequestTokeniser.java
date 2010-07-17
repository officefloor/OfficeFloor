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

package net.officefloor.plugin.socket.server.http.tokenise;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parameters.HttpParametersException;

/**
 * Tokenises the {@link HttpRequest} for the path, parameters, fragment.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestTokeniser {

	/**
	 * Tokenises the {@link HttpRequest} for the path, parameters and fragment
	 * providing them to the {@link HttpRequestTokenHandler} to handle.
	 * 
	 * @param request
	 *            {@link HttpRequest} to be tokenised.
	 * @param handler
	 *            {@link HttpRequestTokenHandler} to handle the
	 *            {@link HttpRequest} tokens.
	 * @throws HttpParametersException
	 *             If fails to tokenise the {@link HttpRequest}.
	 */
	void parseHttpParameters(HttpRequest request,
			HttpRequestTokenHandler handler) throws HttpParametersException;

}