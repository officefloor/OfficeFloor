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
package net.officefloor.eclipse.common.dialog.input;

/**
 * Listener for the {@link Input}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputListener {

	/**
	 * Invoked to notify value has changed.
	 * 
	 * @param value
	 *            New value.
	 */
	void notifyValueChanged(Object value);

	/**
	 * Invoked to notify value is invalid.
	 * 
	 * @param message
	 *            Reason value is invalid.
	 */
	void notifyValueInvalid(String message);
}
