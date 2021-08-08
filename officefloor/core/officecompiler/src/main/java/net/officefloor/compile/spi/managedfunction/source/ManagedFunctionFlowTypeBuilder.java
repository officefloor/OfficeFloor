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
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide a
 * <code>type definition</code> of a possible {@link Flow} instigated by the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionFlowTypeBuilder<F extends Enum<F>> {

	/**
	 * Obtains the index of the {@link Flow}.
	 * 
	 * @return Index of the {@link Flow}.
	 */
	int getIndex();

	/**
	 * Specifies the {@link Enum} for this {@link ManagedFunctionFlowTypeBuilder}.
	 * This is required to be set if <code>F</code> is not {@link None} or
	 * {@link Indexed}.
	 * 
	 * @param key {@link Enum} for this {@link ManagedFunctionFlowTypeBuilder}.
	 * @return <code>this</code>.
	 */
	ManagedFunctionFlowTypeBuilder<F> setKey(F key);

	/**
	 * <p>
	 * Specifies the type of the argument passed by the {@link ManagedFunction} to
	 * the {@link Flow}.
	 * <p>
	 * Should there be no argument, do not call this method.
	 * 
	 * @param argumentType Type of argument passed to {@link Flow}.
	 * @return <code>this</code>.
	 */
	ManagedFunctionFlowTypeBuilder<F> setArgumentType(Class<?> argumentType);

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link Flow}.
	 * <p>
	 * This need not be set as is only an aid to better identify the {@link Flow}.
	 * If not set the {@link ManagedFunctionTypeBuilder} will use the following
	 * order to get a display label:
	 * <ol>
	 * <li>{@link Enum} key name</li>
	 * <li>index value</li>
	 * </ol>
	 * 
	 * @param label Display label for the {@link Flow}.
	 * @return <code>this</code>.
	 */
	ManagedFunctionFlowTypeBuilder<F> setLabel(String label);

	/**
	 * Adds an annotation.
	 * 
	 * @param annotation Annotation.
	 * @return <code>this</code>.
	 */
	ManagedFunctionFlowTypeBuilder<F> addAnnotation(Object annotation);

}
