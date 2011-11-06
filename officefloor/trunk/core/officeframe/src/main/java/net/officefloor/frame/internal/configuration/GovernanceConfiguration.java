/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration for the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceConfiguration<I, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? super I, F> getGovernanceFactory();

	/**
	 * Obtains the extension interface type for {@link ManagedObject} to provide
	 * to enable {@link Governance}.
	 * 
	 * @return Extension interface type for {@link ManagedObject} to provide to
	 *         enable {@link Governance}.
	 */
	Class<I> getExtensionInterface();

	/**
	 * Obtains the name of the {@link Team} to execute the {@link Task}
	 * instances for {@link Governance}.
	 * 
	 * @return Name of {@link Team}.
	 */
	String getTeamName();

}