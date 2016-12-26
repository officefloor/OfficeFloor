/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionMetaData<O extends Enum<O>, F extends Enum<F>>
		extends ManagedFunctionLogicMetaData {

	/**
	 * Obtains the {@link ManagedFunctionFactory} to create the
	 * {@link ManagedFunction} for this {@link ManagedFunctionMetaData}.
	 * 
	 * @return {@link ManagedFunctionFactory}
	 */
	ManagedFunctionFactory<O, F> getManagedFunctionFactory();

	/**
	 * Obtains the differentiator for the {@link ManagedFunction}.
	 * 
	 * @return Differentiator or <code>null</code> if no differentiator.
	 */
	Object getDifferentiator();

	/**
	 * Obtains the parameter type for the {@link ManagedFunction}.
	 * 
	 * @return Parameter type for the {@link ManagedFunction}. May be
	 *         <code>null</code> to indicate no parameter.
	 */
	Class<?> getParameterType();

	/**
	 * Obtains the {@link ManagedObjectIndex} instances identifying the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link ManagedFunction} may be executed.
	 * 
	 * @return Listing of {@link ManagedObjectIndex} instances.
	 */
	ManagedObjectIndex[] getRequiredManagedObjects();

	/**
	 * Obtains the activation flags for the {@link Governance}. The index into
	 * the array identifies the {@link Governance} for the respective activation
	 * flag.
	 * 
	 * @return Activation flags for the {@link Governance}.
	 */
	boolean[] getRequiredGovernance();

	/**
	 * Obtains the {@link FlowMetaData} of the specified {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 * @return {@link FlowMetaData} of the specified {@link Flow}.
	 */
	FlowMetaData getFlow(int flowIndex);

	/**
	 * Creates a {@link ManagedFunction}.
	 * 
	 * @param flow
	 *            {@link Flow}.
	 * @param parallelFunctionOwner
	 *            Parallel {@link ManagedFunctionContainer} owner.
	 * @param parameter
	 *            Parameter.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @return {@link ManagedObjectContainer} containing the
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionContainer createManagedFunctionContainer(Flow flow, ManagedFunctionContainer parallelFunctionOwner,
			Object parameter, GovernanceDeactivationStrategy governanceDeactivationStrategy);

	/**
	 * Meta-data of the {@link Duty} to undertake before executing the
	 * {@link ManagedFunction}.
	 * 
	 * @return Listing of the {@link Duty} instances to undertake before
	 *         executing the {@link ManagedFunction}.
	 */
	ManagedFunctionDutyAssociation<?>[] getPreAdministrationMetaData();

	/**
	 * Meta-data of the {@link Administrator} to undertake after executing the
	 * {@link ManagedFunction}.
	 * 
	 * @return Listing the {@link Duty} instances to undertake after executing
	 *         the {@link ManagedFunction}.
	 */
	ManagedFunctionDutyAssociation<?>[] getPostAdministrationMetaData();

}