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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.function.ManagedObjectFunctionEnhancer;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceProperties;

/**
 * Configuration of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceConfiguration<F extends Enum<F>, MS extends ManagedObjectSource<?, F>> {

	/**
	 * Obtains the name of this {@link ManagedObjectSource}.
	 * 
	 * @return Name of this {@link ManagedObjectSource}.
	 */
	String getManagedObjectSourceName();

	/**
	 * Obtains the {@link ManagedObjectSource} instance to use.
	 * 
	 * @return {@link ManagedObjectSource} instance to use. This may be
	 *         <code>null</code> and therefore the
	 *         {@link #getManagedObjectSourceClass()} should be used to obtain the
	 *         {@link ManagedObjectSource}.
	 */
	MS getManagedObjectSource();

	/**
	 * Obtains the {@link Class} of the {@link ManagedObjectSource}.
	 * 
	 * @return {@link Class} of the {@link ManagedObjectSource}. Will be
	 *         <code>null</code> if a {@link ManagedObjectSource} instance is
	 *         configured.
	 */
	Class<MS> getManagedObjectSourceClass();

	/**
	 * Obtains the additional profiles.
	 * 
	 * @return Additional profiles.
	 */
	String[] getAdditionalProfiles();

	/**
	 * Obtains the {@link SourceProperties} to initialise the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link SourceProperties} to initialise the
	 *         {@link ManagedObjectSource}.
	 */
	SourceProperties getProperties();

	/**
	 * Obtains the {@link ManagedObjectFunctionEnhancer} instances.
	 * 
	 * @return {@link ManagedObjectFunctionEnhancer} instances.
	 */
	ManagedObjectFunctionEnhancer[] getManagedObjectFunctionEnhancers();

	/**
	 * Obtains the {@link ManagingOfficeConfiguration} detailing the {@link Office}
	 * responsible for managing this {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagingOfficeConfiguration}.
	 */
	ManagingOfficeConfiguration<F> getManagingOfficeConfiguration();

	/**
	 * Obtains the {@link ManagedObjectPoolConfiguration} for this
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectPoolConfiguration} for this
	 *         {@link ManagedObjectSource} or <code>null</code> if not to be pooled.
	 */
	ManagedObjectPoolConfiguration getManagedObjectPoolConfiguration();

	/**
	 * Obtains the timeout to:
	 * <ol>
	 * <li>to source the {@link ManagedObject}</li>
	 * <li>have asynchronous operations on the {@link ManagedObject} complete</li>
	 * </ol>
	 * 
	 * @return Timeout.
	 */
	long getTimeout();

	/**
	 * Obtains the names of the {@link ManagedObjectSource} to start up before.
	 * 
	 * @return Names of the {@link ManagedObjectSource} to start up before.
	 */
	String[] getStartupBefore();

	/**
	 * Obtains the names of the {@link ManagedObjectSource} to start up after.
	 * 
	 * @return Names of the {@link ManagedObjectSource} to start up after.
	 */
	String[] getStartupAfter();

}
