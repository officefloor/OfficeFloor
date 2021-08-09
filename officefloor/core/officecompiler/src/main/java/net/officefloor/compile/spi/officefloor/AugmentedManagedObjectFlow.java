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

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;

/**
 * Augmented {@link ManagedObjectFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedManagedObjectFlow {

	/**
	 * Obtains the name of this {@link ManagedObjectFlow}.
	 * 
	 * @return Name of this {@link ManagedObjectFlow}.
	 */
	String getManagedObjectFlowName();

	/**
	 * Indicates if the {@link ManagedObjectFlow} is already linked.
	 * 
	 * @return <code>true</code> if already linked.
	 */
	boolean isLinked();

}
