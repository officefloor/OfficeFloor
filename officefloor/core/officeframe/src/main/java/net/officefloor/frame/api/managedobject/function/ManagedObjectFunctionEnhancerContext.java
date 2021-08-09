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

package net.officefloor.frame.api.managedobject.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;

/**
 * Context for the {@link ManagedObjectFunctionEnhancer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionEnhancerContext {

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
	ManagedFunctionFactory<?, ?> getManagedFunctionFactory();

	/**
	 * Indicates if using the {@link ManagedObject} from the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return <code>true</code> if using the {@link ManagedObject} from the
	 *         {@link ManagedObjectSource}.
	 */
	boolean isUsingManagedObject();

	/**
	 * Obtains the {@link ManagedObjectFunctionDependency} instances for the
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedObjectFunctionDependency} instances for the
	 *         {@link ManagedFunction}.
	 */
	ManagedObjectFunctionDependency[] getFunctionDependencies();

	/**
	 * Obtains the name of the responsible {@link Team}.
	 * 
	 * @return Name of the responsible {@link Team} or <code>null</code> if
	 *         {@link Team} assigned.
	 */
	String getResponsibleTeam();

	/**
	 * Specifies the responsible {@link Team}.
	 * 
	 * @param teamName Name of the responsible {@link Team}.
	 */
	void setResponsibleTeam(String teamName);

}
