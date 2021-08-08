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
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionType<M extends Enum<M>, F extends Enum<F>> extends AnnotatedType {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionFactory}.
	 * 
	 * @return {@link ManagedFunctionFactory}.
	 */
	ManagedFunctionFactory<M, F> getManagedFunctionFactory();

	/**
	 * Obtains the type name of {@link Object} returned from the
	 * {@link ManagedFunction} that is to be used as the argument to the next
	 * {@link ManagedFunction}.
	 * 
	 * @return Return type name of the {@link ManagedFunction}.
	 */
	Class<?> getReturnType();

	/**
	 * Obtains the {@link Enum} providing the keys for the dependent {@link Object}
	 * instances.
	 * 
	 * @return {@link Enum} providing the dependent {@link Object} keys or
	 *         <code>null</code> if {@link Indexed} or no dependencies.
	 */
	Class<M> getObjectKeyClass();

	/**
	 * Obtains the {@link ManagedFunctionObjectType} definitions for the dependent
	 * {@link Object} instances required by the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionObjectType} definitions for the dependent
	 *         {@link Object} instances required by the {@link ManagedFunction}.
	 */
	ManagedFunctionObjectType<M>[] getObjectTypes();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Flow} instances
	 * instigated by the {@link ManagedFunction}.
	 * 
	 * @return {@link Enum} providing instigated {@link Flow} keys or
	 *         <code>null</code> if {@link Indexed} or no instigated {@link Flow}
	 *         instances.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link ManagedFunctionFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionFlowType} definitions for the possible
	 *         {@link Flow} instances instigated by the {@link ManagedFunction}.
	 */
	ManagedFunctionFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link ManagedFunctionEscalationType} definitions for the
	 * possible {@link EscalationFlow} instances by the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link ManagedFunction}.
	 */
	ManagedFunctionEscalationType[] getEscalationTypes();

}
