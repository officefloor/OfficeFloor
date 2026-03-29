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

package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} instigated by the
 * {@link ManagedObjectSource} or one of its {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Flow}.
	 * 
	 * @return Name of the {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the key identifying the {@link Flow}.
	 * 
	 * @return Key identifying the {@link Flow}.
	 */
	F getKey();

	/**
	 * Obtains the index identifying the {@link Flow}.
	 * 
	 * @return Index identifying the {@link Flow}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed to the {@link Flow}.
	 * 
	 * @return Type of argument passed to the {@link Flow}. May be <code>null</code>
	 *         to indicate no argument.
	 */
	Class<?> getArgumentType();

}
