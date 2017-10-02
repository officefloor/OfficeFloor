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
package net.officefloor.web.parameter;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;

/**
 * Indicates a failure with the {@link HttpRequest} parameters.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersException extends HttpException {

	/**
	 * Initiate.
	 * 
	 * @param reason
	 *            Reason.
	 */
	public HttpParametersException(String reason) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, null, reason);
	}

	/**
	 * Initiate.
	 * 
	 * @param cause
	 *            Cause.
	 */
	public HttpParametersException(Throwable cause) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}

}