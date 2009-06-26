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
package net.officefloor.plugin.socket.server.http.parse;

import net.officefloor.plugin.socket.server.http.HttpStatus;

/**
 * Indicates that failed to parse.
 * 
 * @author Daniel Sagenschneider
 */
public class ParseException extends Exception {

	/**
	 * HTTP status indicating failure.
	 */
	private final int httpStatus;

	/**
	 * Initiate.
	 * 
	 * @param httpStatus
	 *            HTTP status indicating failure.
	 * @param message
	 *            Reason for parsing failure.
	 */
	public ParseException(int httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
	}

	/**
	 * Initiate.
	 * 
	 * @param cause
	 *            Cause of parsing failure.
	 */
	public ParseException(Throwable cause) {
		super(cause);

		// Server failure
		this.httpStatus = HttpStatus._500;
	}

	/**
	 * Obtains the HTTP status indicating failure.
	 * 
	 * @return HTTP status indicating failure.
	 */
	public int getHttpStatus() {
		return this.httpStatus;
	}
}
