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

package net.officefloor.compile.managedfunction;

import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} possibly instigated by a
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionFlowType<F extends Enum<F>> extends AnnotatedType {

	/**
	 * Obtains the name for the {@link ManagedFunctionFlowType}.
	 * 
	 * @return Name for the {@link ManagedFunctionFlowType}.
	 */
	String getFlowName();

	/**
	 * <p>
	 * Obtains the index for the {@link ManagedFunctionFlowType}.
	 * <p>
	 * Should there be an {@link Enum} then will be the {@link Enum#ordinal()}
	 * value. Otherwise will be the index that this was added.
	 * 
	 * @return Index for the {@link ManagedFunctionFlowType}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed by the {@link ManagedFunction} to the
	 * {@link Flow}.
	 * 
	 * @return Type of argument passed to {@link Flow}. May be <code>null</code> to
	 *         indicate no argument.
	 */
	Class<?> getArgumentType();

	/**
	 * Obtains the {@link Enum} key for the {@link ManagedFunctionFlowType}.
	 * 
	 * @return {@link Enum} key for the {@link ManagedFunctionFlowType}. May be
	 *         <code>null</code> if no {@link Enum} for flows.
	 */
	F getKey();

}
