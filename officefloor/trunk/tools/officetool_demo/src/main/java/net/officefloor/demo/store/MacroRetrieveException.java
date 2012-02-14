/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.demo.store;

import net.officefloor.demo.macro.Macro;

/**
 * Indicates a failure in retrieving the {@link Macro} from the
 * {@link MacroStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroRetrieveException extends Exception {

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Reason.
	 */
	public MacroRetrieveException(String message) {
		super(message);
	}

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Reason.
	 * @param cause
	 *            Cause.
	 */
	public MacroRetrieveException(String message, Throwable cause) {
		super(message, cause);
	}

}