/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.section;

import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * {@link OfficeAvailableSectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionInputTypeImpl implements OfficeSectionInputType {

	/**
	 * Name of the {@link OfficeSectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type of the {@link OfficeSectionInput}.
	 */
	private final String parameterType;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 * @param parameterType
	 *            Parameter type of the {@link OfficeSectionInput}.
	 * @param annotations
	 *            Annotations.
	 */
	public OfficeSectionInputTypeImpl(String inputName, String parameterType, Object[] annotations) {
		this.inputName = inputName;
		this.parameterType = parameterType;
		this.annotations = annotations;
	}

	/*
	 * ====================== OfficeSectionInputType ======================
	 */

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

}