/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
