/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.api.team;

import java.util.concurrent.RejectedExecutionException;

/**
 * <p>
 * Indicates the {@link Team} is overloaded.
 * <p>
 * By convention {@link Team} instances should throw this to indicate back
 * pressure, as load on the {@link Team} is too high.
 * <p>
 * This is similar to {@link RejectedExecutionException}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamOverloadException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 * @param cause   Cause.
	 */
	public TeamOverloadException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 */
	public TeamOverloadException(String message) {
		super(message);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public TeamOverloadException(Throwable cause) {
		super(cause);
	}

}