/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.spi.office.OfficeSectionObject;

/**
 * {@link OfficeSectionObjectTypeImpl} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionObjectTypeImpl implements OfficeSectionObjectType {

	/**
	 * Name of the {@link OfficeSectionObject}.
	 */
	private final String objectName;

	/**
	 * Type of the {@link OfficeSectionObject}.
	 */
	private final String objectType;

	/**
	 * Type qualifier for the {@link OfficeSectionObject}.
	 */
	private final String typeQualifier;

	/**
	 * Instantiate.
	 * 
	 * @param objectName
	 *            Name of the {@link OfficeSectionObject}.
	 * @param objectType
	 *            Type of the {@link OfficeSectionObject}.
	 * @param typeQualifier
	 *            Type qualifier for the {@link OfficeSectionObject}.
	 */
	public OfficeSectionObjectTypeImpl(String objectName, String objectType,
			String typeQualifier) {
		this.objectName = objectName;
		this.objectType = objectType;
		this.typeQualifier = typeQualifier;
	}

	/*
	 * ================= OfficeSectionObjectType =====================
	 */

	@Override
	public String getOfficeSectionObjectName() {
		return this.objectName;
	}

	@Override
	public String getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

}
