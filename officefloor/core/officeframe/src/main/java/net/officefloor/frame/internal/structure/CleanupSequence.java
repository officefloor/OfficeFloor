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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.CleanupEscalation;

/**
 * Sequence of {@link JobNode} instances to undertake clean-up.
 *
 * @author Daniel Sagenschneider
 */
public interface CleanupSequence {

	/**
	 * <p>
	 * Registers the {@link JobNode} that undertakes clean-up.
	 * <p>
	 * {@link JobNode} instances are executed in the order they are registered.
	 * 
	 * @param cleanupJob
	 *            Clean up {@link JobNode}.
	 * @return Optional {@link JobNode} to register the clean {@link JobNode}.
	 */
	JobNode registerCleanUpJob(JobNode cleanupJob);

	/**
	 * Obtains the registered {@link CleanupEscalation} instances.
	 * 
	 * @return {@link CleanupEscalation} instances.
	 */
	CleanupEscalation[] getCleanupEscalations();

	/**
	 * Registers a {@link Throwable} from attempting to cleanup the
	 * {@link ManagedObject} of the object type.
	 * 
	 * @param objectType
	 *            Object type of the {@link ManagedObject}.
	 * @param escalation
	 *            {@link Escalation}.
	 */
	void registerCleanupEscalation(Class<?> objectType, Throwable escalation);

}