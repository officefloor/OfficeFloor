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

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.spi.office.OfficeInput;

/**
 * {@link OfficeInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeInputTypeImpl implements OfficeInputType {

	/**
	 * Name of the {@link OfficeInput}.
	 */
	private final String inputName;

	/**
	 * {@link Class} name of the parameter.
	 */
	private final String parameterType;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeInput}.
	 * @param parameterType
	 *            {@link Class} name of the parameter.
	 */
	public OfficeInputTypeImpl(String inputName, String parameterType) {
		this.inputName = inputName;
		this.parameterType = parameterType;
	}

	/*
	 * ================== OfficeInputType ======================
	 */

	@Override
	public String getOfficeInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

}
