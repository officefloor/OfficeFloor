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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Configuration for a {@link Duty}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyConfiguration<A extends Enum<A>> {

	/**
	 * Obtains name identifying the {@link Duty} of the {@link Administrator}.
	 * 
	 * @return Name identifying the {@link Duty} on the {@link Administrator}.
	 */
	String getDutyName();

	/**
	 * Obtains the configuration for the {@link Flow} instances invoked by the
	 * {@link Duty}.
	 * 
	 * @return {@link TaskNodeReference} specifying the first {@link ManagedFunction} of
	 *         the linked {@link Flow}.
	 */
	TaskNodeReference[] getLinkedProcessConfiguration();

	/**
	 * Obtains the configuration for the linked {@link Governance}.
	 * 
	 * @return {@link DutyGovernanceConfiguration} specifying the linked
	 *         {@link Governance}.
	 */
	DutyGovernanceConfiguration<?>[] getGovernanceConfiguration();

}