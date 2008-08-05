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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration of an Office.
 * 
 * @author Daniel
 */
public interface OfficeConfiguration {

	/**
	 * Obtains the name of this {@link Office}.
	 * 
	 * @return Name of this {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Obtains the registered {@link Team} links.
	 * 
	 * @return Registered {@link Team} links.
	 */
	LinkedTeamConfiguration[] getRegisteredTeams();

	/**
	 * Obtains the registered {@link ManagedObject} links.
	 * 
	 * @return Registered {@link ManagedObject} links.
	 */
	LinkedManagedObjectConfiguration[] getRegisteredManagedObjects();

	/**
	 * Obtains the {@link ManagedObject} instances to be bound to the
	 * {@link ProcessState} of this {@link Office}.
	 * 
	 * @return Listing of the {@link ManagedObject} instances.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	ManagedObjectConfiguration[] getManagedObjectConfiguration()
			throws ConfigurationException;

	/**
	 * Obtains the configuration of the {@link Work} instances.
	 * 
	 * @return {@link Work} configuration for the input name.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	<W extends Work> WorkConfiguration<W>[] getWorkConfiguration()
			throws ConfigurationException;

	/**
	 * Obtains the {@link OfficeEnhancer} instances for this {@link Office}.
	 * 
	 * @return Listing of the {@link OfficeEnhancer} for this {@link Office}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	OfficeEnhancer[] getOfficeEnhancers() throws ConfigurationException;

	/**
	 * Obtains the {@link EscalationHandler} for the {@link Office}.
	 * 
	 * @return {@link EscalationHandler} for the {@link Office}. May be
	 *         <code>null</code>.
	 */
	EscalationHandler getOfficeEscalationHandler();

	/**
	 * <p>
	 * Obtains a {@link FlowNodeBuilder} registered with this
	 * {@link OfficeBuilder}.
	 * <p>
	 * This enables addition configuration of {@link Task} instances registered
	 * by a {@link ManagedObjectSource}.
	 * 
	 * @param namespace
	 *            Namespace. Likely the {@link ManagedObjectSource} name.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @return {@link TaskBuilder}.
	 * @throws ConfigurationException
	 *             If can not find the {@link FlowNodeBuilder}.
	 */
	FlowNodeBuilder<?> getFlowNodeBuilder(String namespace, String workName,
			String taskName) throws ConfigurationException;

	/**
	 * Obtains the configuration of the {@link AdministratorSource} instances.
	 * 
	 * @return {@link AdministratorSource} configuration.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	AdministratorSourceConfiguration<?, ?>[] getAdministratorSourceConfiguration()
			throws ConfigurationException;

	/**
	 * Obtains the list of {@link TaskNodeReference} instances referencing the
	 * {@link Task} instances to invoke on Office start up.
	 * 
	 * @return List of start up {@link TaskNodeReference} references.
	 */
	TaskNodeReference[] getStartupTasks();

}
