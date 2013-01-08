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

import net.officefloor.compile.spi.office.TypeQualification;

/**
 * {@link TypeQualification} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeQualificationImpl implements TypeQualification {

	/**
	 * Qualifier.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final String type;

	/**
	 * Initiate.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code>.
	 * @param type
	 *            Type.
	 */
	public TypeQualificationImpl(String qualifier, String type) {
		this.qualifier = qualifier;
		this.type = type;
	}

	/*
	 * ==================== TypeQualification =======================
	 */

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	@Override
	public String getType() {
		return this.type;
	}

}