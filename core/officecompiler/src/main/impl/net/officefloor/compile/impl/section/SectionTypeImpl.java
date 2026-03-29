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
