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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.OfficeFloorOutputNode;
import net.officefloor.compile.officefloor.OfficeFloorInputType;
import net.officefloor.compile.officefloor.OfficeFloorOutputType;
import net.officefloor.compile.spi.officefloor.OfficeFloorOutput;

/**
 * Implementation of the {@link OfficeFloorOutputNode}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorOutputNodeImpl implements OfficeFloorOutputNode,
		OfficeFloorOutputType {

	/**
	 * Name of this {@link OfficeFloorOutput}.
	 */
	private final String name;

	/**
	 * Argument type from this {@link OfficeFloorOutput}.
	 */
	private final String argumentType;

	/**
	 * Initialise.
	 * 
	 * @param name
	 *            Name of this {@link OfficeFloorOutput}.
	 * @param argumentType
	 *            Argument type from this {@link OfficeFloorOutput}.
	 */
	public OfficeFloorOutputNodeImpl(String name, String argumentType) {
		this.name = name;
		this.argumentType = argumentType;
	}

	/*
	 * ================= OfficeFloorOuputNode =============================
	 */

	@Override
	public OfficeFloorOutputType getOfficeFloorOutputType() {
		return this;
	}

	/*
	 * =================== OfficeFloorOuput ===============================
	 */

	@Override
	public String getOfficeFloorOutputName() {
		return this.name;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

	/*
	 * ================= OfficeFloorOuputType =============================
	 */

	@Override
	public OfficeFloorInputType getHandlingOfficeFloorInputType() {
		// TODO Auto-generated method stub
		return null;
	}

}