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
package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.spi.office.OfficeOutput;

/**
 * {@link OfficeOutputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeOutputTypeImpl implements OfficeOutputType {

	/**
	 * Name of the {@link OfficeOutput}.
	 */
	private final String outputName;

	/**
	 * Argument type of the {@link OfficeOutput}.
	 */
	private final String argumentType;

	/**
	 * Handling {@link OfficeInputType}.
	 */
	private final OfficeInputType inputType;

	/**
	 * Instantiate.
	 * 
	 * @param outputName
	 *            Name of the {@link OfficeOutput}.
	 * @param argumentType
	 *            Argument type of the {@link OfficeOutput}.
	 * @param inputType
	 *            Handling {@link OfficeInputType}.
	 */
	public OfficeOutputTypeImpl(String outputName, String argumentType,
			OfficeInputType inputType) {
		this.outputName = outputName;
		this.argumentType = argumentType;
		this.inputType = inputType;
	}

	/*
	 * =================== OfficeOutputType =========================
	 */

	@Override
	public String getOfficeOutputName() {
		return this.outputName;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

	@Override
	public OfficeInputType getHandlingOfficeInputType() {
		return this.inputType;
	}

}