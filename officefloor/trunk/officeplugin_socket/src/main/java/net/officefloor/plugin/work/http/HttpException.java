/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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