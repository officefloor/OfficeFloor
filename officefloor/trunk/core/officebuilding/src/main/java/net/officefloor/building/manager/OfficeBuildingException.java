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
package net.officefloor.building.manager;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@link Exception} from the {@link OfficeBuildingManagerMBean} that is
 * {@link Serializable} to enable propagating failures back from the MBean.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingException extends Exception implements Serializable {

	/**
	 * Creates the {@link OfficeBuildingException}.
	 * 
	 * @param cause
	 *            {@link Throwable}.
	 * @return {@link OfficeBuildingException}.
	 */
	public static OfficeBuildingException newException(Throwable cause) {
		try {

			// Attempt to serialize the exception
			ByteArrayOutputStream temp = new ByteArrayOutputStream();
			ObjectOutputStream serializer = new ObjectOutputStream(temp);
			serializer.writeObject(cause);
			serializer.flush();

			// Able to serialize so propagate as is
			return new OfficeBuildingException(cause);

		} catch (Throwable notSerializable) {

			// Create a message describing the exception
			StringBuilder msg = new StringBuilder();
			msg.append(cause.getMessage());
			Throwable previous = cause;
			Throwable current = cause.getCause();
			while ((current != null) && (current != previous)) {
				msg.append("\n\tCaused by");
				msg.append(current.getMessage());

				// Setup for next iteration
				previous = current;
				current = current.getCause();
			}

			// Return serializable exception to propagate exception details
			return new OfficeBuildingException(msg.toString());
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param ex
	 *            {@link Serializable} cause.
	 */
	private OfficeBuildingException(Throwable ex) {
		super(ex);
	}

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 */
	private OfficeBuildingException(String message) {
		super(message);
	}

}