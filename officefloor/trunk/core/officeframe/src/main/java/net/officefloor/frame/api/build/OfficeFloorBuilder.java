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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Builder of an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorBuilder {

	/**
	 * Allows overriding the {@link ClassLoader} provided to the sources by the
	 * {@link SourceContext}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	void setClassLoader(ClassLoader classLoader);

	/**
	 * Adds a {@link ResourceSource} to locate resources.
	 * 
	 * @param resourceSource
	 *            {@link ResourceSource}.
	 */
	void addResources(ResourceSource resourceSource);

	/**
	 * Adds a {@link ManagedObjectSource} to this {@link OfficeFloorBuilder}.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param <MS>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass
	 *            Class of the {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> addManagedObject(
			String managedObjectSourceName, Class<MS> managedObjectSourceClass);

	/**
	 * Adds a {@link ManagedObjectSource} to this {@link OfficeFloorBuilder}.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance to use.
	 * @return {@link ManagedObjectBuilder}.
	 */
	<D extends Enum<D>, F extends Enum<F>> ManagedObjectBuilder<F> addManagedObject(
			String managedObjectSourceName,
			ManagedObjectSource<D, F> managedObjectSource);

	/**
	 * Adds a {@link Team} which will execute {@link JobNode} instances within
	 * this {@link OfficeFloor}.
	 * 
	 * @param <TS>
	 *            {@link TeamSource} type.
	 * @param teamName
	 *            Name to register the {@link Team} under.
	 * @param teamSourceClass
	 *            {@link TeamSource} to source the {@link Team}.
	 * @return {@link TeamBuilder} to build the {@link Team}.
	 */
	<TS extends TeamSource> TeamBuilder<TS> addTeam(String teamName,
			Class<TS> teamSourceClass);

	/**
	 * Adds an {@link Office} on the {@link OfficeFloor}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return {@link OfficeBuilder} to build the {@link Office}.
	 */
	OfficeBuilder addOffice(String officeName);

	/**
	 * Specifies the {@link EscalationHandler} for issues escalating out of the
	 * {@link Office} instances.
	 * 
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 */
	void setEscalationHandler(EscalationHandler escalationHandler);

	/**
	 * Builds the {@link OfficeFloor}.
	 * 
	 * @param issuesListener
	 *            {@link OfficeFloorIssues} to listen for issues in constructing
	 *            the {@link OfficeFloor}.
	 * @return Built {@link OfficeFloor} if successfully built, or
	 *         <code>null</code> if could not construct {@link OfficeFloor} with
	 *         reasons passed to the {@link OfficeFloorIssues}.
	 */
	OfficeFloor buildOfficeFloor(OfficeFloorIssues issuesListener);

	/**
	 * Builds the {@link OfficeFloor}.
	 * 
	 * @return Built {@link OfficeFloor}.
	 * @throws OfficeFloorBuildException
	 *             If fails to build the {@link OfficeFloor}.
	 * 
	 * @see OfficeFloorBuildException
	 */
	OfficeFloor buildOfficeFloor() throws OfficeFloorBuildException;

}