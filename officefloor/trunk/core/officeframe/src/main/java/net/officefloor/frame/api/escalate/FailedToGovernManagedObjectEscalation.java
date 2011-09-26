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
package net.officefloor.frame.api.escalate;

import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link Escalation} if failure of setting up {@link Governance} for the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class FailedToGovernManagedObjectEscalation extends
		ManagedObjectEscalation {

	/**
	 * Initiate.
	 * 
	 * @param objectType
	 *            {@link Class} of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 * @param cause
	 *            Cause from the {@link ManagedObjectSource} on why
	 *            {@link Governance} could not be set up for the
	 *            {@link ManagedObject}.
	 */
	public FailedToGovernManagedObjectEscalation(Class<?> objectType,
			Throwable cause) {
		super(objectType, cause);
	}

}