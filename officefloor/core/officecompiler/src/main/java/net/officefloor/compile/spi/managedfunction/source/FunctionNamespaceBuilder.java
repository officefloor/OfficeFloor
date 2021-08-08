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

package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide the
 * {@link ManagedFunction} <code>type definition</code>s.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceBuilder {

	/**
	 * Adds a {@link ManagedFunctionTypeBuilder} to this
	 * {@link FunctionNamespaceBuilder} definition.
	 * 
	 * @param <M>             Dependency key type.
	 * @param <F>             Flow key type.
	 * @param functionName    Name of the {@link ManagedFunction}.
	 * @param objectKeysClass {@link Enum} providing the keys of the dependent
	 *                        {@link Object} instances required by the
	 *                        {@link ManagedFunctionTypeBuilder}. This may be
	 *                        <code>null</code> if the
	 *                        {@link ManagedFunctionTypeBuilder} requires no
	 *                        dependent {@link Object} instances or they are
	 *                        {@link Indexed}.
	 * @param flowKeysClass   {@link Enum} providing the keys of the {@link Flow}
	 *                        instigated by the {@link ManagedFunctionTypeBuilder}.
	 *                        This may be <code>null</code> if the
	 *                        {@link ManagedFunctionTypeBuilder} does not instigate
	 *                        {@link Flow} instances or they are {@link Indexed}.
	 * @return {@link ManagedFunctionTypeBuilder} to provide
	 *         <code>type definition</code> of the added {@link ManagedFunction}.
	 */
	<M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> addManagedFunctionType(String functionName,
			Class<M> objectKeysClass, Class<F> flowKeysClass);

}
