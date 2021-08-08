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
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Configuration of the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationConfiguration<E, F extends Enum<F>, G extends Enum<G>> extends FunctionConfiguration<F> {

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	String getAdministrationName();

	/**
	 * Obtains the {@link AdministrationFactory}.
	 * 
	 * @return {@link AdministrationFactory}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the extension interface.
	 * 
	 * @return Extension interface.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the names of the {@link ManagedObject} instances to be administered.
	 * 
	 * @return Names of the {@link ManagedObject} instances to be administered.
	 */
	String[] getAdministeredManagedObjectNames();

	/**
	 * Obtains the configuration for the linked {@link Governance}.
	 * 
	 * @return {@link AdministrationGovernanceConfiguration} specifying the linked
	 *         {@link Governance}.
	 */
	AdministrationGovernanceConfiguration<?>[] getGovernanceConfiguration();

	/**
	 * Obtains the timeout for any {@link AsynchronousFlow} instigated.
	 * 
	 * @return Timeout for any {@link AsynchronousFlow} instigated.
	 */
	long getAsynchronousFlowTimeout();

}
