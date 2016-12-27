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
package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.internal.configuration.GovernanceEscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;

/**
 * Implementation of the {@link GovernanceEscalationConfiguration}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceEscalationConfigurationImpl implements
		GovernanceEscalationConfiguration {

	/**
	 * Type of cause.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * {@link ManagedFunctionReference}.
	 */
	private final ManagedFunctionReference taskNodeReference;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause.
	 * @param taskNodeReference
	 *            {@link ManagedFunctionReference}.
	 */
	public GovernanceEscalationConfigurationImpl(
			Class<? extends Throwable> typeOfCause,
			ManagedFunctionReference taskNodeReference) {
		this.typeOfCause = typeOfCause;
		this.taskNodeReference = taskNodeReference;
	}

	/*
	 * =============== GovernanceEscalationConfiguration =====================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public ManagedFunctionReference getTaskNodeReference() {
		return this.taskNodeReference;
	}

}