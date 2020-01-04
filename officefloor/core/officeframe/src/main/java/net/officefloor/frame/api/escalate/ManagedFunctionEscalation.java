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

package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * <p>
 * {@link Escalation} from managing a {@link ManagedFunction}.
 * <p>
 * This enables generic handling of {@link ManagedFunction} {@link Escalation}
 * failures.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ManagedFunctionEscalation extends Escalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String managedFunctionName;

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionName Name of the {@link ManagedFunction}.
	 */
	public ManagedFunctionEscalation(String managedFunctionName) {
		this.managedFunctionName = managedFunctionName;
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param managedFunctionName Name of the {@link ManagedFunction}.
	 * @param cause               Cause of the {@link Escalation}.
	 */
	public ManagedFunctionEscalation(String managedFunctionName, Throwable cause) {
		super(cause);
		this.managedFunctionName = managedFunctionName;
	}

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	public String getManagedFunctionName() {
		return this.managedFunctionName;
	}

}
