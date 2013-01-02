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
package net.officefloor.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Abstract functionality for a Maven Goal.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGoal extends AbstractMojo {

	/**
	 * Ensure the value is not <code>null</code>.
	 * 
	 * @param message
	 *            Message to report if value is <code>null</code>.
	 * @param value
	 *            Value to check.
	 * @throws MojoFailureException
	 *             If value is <code>null</code>.
	 */
	protected static void ensureNotNull(String message, Object value)
			throws MojoFailureException {
		if (value == null) {
			throw new MojoFailureException(message);
		}
	}

	/**
	 * Defaults the value.
	 * 
	 * @param value
	 *            Value to check if blank.
	 * @param defaultValue
	 *            Default value.
	 * @return Defaulted value.
	 */
	protected static <T> T defaultValue(T value, T defaultValue) {
		if (value == null) {
			// Use default
			return defaultValue;
		} else if (value instanceof String) {
			// String value so determine if blank
			String text = (String) value;
			if (text.trim().length() == 0) {
				// Blank text so default value
				return defaultValue;
			} else {
				// Non-blank text so use value
				return value;
			}
		} else {
			// Not string and not null so use value
			return value;
		}
	}

	/**
	 * Creates a new {@link MojoExecutionException}.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 * @return {@link MojoExecutionException}.
	 */
	public static MojoExecutionException newMojoExecutionException(String message,
			Throwable cause) {

		// Create the message
		StringBuilder msg = new StringBuilder();
		msg.append(message);

		// Provide the causes
		Throwable parent = null;
		while ((cause != null) && (cause != parent)) {

			// Output the cause
			msg.append("\n   Caused by ");
			msg.append(cause.getMessage());
			msg.append(" [");
			msg.append(cause.getClass().getSimpleName());
			msg.append("]");

			// Obtain the cause for the next iteration
			parent = cause;
			cause = cause.getCause();
		}

		// Create and return the Mojo Execution Exception
		return new MojoExecutionException(msg.toString());
	}

}