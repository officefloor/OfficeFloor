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
 * Handler that receives the {@link HttpRequest} tokens.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestTokenHandler {

	/**
	 * Handles a {@link HttpRequest} parameter.
	 * 
	 * @param name
	 *            Name of the parameter.
	 * @param value
	 *            Value for the parameter.
	 * @throws HttpParametersException
	 *             If fails to handle the {@link HttpRequest} parameter.
	 */
	void handleHttpParameter(String name, String value)
			throws HttpParametersException;

}