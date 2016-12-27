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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionConfiguration<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the name of this {@link ManagedFunction}.
	 * 
	 * @return Name of this {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionFactory} for the
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionFactory}.
	 */
	ManagedFunctionFactory<O, F> getTaskFactory();

	/**
	 * Obtains the differentiator for the {@link ManagedFunction}.
	 * 
	 * @return Differentiator or <code>null</code> if no differentiator for the
	 *         {@link ManagedFunction}.
	 */
	Object getDifferentiator();

	/**
	 * Obtains the name of {@link Office} registered {@link Team} responsible
	 * for completing this {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link Office} registered {@link Team}. May be
	 *         <code>null</code> to use any {@link Team}.
	 */
	String getOfficeTeamName();

	/**
	 * Obtains the configuration of the {@link ManagedFunction} bound
	 * {@link ManagedObject} instances.
	 * 
	 * @return Listing of the {@link ManagedObject} configuration for this
	 *         {@link ManagedFunction}.
	 */
	ManagedObjectConfiguration<?>[] getManagedObjectConfiguration();

	/**
	 * Obtains the configuration of the {@link ManagedFunction} bound
	 * {@link Administrator} instances.
	 * 
	 * @return Listing of {@link Administrator} configuration this
	 *         {@link ManagedFunction}.
	 */
	AdministratorSourceConfiguration<?, ?>[] getAdministratorConfiguration();

	/**
	 * Obtains the reference to the next {@link ManagedFunction}.
	 * 
	 * @return Reference to the next {@link ManagedFunction}.
	 */
	ManagedFunctionReference getNextFunction();

	/**
	 * Obtains the configuration of the {@link Flow} instances for this
	 * {@link ManagedFunction}.
	 * 
	 * @return Configuration of {@link Flow} instances for this
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionFlowConfiguration<F>[] getFlowConfiguration();

	/**
	 * Obtains the configuration of the dependent {@link Object} instances for
	 * this {@link ManagedFunction}.
	 * 
	 * @return Configuration of the dependent {@link Object} instances for this
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionObjectConfiguration<O>[] getObjectConfiguration();

	/**
	 * Obtains the configuration of the {@link Governance} instances for this
	 * {@link ManagedFunction}.
	 * 
	 * @return Configuration of the {@link Governance} for this
	 *         {@link ManagedFunction}.
	 */
	ManagedFunctionGovernanceConfiguration[] getGovernanceConfiguration();

	/**
	 * Obtains the listing of the {@link ManagedFunctionDutyConfiguration} for
	 * the administration to be done before the {@link ManagedFunction} is
	 * executed.
	 * 
	 * @return Listing of the {@link ManagedFunctionDutyConfiguration} for the
	 *         administration to be done before the {@link ManagedFunction} is
	 *         executed.
	 */
	ManagedFunctionDutyConfiguration<?>[] getPreFunctionAdministratorDutyConfiguration();

	/**
	 * Obtains the listing of the {@link ManagedFunctionDutyConfiguration} for
	 * the administration to be done after the {@link ManagedFunction} is
	 * executed.
	 * 
	 * @return Listing of the {@link ManagedFunctionDutyConfiguration} for the
	 *         administration to be done after the {@link ManagedFunction} is
	 *         executed.
	 */
	ManagedFunctionDutyConfiguration<?>[] getPostFunctionAdministratorDutyConfiguration();

	/**
	 * Obtains the {@link ManagedFunctionEscalationConfiguration} instances in
	 * escalation order. Index 0 being first, index 1 second and so forth.
	 * 
	 * @return {@link ManagedFunctionEscalationConfiguration} instances.
	 */
	ManagedFunctionEscalationConfiguration[] getEscalations();

}