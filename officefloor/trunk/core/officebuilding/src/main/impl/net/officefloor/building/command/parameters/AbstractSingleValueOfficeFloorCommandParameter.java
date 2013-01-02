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
package net.officefloor.building.command.parameters;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} that provides only the first value
 * specified.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSingleValueOfficeFloorCommandParameter extends
		AbstractOfficeFloorCommandParameter {

	/**
	 * Value for this parameter.
	 */
	private String value = null;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param shortName
	 *            Short name.
	 * @param description
	 *            Description.
	 */
	public AbstractSingleValueOfficeFloorCommandParameter(String name,
			String shortName, String description) {
		super(name, shortName, description, true);
	}

	/**
	 * Obtains the value for this parameter.
	 * 
	 * @return Value for this parameter.
	 */
	protected String getValue() {
		return this.value;
	}

	/*
	 * ================ OfficeFloorCommandParameter =================
	 */

	@Override
	public void addValue(String value) {
		// Take only the first specified value
		if (this.value == null) {
			this.value = value;
		}
	}

}