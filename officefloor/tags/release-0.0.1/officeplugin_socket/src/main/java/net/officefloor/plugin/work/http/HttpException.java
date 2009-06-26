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
package net.officefloor.plugin.work.http;

/**
 * Indicates a HTTP failure.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpException extends Exception {

	/**
	 * Suggested response HTTP status.
	 */
	private final int httpStatus;

	/**
	 * Initiate.
	 * 
	 * @param httpStatus
	 *            Suggested response HTTP status.
	 * @param message
	 *            Reason for failure.
	 */
	public HttpException(int httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
	}

	/**
	 * Initiate.
	 * 
	 * @param httpStatus
	 *            Suggested response HTTP status.
	 * @param cause
	 *            Cause of failure.
	 */
	public HttpException(int httpStatus, Throwable cause) {
		super(cause);
		this.httpStatus = httpStatus;
	}

	/**
	 * Initiate.
	 * 
	 * @param httpStatus
	 *            Suggested response HTTP status.
	 * @param message
	 *            Reason for failure.
	 * @param cause
	 *            Cause of failure.
	 */
	public HttpException(int httpStatus, String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = httpStatus;
	}

	/**
	 * Obtains the suggested HTTP status for the failure.
	 * 
	 * @return Suggested HTTP status for the failure.
	 */
	public int getHttpStatus() {
		return this.httpStatus;
	}

}