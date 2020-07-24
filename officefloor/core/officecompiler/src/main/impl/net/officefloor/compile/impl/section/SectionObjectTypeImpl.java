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

import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.section.SectionObject;

/**
 * {@link SectionObjectType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionObjectTypeImpl implements SectionObjectType {

	/**
	 * Name of the {@link SectionObject}.
	 */
	private final String objectName;

	/**
	 * Type of the {@link SectionObject}.
	 */
	private final String objectType;

	/**
	 * Type qualifier of the {@link SectionObject}.
	 */
	private final String typeQualifier;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Instantiate.
	 * 
	 * @param objectName    Name of the {@link SectionObject}.
	 * @param objectType    Type of the {@link SectionObject}.
	 * @param typeQualifier Type qualifier of the {@link SectionObject}.
	 * @param annotations   Annotations.
	 */
	public SectionObjectTypeImpl(String objectName, String objectType, String typeQualifier, Object[] annotations) {
		this.objectName = objectName;
		this.objectType = objectType;
		this.typeQualifier = typeQualifier;
		this.annotations = annotations;
	}

	/*
	 * ===================== SectionObjectType ============================
	 */

	@Override
	public String getSectionObjectName() {
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

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

}
