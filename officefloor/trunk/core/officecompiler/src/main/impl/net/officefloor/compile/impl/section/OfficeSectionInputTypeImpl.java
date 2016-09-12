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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.office.OfficeSectionInputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * {@link OfficeSectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionInputTypeImpl implements OfficeSectionInputType {

	/**
	 * Name of the containing {@link OfficeSection}.
	 */
	private final String sectionName;

	/**
	 * Name of the {@link OfficeSectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type of the {@link OfficeSectionInput}.
	 */
	private final String parameterType;

	/**
	 * Instantiate.
	 * 
	 * @param sectionName
	 *            Name of the containing {@link OfficeSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 * @param parameterType
	 *            Parameter type of the {@link OfficeSectionInput}.
	 */
	public OfficeSectionInputTypeImpl(String sectionName, String inputName,
			String parameterType) {
		this.sectionName = sectionName;
		this.inputName = inputName;
		this.parameterType = parameterType;
	}

	/*
	 * ====================== OfficeSectionInputType ======================
	 */

	@Override
	public String getOfficeSectionName() {
		// TODO implement OfficeSectionInputType.getOfficeSectionName
		throw new UnsupportedOperationException(
				"TODO implement OfficeSectionInputType.getOfficeSectionName");

	}

	@Override
	public String getOfficeSectionInputName() {
		// TODO implement OfficeSectionInputType.getOfficeSectionInputName
		throw new UnsupportedOperationException(
				"TODO implement OfficeSectionInputType.getOfficeSectionInputName");

	}

	@Override
	public String getParameterType() {
		// TODO implement OfficeSectionInputType.getParameterType
		throw new UnsupportedOperationException(
				"TODO implement OfficeSectionInputType.getParameterType");

	}

}