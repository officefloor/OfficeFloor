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
