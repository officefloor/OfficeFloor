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

package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Augmented {@link FunctionObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedFunctionObject {

	/**
	 * Obtains the name of this {@link FunctionObject}.
	 * 
	 * @return Name of this {@link FunctionObject}.
	 */
	String getFunctionObjectName();

	/**
	 * Flags this {@link FunctionObject} as a parameter for the
	 * {@link ManagedFunction}.
	 */
	void flagAsParameter();

	/**
	 * Indicates if the {@link FunctionObject} is already linked.
	 * 
	 * @return <code>true</code> if already linked.
	 */
	boolean isLinked();

}
