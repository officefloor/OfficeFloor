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

package net.officefloor.compile.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * Direction of {@link AutoWire}.
 * 
 * @author Daniel Sagenschneider
 */
public enum AutoWireDirection {

	/**
	 * <p>
	 * Flags that the source requires to use the target. Hence, target must be child
	 * of source.
	 * <p>
	 * This is typically used in {@link ManagedObject} auto-wirings to provide
	 * dependent {@link ManagedObject}.
	 */
	SOURCE_REQUIRES_TARGET,

	/**
	 * <p>
	 * Flags that the target categories the source. Hence, source must be child of
	 * target.
	 * <p>
	 * This is typically used in {@link Team} auto-wirings to assign
	 * {@link ManagedFunction} to {@link Team}.
	 */
	TARGET_CATEGORISES_SOURCE
}
