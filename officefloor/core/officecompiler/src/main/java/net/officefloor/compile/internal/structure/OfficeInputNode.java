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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.spi.office.OfficeInput;

/**
 * {@link OfficeInput} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeInputNode extends LinkFlowNode, OfficeInput {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Input";

	/**
	 * Initialises the {@link OfficeInputNode}.
	 * 
	 * @param parameterType
	 *            Parameter type of {@link OfficeInput}.
	 */
	void initialise(String parameterType);

	/**
	 * Obtains the {@link OfficeInputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeInputType} or <code>null</code> if can not
	 *         determine.
	 */
	OfficeInputType loadOfficeInputType(CompileContext compileContext);

}
