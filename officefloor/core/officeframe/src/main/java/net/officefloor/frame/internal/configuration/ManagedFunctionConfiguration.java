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
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Configuration of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionConfiguration<O extends Enum<O>, F extends Enum<F>> extends FunctionConfiguration<F> {

	/**
	 * Obtains the name of this {@link ManagedFunction}.
	 * 
	 * @return Name of this {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionFactory} for the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionFactory}.
	 */
	ManagedFunctionFactory<O, F> getManagedFunctionFactory();

	/**
	 * Obtains the annotations for the {@link ManagedFunction}.
	 * 
	 * @return Annotations for the {@link ManagedFunction}.
	 */
	Object[] getAnnotations();

	/**
	 * Obtains the configuration of the dependent {@link Object} instances for this
	 * {@link ManagedFunction}.
	 * 
	 * @return Configuration of the dependent {@link Object} instances for this
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionObjectConfiguration<O>[] getObjectConfiguration();

	/**
	 * Obtains the configuration of the {@link ManagedFunction} bound
	 * {@link ManagedObject} instances.
	 * 
	 * @return Listing of the {@link ManagedObject} configuration for this
	 *         {@link ManagedFunction}.
	 */
	ManagedObjectConfiguration<?>[] getManagedObjectConfiguration();

	/**
	 * Obtains the configuration of the {@link Governance} instances for this
	 * {@link ManagedFunction}.
	 * 
	 * @return Configuration of the {@link Governance} for this
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionGovernanceConfiguration[] getGovernanceConfiguration();

	/**
	 * Obtains the listing of the {@link Administration} to be done before the
	 * {@link ManagedFunction} is executed.
	 * 
	 * @return Listing of the {@link Administration} to be done before the
	 *         {@link ManagedFunction} is executed.
	 */
	AdministrationConfiguration<?, ?, ?>[] getPreAdministration();

	/**
	 * Obtains the listing of the {@link Administration} to be done after the
	 * {@link ManagedFunction} is executed.
	 * 
	 * @return Listing of the {@link Administration} to be done after the
	 *         {@link ManagedFunction} is executed.
	 */
	AdministrationConfiguration<?, ?, ?>[] getPostAdministration();

	/**
	 * Obtains the reference to the next {@link ManagedFunction}.
	 * 
	 * @return Reference to the next {@link ManagedFunction}.
	 */
	ManagedFunctionReference getNextFunction();

	/**
	 * Obtains the timeout for any {@link AsynchronousFlow} instigated.
	 * 
	 * @return Timeout for any {@link AsynchronousFlow} instigated.
	 */
	long getAsynchronousFlowTimeout();

}
