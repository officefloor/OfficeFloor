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

import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} possibly instigated by a
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureFlowType {

	/**
	 * Obtains the name for the {@link ProcedureFlowType}.
	 * 
	 * @return Name for the {@link ProcedureFlowType}.
	 */
	String getFlowName();

	/**
	 * Obtains the type of the argument passed by the {@link Procedure} to the
	 * {@link Flow}.
	 * 
	 * @return Type of argument passed to {@link Flow}. May be <code>null</code> to
	 *         indicate no argument.
	 */
	Class<?> getArgumentType();

}
