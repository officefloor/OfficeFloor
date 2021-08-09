/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure;

import net.officefloor.plugin.variable.Var;

/**
 * <code>Type definition</code> of {@link Var} required by the
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureVariableType {

	/**
	 * Obtains the name for the {@link Var}.
	 * 
	 * @return Name for the {@link Var}.
	 */
	String getVariableName();

	/**
	 * Obtains the type of the {@link Var}.
	 * 
	 * @return Type of the {@link Var}.
	 */
	String getVariableType();

}
