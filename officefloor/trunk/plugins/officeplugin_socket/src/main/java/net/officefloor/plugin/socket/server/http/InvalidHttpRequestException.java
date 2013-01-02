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
package net.officefloor.plugin.socket.server.http;

/**
 * Thrown by processing of the {@link HttpRequest} to indicate that the
 * {@link HttpRequest} is invalid.
 *
 * @author Daniel Sagenschneider
 */
public class InvalidHttpRequestException extends HttpException {

	/**
	 * Initiate.
	 *
	 * @param httpStatus
	 *            HTTP status of this exception.
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	public InvalidHttpRequestException(int httpStatus, String message,
			Throwable cause) {
		super(httpStatus, message, cause);
	}

	/**
	 * Initiate.
	 *
	 * @param httpStatus
	 *            HTTP status of this exception.
	 * @param message
	 *            Message.
	 */
	public InvalidHttpRequestException(int httpStatus, String message) {
		super(httpStatus, message);
	}

	/**
	 * Initiate.
	 *
	 * @param httpStatus
	 *            HTTP status of this exception.
	 * @param cause
	 *            Cause.
	 */
	public InvalidHttpRequestException(int httpStatus, Throwable cause) {
		super(httpStatus, cause);
	}

	/**
	 * Initiate.
	 *
	 * @param httpStatus
	 *            HTTP status of this exception.
	 */
	public InvalidHttpRequestException(int httpStatus) {
		super(httpStatus);
	}

}