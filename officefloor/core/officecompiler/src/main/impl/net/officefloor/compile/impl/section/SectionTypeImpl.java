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

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;

/**
 * {@link SectionType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionTypeImpl implements SectionType {

	/**
	 * {@link SectionInputType} instances.
	 */
	private final SectionInputType[] inputTypes;

	/**
	 * {@link SectionOutputType} instances.
	 */
	private final SectionOutputType[] outputTypes;

	/**
	 * {@link SectionObjectType} instances.
	 */
	private final SectionObjectType[] objectTypes;

	/**
	 * Instantiate.
	 * 
	 * @param inputTypes
	 *            {@link SectionInputType} instances.
	 * @param outputTypes
	 *            {@link SectionOutputType} instances.
	 * @param objectTypes
	 *            {@link SectionObjectType} instances.
	 */
	public SectionTypeImpl(SectionInputType[] inputTypes,
			SectionOutputType[] outputTypes, SectionObjectType[] objectTypes) {
		this.inputTypes = inputTypes;
		this.outputTypes = outputTypes;
		this.objectTypes = objectTypes;
	}

	/*
	 * ================== SectionType =========================
	 */

	@Override
	public SectionInputType[] getSectionInputTypes() {
		return this.inputTypes;
	}

	@Override
	public SectionOutputType[] getSectionOutputTypes() {
		return this.outputTypes;
	}

	@Override
	public SectionObjectType[] getSectionObjectTypes() {
		return this.objectTypes;
	}

}