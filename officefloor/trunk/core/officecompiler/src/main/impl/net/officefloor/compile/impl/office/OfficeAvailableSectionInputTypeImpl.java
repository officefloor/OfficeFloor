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

import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * {@link OfficeAvailableSectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeAvailableSectionInputTypeImpl implements
		OfficeAvailableSectionInputType {

	/**
	 * Name of the {@link OfficeSection}.
	 */
	private final String sectionName;

	/**
	 * Name of the {@link OfficeSectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type for the {@link OfficeSectionInput}.
	 */
	private final String parameterType;

	/**
	 * Instantiate.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 * @param parameterType
	 *            Parameter type for the {@link OfficeSectionInput}.
	 */
	public OfficeAvailableSectionInputTypeImpl(String sectionName,
			String inputName, String parameterType) {
		this.sectionName = sectionName;
		this.inputName = inputName;
		this.parameterType = parameterType;
	}

	/*
	 * =================== OfficeAvailableSectionInputType =============
	 */

	@Override
	public String getOfficeSectionName() {
		return this.sectionName;
	}

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

}