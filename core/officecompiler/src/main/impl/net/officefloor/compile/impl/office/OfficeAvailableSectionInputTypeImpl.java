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
