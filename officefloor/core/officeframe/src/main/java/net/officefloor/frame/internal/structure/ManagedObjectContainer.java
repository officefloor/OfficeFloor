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
package net.officefloor.frame.internal.structure;

import java.util.List;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * Container managing a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContainer {

	/**
	 * Obtains the {@link ThreadState} responsible for changes to this
	 * {@link ManagedObjectContainer}.
	 * 
	 * @return {@link ThreadState} responsible for changes to this
	 *         {@link ManagedObjectContainer}.
	 */
	ThreadState getResponsibleThreadState();

	/**
	 * Creates a {@link FunctionState} to load the {@link ManagedObject}.
	 * 
	 * @param managedFunctionContainer
	 *            {@link ManagedFunctionContainer} requiring the
	 *            {@link ManagedObject}.
	 * @return Optional {@link FunctionState} to load the {@link ManagedObject}.
	 *         Should this return <code>null</code>, the
	 *         {@link ManagedFunctionContainer} should not then be executed, as it
	 *         is expecting to wait. This will return the
	 *         {@link ManagedFunctionContainer} when the {@link ManagedObject} is
	 *         loaded.
	 */
	FunctionState loadManagedObject(ManagedFunctionContainer managedFunctionContainer);

	/**
	 * <p>
	 * Creates a {@link FunctionState} to check if the {@link ManagedObject}
	 * contained within this {@link ManagedObjectContainer} is ready.
	 * <p>
	 * Should the {@link ManagedObject} not be ready, then will latch to wait for
	 * the {@link ManagedObject} to be ready.
	 * 
	 * @param check
	 *            {@link ManagedObjectReadyCheck}.
	 * @return {@link FunctionState} to check if the {@link ManagedObject} contained
	 *         within this {@link ManagedObjectContainer} is ready.
	 */
	FunctionState checkReady(ManagedObjectReadyCheck check);

	/**
	 * <p>
	 * Extracts the {@link ManagedObject} extension from the {@link ManagedObject}
	 * contained in this {@link ManagedObjectContainer}.
	 * <p>
	 * Should the {@link ManagedObject} not be loaded, then no {@link ManagedObject}
	 * extension will be loaded.
	 *
	 * @param <E>
	 *            Extension type.
	 * @param extractor
	 *            {@link ManagedObjectExtensionExtractor}.
	 * @param managedObjectExtensions
	 *            {@link List} to load the {@link ManagedObject} extension.
	 * @param extensionIndex
	 *            Index within the {@link ManagedObject} extensions array to load
	 *            the extension.
	 * @param responsibleTeam
	 *            {@link TeamManagement} responsible for extracting the extension.
	 *            May be <code>null</code> to use any {@link Team}.
	 * @return {@link FunctionState} to load the {@link ManagedObject} extension.
	 */
	<E> FunctionState extractExtension(ManagedObjectExtensionExtractor<E> extractor, E[] managedObjectExtensions,
			int extensionIndex, TeamManagement responsibleTeam);

	/**
	 * Obtains the object being managed by the {@link ManagedObject}.
	 * 
	 * @return Object being managed by the {@link ManagedObject}.
	 */
	Object getObject();

	/**
	 * Unregisters the {@link ManagedObject} from {@link Governance}.
	 * 
	 * @param governanceIndex
	 *            Index of the {@link Governance}.
	 * @return {@link FunctionState} to unregister the {@link ManagedObject} from
	 *         {@link Governance}.
	 */
	FunctionState unregisterGovernance(int governanceIndex);

	/**
	 * Creates a {@link FunctionState} to unload the {@link ManagedObject}.
	 * 
	 * @return {@link FunctionState} to unload the {@link ManagedObject}.
	 */
	FunctionState unloadManagedObject();

}