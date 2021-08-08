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
import net.officefloor.compile.spi.section.SectionInput;

/**
 * {@link SectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionInputTypeImpl implements SectionInputType {

	/**
	 * Name of the {@link SectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type of the {@link SectionInput}.
	 */
	private final String parameterType;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInput}.
	 * @param parameterType
	 *            Parameter type of the {@link SectionInput}.
	 * @param annotations
	 *            Annotations.
	 */
	public SectionInputTypeImpl(String inputName, String parameterType, Object[] annotations) {
		this.inputName = inputName;
		this.parameterType = parameterType;
		this.annotations = annotations;
	}

	/*
	 * ==================== SectionInputType =========================
	 */

	@Override
	public String getSectionInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

}
