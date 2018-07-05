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
package net.officefloor.compile.impl.office;

import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.spi.office.OfficeSectionFunction;

/**
 * {@link OfficeFunctionType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFunctionTypeImpl implements OfficeFunctionType {

	/**
	 * Name of the {@link OfficeSectionFunction}.
	 */
	private final String functionName;

	/**
	 * Containing {@link OfficeSubSectionType}.
	 */
	private final OfficeSubSectionType subSectionType;

	/**
	 * {@link ObjectDependencyType} instances of the
	 * {@link OfficeSectionFunction}.
	 */
	private final ObjectDependencyType[] dependencies;

	/**
	 * Instantiate.
	 * 
	 * @param functionName
	 *            Name of the {@link OfficeSectionFunction}.
	 * @param subSectionType
	 *            Containing {@link OfficeSubSectionType}.
	 * @param dependencies
	 *            {@link ObjectDependencyType} instances of the
	 *            {@link OfficeSectionFunction}.
	 */
	public OfficeFunctionTypeImpl(String functionName, OfficeSubSectionType subSectionType,
			ObjectDependencyType[] dependencies) {
		this.functionName = functionName;
		this.subSectionType = subSectionType;
		this.dependencies = dependencies;
	}

	/*
	 * =============== OfficeFunctionType ======================
	 */

	@Override
	public String getOfficeFunctionName() {
		return this.functionName;
	}

	@Override
	public OfficeSubSectionType getOfficeSubSectionType() {
		return this.subSectionType;
	}

	@Override
	public ObjectDependencyType[] getObjectDependencies() {
		return this.dependencies;
	}

}