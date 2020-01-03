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

package net.officefloor.frame.impl.construct.function;

import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;

/**
 * Implementation of the {@link EscalationConfiguration}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationConfigurationImpl implements EscalationConfiguration {

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
	public EscalationConfigurationImpl(Class<? extends Throwable> typeOfCause,
			ManagedFunctionReference taskNodeReference) {
		this.typeOfCause = typeOfCause;
		this.taskNodeReference = taskNodeReference;
	}

	/*
	 * ================= EscalationConfiguration ========================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public ManagedFunctionReference getManagedFunctionReference() {
		return this.taskNodeReference;
	}

}
