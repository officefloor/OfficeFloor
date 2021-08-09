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
