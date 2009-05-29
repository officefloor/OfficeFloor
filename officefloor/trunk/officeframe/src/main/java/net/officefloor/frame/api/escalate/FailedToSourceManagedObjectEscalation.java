/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.api.escalate;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * {@link Escalation} that the {@link ManagedObjectSource} indicated a failure
 * and could not provide a {@link ManagedObject} or its corresponding
 * {@link Object} for processing.
 * <p>
 * The failure to source the {@link ManagedObject} can be obtained from
 * {@link #getCause()}.
 * 
 * @author Daniel Sagenschneider
 */
public class FailedToSourceManagedObjectEscalation extends
		ManagedObjectEscalation {

	/**
	 * Initiate.
	 * 
	 * @param objectType
	 *            {@link Class} of the {@link Object} returned from the failed
	 *            {@link ManagedObject}.
	 * @param cause
	 *            Cause from the {@link ManagedObjectSource} on why it could not
	 *            source a {@link ManagedObject} or its corresponding
	 *            {@link Object} for processing.
	 */
	public FailedToSourceManagedObjectEscalation(Class<?> objectType,
			Throwable cause) {
		super(objectType, cause);
	}

}