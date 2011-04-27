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

package net.officefloor.compile.test.managedobject;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Builder for the {@link ManagedObjectType} to validate the loaded
 * {@link ManagedObjectType} from the {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTypeBuilder {

	/**
	 * Specifies the {@link Object} class returned from the
	 * {@link ManagedObject}.
	 *
	 * @param objectClass
	 *            Class of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 */
	void setObjectClass(Class<?> objectClass);

	/**
	 * Adds a {@link ManagedObjectDependencyType}.
	 *
	 * @param name
	 *            Name of the {@link ManagedObjectDependency}.
	 * @param type
	 *            Type of the {@link ManagedObjectDependency}.
	 * @param index
	 *            Index of the {@link ManagedObjectDependency}.
	 * @param key
	 *            Key identifying the {@link ManagedObjectDependency}.
	 */
	void addDependency(String name, Class<?> type, int index, Enum<?> key);

	/**
	 * <p>
	 * Convenience method to add a {@link ManagedObjectDependencyType} based on
	 * the key.
	 * <p>
	 * Both the <code>name</code> and <code>index</code> are extracted from the
	 * key.
	 *
	 * @param key
	 *            Key identifying the {@link ManagedObjectDependency}.
	 * @param type
	 *            Type of the {@link ManagedObjectDependency}.
	 */
	void addDependency(Enum<?> key, Class<?> type);

	/**
	 * Adds a {@link ManagedObjectFlowType}.
	 *
	 * @param name
	 *            Name of the {@link ManagedObjectFlow}.
	 * @param argumentType
	 *            Type of argument passed from the {@link ManagedObjectFlow}.
	 * @param index
	 *            Index of the {@link ManagedObjectFlow}.
	 * @param key
	 *            Key identifying the {@link ManagedObjectFlow}.
	 * @param workName
	 *            Name of {@link Work} instigating the {@link Flow} or
	 *            <code>null</code> if done directly by
	 *            {@link ManagedObjectSource}.
	 * @param taskName
	 *            Name of {@link Task} instigating the {@link Flow} or
	 *            <code>null</code> if done directly by
	 *            {@link ManagedObjectSource}.
	 */
	void addFlow(String name, Class<?> argumentType, int index, Enum<?> key,
			String workName, String taskName);

	/**
	 * <p>
	 * Convenience method to add a {@link ManagedObjectFlowType} based on the
	 * key.
	 * <p>
	 * Both the <code>name</code> and <code>index</code> are extracted from the
	 * key.
	 *
	 * @param key
	 *            Key identifying the {@link ManagedObjectFlow}.
	 * @param argumentType
	 *            Type of argument passed from the {@link ManagedObjectFlow}.
	 * @param workName
	 *            Name of {@link Work} instigating the {@link Flow} or
	 *            <code>null</code> if done directly by
	 *            {@link ManagedObjectSource}.
	 * @param taskName
	 *            Name of {@link Task} instigating the {@link Flow} or
	 *            <code>null</code> if done directly by
	 *            {@link ManagedObjectSource}.
	 */
	void addFlow(Enum<?> key, Class<?> argumentType, String workName,
			String taskName);

	/**
	 * Adds a {@link ManagedObjectTeamType}.
	 *
	 * @param teamName
	 *            Name of the {@link ManagedObjectTeam}.
	 */
	void addTeam(String teamName);

	/**
	 * Adds an extension interface.
	 *
	 * @param extensionInterface
	 *            Extension interface.
	 */
	void addExtensionInterface(Class<?> extensionInterface);

}