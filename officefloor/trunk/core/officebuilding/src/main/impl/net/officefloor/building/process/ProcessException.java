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
package net.officefloor.building.process;

/**
 * Indicates a failure with a {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessException extends Exception {

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 */
	public ProcessException(String message) {
		super(message);
	}

	/**
	 * Initiate.
	 * 
	 * @param cause
	 *            Cause.
	 */
	public ProcessException(Throwable cause) {
		super(cause);
	}

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	public ProcessException(String message, Throwable cause) {
		super(message, cause);
	}

}