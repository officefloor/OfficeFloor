/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Configuration for a {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name of this {@link Flow}.
	 * 
	 * @return Name of this {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the reference to the initial {@link ManagedFunction} of this
	 * {@link Flow}.
	 * 
	 * @return Reference to the initial {@link ManagedFunction} of this
	 *         {@link Flow}.
	 */
	ManagedFunctionReference getInitialFunction();

	/**
	 * Indicates whether to spawn a {@link ThreadState} for the {@link Flow}.
	 * 
	 * @return <code>true</code> to spawn a {@link ThreadState} for the
	 *         {@link Flow}.
	 */
	boolean isSpawnThreadState();

	/**
	 * Obtains the index identifying this {@link Flow}.
	 * 
	 * @return Index identifying this {@link Flow}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying this {@link Flow}.
	 * 
	 * @return Key identifying this {@link Flow}. <code>null</code> if indexed.
	 */
	F getKey();

}
