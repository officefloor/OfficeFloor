/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.team.Team;

/**
 * Builds the {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public interface OfficeFloorBuilder {

	/**
	 * Adds a
	 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
	 * to this {@link OfficeFloorBuilder}.
	 * 
	 * @param id
	 *            Id to register the
	 *            {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
	 *            under.
	 * @param managedObjectBuilder
	 *            Builder of the
	 *            {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void addManagedObject(String id, ManagedObjectBuilder managedObjectBuilder)
			throws BuildException;

	/**
	 * Adds a {@link Team} which will execute
	 * {@link net.officefloor.frame.api.execute.Task} instances within this
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param id
	 *            Id to register the {@link Team} under.
	 * @param team
	 *            {@link Team} to execute
	 *            {@link net.officefloor.frame.api.execute.Task} instances.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void addTeam(String id, Team team) throws BuildException;

	/**
	 * Creates a new {@link OfficeBuilder}.
	 * 
	 * @return New {@link OfficeBuilder}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void addOffice(String id, OfficeBuilder officeBuilder)
			throws BuildException;

	/**
	 * Specifies the {@link EscalationProcedure} for issues of the offices.
	 * 
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void setEscalationProcedure(EscalationProcedure escalationProcedure)
			throws BuildException;
}
