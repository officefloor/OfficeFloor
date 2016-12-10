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
package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.internal.configuration.TaskGovernanceConfiguration;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link TaskGovernanceConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskGovernanceConfigurationImpl implements
		TaskGovernanceConfiguration {

	/**
	 * {@link Governance} name.
	 */
	private final String governanceName;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            {@link Governance} name.
	 */
	public TaskGovernanceConfigurationImpl(String governanceName) {
		this.governanceName = governanceName;
	}

	/*
	 * ===================== TaskGovernanceConfiguration =================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

}