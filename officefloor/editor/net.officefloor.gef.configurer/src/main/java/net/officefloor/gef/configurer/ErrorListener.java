/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.gef.configurer;

/**
 * Listener for errors in configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ErrorListener {

	/**
	 * Informed of error message.
	 * 
	 * @param inputLabel
	 *            Label for the input.
	 * @param message
	 *            Error message.
	 */
	void error(String inputLabel, String message);

	/**
	 * Informed of {@link Throwable}.
	 * 
	 * @param inputLabel
	 *            Label for the input.
	 * @param error
	 *            {@link Throwable}.
	 */
	void error(String inputLabel, Throwable error);

	/**
	 * Informed that valid.
	 */
	void valid();

}