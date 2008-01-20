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
package net.officefloor.frame.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.impl.execute.TaskMetaDataImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw {@link net.officefloor.frame.internal.structure.TaskMetaData}.
 * 
 * @author Daniel
 */
public class RawTaskMetaData {

	/**
	 * Creates the registry of {@link RawTaskMetaData}.
	 * 
	 * @param taskConfiguration
	 *            {@link TaskConfiguration}.
	 * @param officeResources
	 *            Resources of the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param workMoRegistry
	 *            {@link RawWorkManagedObjectRegistry}.
	 * @param workAdminRegistry
	 *            {@link RawWorkAdministratorRegistry}.
	 * @return {@link RawTaskMetaData}.
	 * @throws Exception
	 *             If fails to create registry.
	 */
	@SuppressWarnings("unchecked")
	public static RawTaskMetaData createRawTaskMetaData(
			TaskConfiguration taskConfiguration,
			RawOfficeResourceRegistry officeResources,
			RawWorkManagedObjectRegistry workMoRegistry,
			RawWorkAdministratorRegistry workAdminRegistry) throws Exception {

		// Obtain the Team for the Task
		String teamId = taskConfiguration.getTeamId();
		Team team = officeResources.getTeam(teamId);
		if (team == null) {
			throw new ConfigurationException("Unknown team '" + teamId + "'");
		}

		// Obtain the managed object index translations
		TaskManagedObjectConfiguration[] tmoNamed = taskConfiguration
				.getManagedObjectConfiguration();
		int[] tmoToWmoTranslation = new int[tmoNamed.length];
		for (int i = 0; i < tmoToWmoTranslation.length; i++) {
			tmoToWmoTranslation[i] = workMoRegistry
					.getIndexByWorkManagedObjectName(tmoNamed[i]
							.getWorkManagedObjectName());
		}

		// Obtain the required managed objects
		// TODO: include the managed objects being administerred
		// TODO: include dependencies when implemented
		Set<Integer> requiredManagedObjectIndexes = new HashSet<Integer>();
		for (int workMoIndex : tmoToWmoTranslation) {
			// Obtain the raw work managed object meta-data
			RawWorkManagedObjectMetaData rawWorkMoMetaData = workMoRegistry
					.getRawWorkManagedObjectMetaData()[workMoIndex];
			for (int dependencyWorkIndex : rawWorkMoMetaData
					.getDependencyWorkIndexes()) {
				requiredManagedObjectIndexes.add(new Integer(
						dependencyWorkIndex));
			}
		}

		// Create the listing of required managed objects
		int[] requiredManagedObjects = new int[requiredManagedObjectIndexes
				.size()];
		int moI = 0;
		for (Integer moIndex : requiredManagedObjectIndexes) {
			requiredManagedObjects[moI++] = moIndex.intValue();
		}

		// Obtain the check managed objects
		ManagedObjectMetaData[] workMoMetaData = workMoRegistry
				.getWorkManagedObjectListing();
		List<Integer> checkMoList = new LinkedList<Integer>();
		for (int moIndex = 0; moIndex < requiredManagedObjects.length; moIndex++) {
			// Add managed object to check if asynchronous
			if (workMoMetaData[moIndex].isManagedObjectAsynchronous()) {
				checkMoList.add(new Integer(moIndex));
			}
		}
		int[] checkManagedObjects = new int[checkMoList.size()];
		for (int i = 0; i < checkManagedObjects.length; i++) {
			checkManagedObjects[i] = checkMoList.get(i).intValue();
		}

		// Obtain the pre task administration
		TaskDutyAssociation<?>[] preTaskAdmin = workAdminRegistry
				.createTaskAdministration(taskConfiguration
						.getPreTaskAdministratorDutyConfiguration());

		// Obtain the post task administration
		TaskDutyAssociation<?>[] postTaskAdmin = workAdminRegistry
				.createTaskAdministration(taskConfiguration
						.getPostTaskAdministratorDutyConfiguration());

		// Register the Task meta-data
		return new RawTaskMetaData(new TaskMetaDataImpl(taskConfiguration
				.getTaskFactory(), team, requiredManagedObjects,
				checkManagedObjects, tmoToWmoTranslation, preTaskAdmin,
				postTaskAdmin));
	}

	/**
	 * {@link net.officefloor.frame.internal.structure.TaskMetaData}.
	 */
	protected final TaskMetaDataImpl<?, ?, ?, ?> taskMetaData;

	/**
	 * Initiate.
	 * 
	 * @param taskMetaData
	 *            {@link net.officefloor.frame.internal.structure.TaskMetaData}.
	 */
	private RawTaskMetaData(TaskMetaDataImpl<?, ?, ?, ?> taskMetaData) {
		this.taskMetaData = taskMetaData;
	}

	/**
	 * Obtains the {@link net.officefloor.frame.internal.structure.TaskMetaData}.
	 * 
	 * @return {@link net.officefloor.frame.internal.structure.TaskMetaData}.
	 */
	public TaskMetaDataImpl<?, ?, ?, ?> getTaskMetaData() {
		return this.taskMetaData;
	}

}
