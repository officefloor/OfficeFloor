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
package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.impl.execute.function.AbstractManagedFunctionContainer;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Propagates the {@link Escalation} to allow the {@link AbstractManagedFunctionContainer}
 * to catch and handle it.
 * <p>
 * The {@link Escalation} (/{@link Throwable}) may be obtained from
 * {@link #getCause()}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropagateEscalationError extends Error {

	/**
	 * Initiate.
	 * 
	 * @param escalation
	 *            {@link Throwable} to be propagated to the
	 *            {@link AbstractManagedFunctionContainer} to be handled. Is
	 *            {@link Throwable} as may be failures from a
	 *            {@link ManagedObjectSource} that requires to be handled.
	 */
	public PropagateEscalationError(Throwable escalation) {
		super(escalation);
	}

}