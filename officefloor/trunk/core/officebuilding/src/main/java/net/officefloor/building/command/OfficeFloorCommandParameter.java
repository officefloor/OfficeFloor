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
package net.officefloor.building.command;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Command parameter for an {@link OfficeFloorCommand}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommandParameter {

	/**
	 * <p>
	 * Obtains the name of this {@link OfficeFloorCommandParameter}.
	 * <p>
	 * This would be for example <code>help</code> for help.
	 * 
	 * @return Name.
	 */
	String getName();

	/**
	 * <p>
	 * Provides an optional short name for easier typing.
	 * <p>
	 * This would be for example <code>h</code> for help.
	 * 
	 * @return Short name or <code>null</code> if no short name.
	 */
	String getShortName();

	/**
	 * Obtains a description for this {@link OfficeFloorCommandParameter}.
	 * 
	 * @return Description for this {@link OfficeFloorCommandParameter}.
	 */
	String getDescription();

	/**
	 * <p>
	 * Flags if this {@link OfficeFloorCommandParameter} requires a value.
	 * <p>
	 * Should this return <code>false</code> then {@link #addValue(String)} will
	 * be invoked with a <code>null</code> value. Also the
	 * {@link OfficeFloorCommandParser} will not expect a value following the
	 * option.
	 * 
	 * @return <code>true</code> if requires value.
	 */
	boolean isRequireValue();

	/**
	 * <p>
	 * Adds a value for this parameter.
	 * <p>
	 * Typically this should only be invoked once however is available to allow
	 * values to be provided multiple times (e.g. in the case of a
	 * {@link OfficeFloor} tag replacement).
	 * 
	 * @param value
	 *            Value for this argument.
	 * 
	 * @see #isRequireValue()
	 */
	void addValue(String value);

}