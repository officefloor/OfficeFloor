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

import net.officefloor.compile.internal.structure.OfficeFloorInputNode;
import net.officefloor.compile.officefloor.OfficeFloorInputType;
import net.officefloor.compile.officefloor.OfficeFloorOutputType;
import net.officefloor.compile.spi.officefloor.OfficeFloorInput;

/**
 * Implementation of the {@link OfficeFloorInputNode}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorInputNodeImpl implements OfficeFloorInputNode,
		OfficeFloorInputType {

	/**
	 * Name of this {@link OfficeFloorInput}.
	 */
	private final String name;

	/**
	 * Parameter type of this {@link OfficeFloorInput}.
	 */
	private final String parameterType;

	/**
	 * Initialise.
	 * 
	 * @param name
	 *            Name of {@link OfficeFloorInput}.
	 * @param parameterType
	 *            Parameter type of {@link OfficeFloorInput}.
	 */
	public OfficeFloorInputNodeImpl(String name, String parameterType) {
		this.name = name;
		this.parameterType = parameterType;
	}

	/*
	 * ======================= OfficeFloorInputNode ===========================
	 */

	@Override
	public OfficeFloorInputType getOfficeFloorInputType() {
		return this;
	}

	/*
	 * ========================= OfficeFloorInput =============================
	 */

	@Override
	public String getOfficeFloorInputName() {
		return this.name;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	/*
	 * ======================= OfficeFloorInputType ===========================
	 */

	@Override
	public OfficeFloorOutputType getResponseOfficeFloorOutputType() {
		// TODO Auto-generated method stub
		return null;
	}

}