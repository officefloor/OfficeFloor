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

import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.spi.office.OfficeOutput;

/**
 * {@link OfficeOutputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeOutputTypeImpl implements OfficeOutputType {

	/**
	 * Name of the {@link OfficeOutput}.
	 */
	private final String outputName;

	/**
	 * Argument type of the {@link OfficeOutput}.
	 */
	private final String argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param outputName
	 *            Name of the {@link OfficeOutput}.
	 * @param argumentType
	 *            Argument type of the {@link OfficeOutput}.
	 */
	public OfficeOutputTypeImpl(String outputName, String argumentType) {
		this.outputName = outputName;
		this.argumentType = argumentType;
	}

	/*
	 * =================== OfficeOutputType =========================
	 */

	@Override
	public String getOfficeOutputName() {
		return this.outputName;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

}
