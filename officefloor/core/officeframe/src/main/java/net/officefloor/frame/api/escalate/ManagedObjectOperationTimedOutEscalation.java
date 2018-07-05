/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Escalation} indicating that an operation by the {@link ManagedObject}
 * was timed out.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectOperationTimedOutEscalation extends
		ManagedObjectEscalation {

	/**
	 * Initiate.
	 * 
	 * @param objectType
	 *            {@link Class} of the {@link Object} returned from the
	 *            {@link ManagedObject} which had its asynchronous operation
	 *            timeout.
	 */
	public ManagedObjectOperationTimedOutEscalation(Class<?> objectType) {
		super(objectType);
	}

}