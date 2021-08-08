/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
