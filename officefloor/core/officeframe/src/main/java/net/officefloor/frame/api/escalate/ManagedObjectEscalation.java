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
 * <p>
 * {@link Escalation} from managing a {@link ManagedObject}.
 * <p>
 * This enables generic handling of {@link ManagedObject} {@link Escalation}
 * failures.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ManagedObjectEscalation extends Escalation {

	/**
	 * {@link Class} of the {@link Object} returned from the failed
	 * {@link ManagedObject}.
	 */
	private final Class<?> objectType;

	/**
	 * Initiate.
	 * 
	 * @param objectType
	 *            {@link Class} of the {@link Object} returned from the failed
	 *            {@link ManagedObject}.
	 */
	public ManagedObjectEscalation(Class<?> objectType) {
		this.objectType = objectType;
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param objectType
	 *            {@link Class} of the {@link Object} returned from the failed
	 *            {@link ManagedObject}.
	 * @param cause
	 *            Cause of the {@link Escalation}.
	 */
	public ManagedObjectEscalation(Class<?> objectType, Throwable cause) {
		super(cause);
		this.objectType = objectType;
	}

	/**
	 * Obtains the {@link Class} of the {@link Object} returned from the failed
	 * {@link ManagedObject}.
	 * 
	 * @return {@link Class} of the {@link Object} returned from the failed
	 *         {@link ManagedObject}.
	 */
	public Class<?> getObjectType() {
		return this.objectType;
	}

}