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
package net.officefloor.plugin.gwt.service;

/**
 * Indicates a failure in sending a response via the
 * {@link ServerGwtRpcConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerGwtRpcConnectionException extends RuntimeException {

	/**
	 * Creates the {@link ServerGwtRpcConnectionException} ensuring that it does
	 * not wrap itself.
	 * 
	 * @param cause
	 *            Cause.
	 * @return {@link ServerGwtRpcConnectionException}.
	 */
	public static ServerGwtRpcConnectionException newException(Throwable cause) {
		if (cause instanceof ServerGwtRpcConnectionException) {
			return (ServerGwtRpcConnectionException) cause;
		} else {
			return new ServerGwtRpcConnectionException(cause);
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param cause
	 *            Cause.
	 */
	private ServerGwtRpcConnectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 */
	public ServerGwtRpcConnectionException(String message) {
		super(message);
	}

}