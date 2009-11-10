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
package net.officefloor.plugin.socket.server.http.parameters;

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Parses the {@link HttpRequest} for the parameters.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpParametersParser {

	/**
	 * Parses the {@link HttpRequest} for the parameters providing them to the
	 * {@link HttpParametersParseHandler} to handle.
	 * 
	 * @param request
	 *            {@link HttpRequest} to be parsed.
	 * @param handler
	 *            {@link HttpParametersParseHandler} to handle the
	 *            {@link HttpRequest} parameters.
	 * @throws HttpParametersException
	 *             If fails to parse the {@link HttpRequest} for its parameters.
	 */
	void parseHttpParameters(HttpRequest request,
			HttpParametersParseHandler handler) throws HttpParametersException;

}