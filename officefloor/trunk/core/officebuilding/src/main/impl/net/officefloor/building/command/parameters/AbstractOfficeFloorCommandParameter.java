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
 * Abstract {@link OfficeFloorCommandParameter}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorCommandParameter implements
		OfficeFloorCommandParameter {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Short name.
	 */
	private final String shortName;

	/**
	 * Description.
	 */
	private final String description;

	/**
	 * Indicates if a value is required.
	 */
	private final boolean isRequireValue;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param shortName
	 *            Short name.
	 * @param description
	 *            Description.
	 * @param isRequireValue
	 *            Flag indicating if value required.
	 */
	public AbstractOfficeFloorCommandParameter(String name, String shortName,
			String description, boolean isRequireValue) {
		this.name = name;
		this.shortName = shortName;
		this.description = description;
		this.isRequireValue = isRequireValue;
	}

	/*
	 * ================ OfficeFloorCommandParameter ==================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getShortName() {
		return this.shortName;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public boolean isRequireValue() {
		return this.isRequireValue;
	}

}