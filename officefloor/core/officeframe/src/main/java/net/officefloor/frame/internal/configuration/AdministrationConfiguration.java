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

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.AdministrationDuty;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Configuration of the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationConfiguration<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	String getAdministratorName();

	/**
	 * Obtains the {@link AdministrationFactory}.
	 * 
	 * @return {@link AdministrationFactory}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the name of the {@link Team} within the {@link Office}
	 * responsible for completing the {@link AdministrationDuty} instances of
	 * this {@link Administration}.
	 * 
	 * @return {@link Office} name of the {@link Team}. May be <code>null</code>
	 *         to use any {@link Team}.
	 */
	String getOfficeTeamName();

	/**
	 * Obtains the names of the {@link ManagedObject} instances to be
	 * administered.
	 * 
	 * @return Names of the {@link ManagedObject} instances to be administered.
	 */
	String[] getAdministeredManagedObjectNames();

	/**
	 * Obtains the configuration for the {@link Flow} instances invoked by the
	 * {@link Administration}.
	 * 
	 * @return {@link ManagedFunctionReference} specifying the first
	 *         {@link ManagedFunction} of the linked {@link Flow}.
	 */
	ManagedFunctionReference[] getFlowConfiguration();

	/**
	 * Obtains the configuration for the linked {@link Governance}.
	 * 
	 * @return {@link AdministrationGovernanceConfiguration} specifying the
	 *         linked {@link Governance}.
	 */
	AdministrationGovernanceConfiguration<?>[] getGovernanceConfiguration();

}