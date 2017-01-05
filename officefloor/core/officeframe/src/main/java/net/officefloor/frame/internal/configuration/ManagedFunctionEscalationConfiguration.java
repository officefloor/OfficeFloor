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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * Configuration for the {@link EscalationFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionEscalationConfiguration {

	/**
	 * Obtains the type of cause handled by this {@link EscalationFlow}.
	 * 
	 * @return Type of cause handled by this {@link EscalationFlow}.
	 */
	Class<? extends Throwable> getTypeOfCause();

	/**
	 * Obtains the {@link ManagedFunctionReference} for the
	 * {@link ManagedFunction} handling the {@link Escalation}.
	 * 
	 * @return {@link ManagedFunctionReference} for the {@link ManagedFunction}
	 *         handling the {@link Escalation}.
	 */
	ManagedFunctionReference getManagedFunctionReference();

}
