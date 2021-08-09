/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Potential {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedPotentialConnection {

	/**
	 * Obtains the source {@link Model} {@link Class}.
	 * 
	 * @return Source {@link Model} {@link Class}.
	 */
	Class<?> getSourceModelClass();

	/**
	 * Obtains the target {@link Model} {@link Class}.
	 * 
	 * @return Target {@link Model} {@link Class}.
	 */
	Class<?> getTargetModelClass();

	/**
	 * Indicates whether can create the {@link ConnectionModel}.
	 * 
	 * @return <code>true</code> if able to create the {@link ConnectionModel}.
	 */
	boolean canCreateConnection();

}
