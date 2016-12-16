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

import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Container managing a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContainer {

	/**
	 * Loads the {@link ManagedObject}.
	 * 
	 * @param jobContext
	 *            {@link JobContext}.
	 * @return Optional {@link JobNode} to load the {@link ManagedObject}.
	 */
	JobNode loadManagedObject(JobContext jobContext);

	/**
	 * Obtains the object being managed by the {@link ManagedObject}.
	 * 
	 * @return Object being managed by the {@link ManagedObject}.
	 */
	Object getObject(ThreadState threadState);

	/**
	 * Extracts the extension interface from the {@link ManagedObject} within
	 * this {@link ManagedObjectContainer}.
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param extractor
	 *            {@link ExtensionInterfaceExtractor} to extract the extension
	 *            interface from the {@link ManagedObject}.
	 * @return Extracted extension interface.
	 */
	<I extends Object> I extractExtensionInterface(ExtensionInterfaceExtractor<I> extractor);

	/**
	 * Unregisters this {@link ManagedObject} from {@link Governance}.
	 * 
	 * @param governance
	 *            {@link ActiveGovernance}.
	 * @return Optional {@link JobNode} to unregister the {@link ManagedObject}
	 *         from {@link Governance}.
	 */
	JobNode unregisterManagedObjectFromGovernance(ActiveGovernance<?, ?> governance);

	/**
	 * Unloads the {@link ManagedObject}.
	 * 
	 * @return Optional {@link JobNode} to unload the {@link ManagedObject}.
	 */
	JobNode unloadManagedObject();

}