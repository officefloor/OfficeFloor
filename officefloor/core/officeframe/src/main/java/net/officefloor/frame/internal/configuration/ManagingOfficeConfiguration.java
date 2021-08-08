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

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Configuration of the {@link ManagedObjectSource} being managed by an
 * {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagingOfficeConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Office} managing this
	 * {@link ManagedObjectSource}.
	 *
	 * @return Name of the {@link Office} managing this {@link ManagedObjectSource}.
	 */
	String getOfficeName();

	/**
	 * Obtains the {@link InputManagedObjectConfiguration} to bind the input
	 * {@link ManagedObject} to the {@link ProcessState}.
	 *
	 * @return {@link InputManagedObjectConfiguration} to bind the input
	 *         {@link ManagedObject} to the {@link ProcessState}.
	 */
	InputManagedObjectConfiguration<?> getInputManagedObjectConfiguration();

	/**
	 * <p>
	 * Obtains the {@link ManagingOfficeBuilder} for this
	 * {@link ManagedObjectSource}.
	 * <p>
	 * This is to enable the {@link ManagedObjectSource} to provide additional
	 * configuration for itself.
	 *
	 * @return {@link ManagingOfficeBuilder}.
	 */
	ManagingOfficeBuilder<F> getBuilder();

	/**
	 * Obtains the {@link ManagedObjectFunctionDependencyConfiguration} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectFunctionDependencyConfiguration} for the
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectFunctionDependencyConfiguration[] getFunctionDependencyConfiguration();

	/**
	 * Obtains the {@link ManagedObjectFlowConfiguration} for the
	 * {@link ManagedObjectSource}.
	 *
	 * @return {@link ManagedObjectFlowConfiguration} for the
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectFlowConfiguration<F>[] getFlowConfiguration();

	/**
	 * Obtains the {@link ManagedObjectExecutionConfiguration} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectExecutionConfiguration} for the
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectExecutionConfiguration[] getExecutionConfiguration();

}
