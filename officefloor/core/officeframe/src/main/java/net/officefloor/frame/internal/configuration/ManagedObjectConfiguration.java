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

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Configuration of a {@link ProcessState} or {@link ThreadState} bound
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectConfiguration<O extends Enum<O>> {

	/**
	 * Obtains the name of the {@link ManagedObject} registered within the
	 * {@link Office}.
	 * 
	 * @return Name of the {@link ManagedObject} registered within the
	 *         {@link Office}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * Obtains name of the {@link ManagedObject} bound to either
	 * {@link ProcessState} or {@link ThreadState}.
	 * 
	 * @return Name of the {@link ManagedObject} bound to either
	 *         {@link ProcessState} or {@link ThreadState}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the listing of {@link ManagedObjectDependencyConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectDependencyConfiguration} instances.
	 */
	ManagedObjectDependencyConfiguration<O>[] getDependencyConfiguration();

	/**
	 * Obtains the listing of {@link ManagedObjectGovernanceConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectGovernanceConfiguration} instances.
	 */
	ManagedObjectGovernanceConfiguration[] getGovernanceConfiguration();

	/**
	 * Obtains the listing of the {@link Administration} to be done before the
	 * {@link ManagedObject} is loaded.
	 * 
	 * @return Listing of the {@link Administration} to be done before the
	 *         {@link ManagedObject} is loaded.
	 */
	AdministrationConfiguration<?, ?, ?>[] getPreLoadAdministration();

	/**
	 * Obtains the {@link ThreadLocalConfiguration}.
	 * 
	 * @return {@link ThreadLocalConfiguration} or <code>null</code> if not bound to
	 *         {@link Thread}.
	 */
	ThreadLocalConfiguration getThreadLocalConfiguration();

}
