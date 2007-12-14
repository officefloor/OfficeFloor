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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.TaskMetaDataImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ParentEscalationProcedure;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Registry of {@link net.officefloor.frame.api.execute.Work}.
 * 
 * @author Daniel
 */
public class RawWorkRegistry {

	/**
	 * Creates the registry of {@link WorkMetaData} defining the
	 * {@link net.officefloor.frame.api.execute.Work} items to be carried out by
	 * the Office.
	 * 
	 * @param officeConfiguration
	 *            Configuration of the Office.
	 * @param officeResources
	 *            Resources for the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param rawAdminRegistry
	 *            Registry of the {@link RawAdministratorMetaData}.
	 * @param rawAssetRegistry
	 *            {@link RawAssetManagerRegistry}.
	 * @param defaultParentEscalationProcedure
	 *            Default {@link ParentEscalationProcedure}.
	 * @return Registry of {@link WorkMetaData} defining the
	 *         {@link net.officefloor.frame.api.execute.Work} items to be
	 *         carried out by the Office.
	 * @throws Exception
	 *             If fails.
	 */
	public static RawWorkRegistry createWorkRegistry(
			OfficeConfiguration officeConfiguration,
			RawOfficeResourceRegistry officeResources,
			RawAdministratorRegistry rawAdminRegistry,
			RawAssetManagerRegistry rawAssetRegistry,
			ParentEscalationProcedure defaultParentEscalationProcedure)
			throws Exception {

		// Create the registry of Work meta-data
		Map<String, RawWorkMetaData> workRegistry = new HashMap<String, RawWorkMetaData>();
		for (WorkConfiguration<?> workConfig : officeConfiguration
				.getWorkConfiguration()) {

			// Create the Work meta-data
			workRegistry.put(workConfig.getWorkName(), RawWorkMetaData
					.createRawWorkMetaData(workConfig, officeResources,
							rawAdminRegistry, rawAssetRegistry,
							defaultParentEscalationProcedure));
		}

		// Create the work registry
		RawWorkRegistry rawWorkRegistry = new RawWorkRegistry(workRegistry);

		// Link the Tasks together for the Office
		rawWorkRegistry.loadRemainingTaskState(rawAssetRegistry,
				officeConfiguration);

		// Return the work configuration
		return rawWorkRegistry;
	}

	/**
	 * Registry of the {@link WorkMetaData}.
	 */
	protected final Map<String, RawWorkMetaData> workRegistry;

	/**
	 * Initiate.
	 * 
	 * @param workRegistry
	 *            Registry of the {@link WorkMetaData}.
	 */
	private RawWorkRegistry(Map<String, RawWorkMetaData> workRegistry) {
		this.workRegistry = workRegistry;
	}

	/**
	 * Obtains the {@link RawWorkMetaData} by its name.
	 * 
	 * @param workName
	 *            Name of {@link Work}.
	 * @return {@link RawWorkMetaData}.
	 */
	public RawWorkMetaData getRawWorkMetaData(String workName) {
		return this.workRegistry.get(workName);
	}

	/**
	 * Obtains the registry of {@link WorkMetaData}.
	 * 
	 * @return Registry of {@link WorkMetaData}.
	 */
	public Map<String, WorkMetaData<?>> createWorkMetaDataRegistry() {
		// Create the registry
		Map<String, WorkMetaData<?>> registry = new HashMap<String, WorkMetaData<?>>();
		for (String workName : this.workRegistry.keySet()) {
			registry.put(workName, this.workRegistry.get(workName)
					.getWorkMetaData());
		}

		// Return the registry
		return registry;
	}

	/**
	 * Obtains the {@link TaskMetaData} by the input {@link TaskNodeReference}.
	 * 
	 * @param taskNodeRef
	 *            {@link TaskNodeReference} to identify the {@link TaskMetaData}.
	 * @return {@link TaskMetaData} for the input {@link TaskNodeReference}.
	 * @throws ConfigurationException
	 *             If no {@link TaskMetaData} exists for the input
	 *             {@link TaskNodeReference}.
	 */
	public TaskMetaData<?, ?, ?, ?> getTaskMetaData(
			TaskNodeReference taskNodeRef) throws ConfigurationException {

		// Obtain the raw work meta-data
		String workName = taskNodeRef.getWorkName();
		RawWorkMetaData rawWork = this.workRegistry.get(workName);
		if (rawWork == null) {
			throw new ConfigurationException("Unknown work '" + workName + "'");
		}

		// Obtain the task meta-data of the work in question
		String taskName = taskNodeRef.getTaskName();
		RawTaskMetaData rawTask = rawWork.getTaskRegistry().getRawTaskMetaData(
				taskName);
		if (rawTask == null) {
			throw new ConfigurationException("Unknown task '" + taskName
					+ "' on work '" + workName + "'");
		}

		// Return the located Task meta-data
		return rawTask.getTaskMetaData();
	}

	/**
	 * Loads the remaining state for the
	 * {@link net.officefloor.frame.api.execute.Task} instances of the Office.
	 * 
	 * @param rawAssetRegistry
	 *            {@link RawAssetManagerRegistry}.
	 * @param officeConfiguration
	 *            {@link OfficeConfiguration} containing the configurations for
	 *            the {@link TaskMetaData} instances.
	 * @throws ConfigurationException
	 *             If failure in configuration.
	 */
	@SuppressWarnings("unchecked")
	private <W extends Work> void loadRemainingTaskState(
			RawAssetManagerRegistry rawAssetRegistry,
			OfficeConfiguration officeConfiguration)
			throws ConfigurationException {

		// TODO remove (display structure)
		System.out.println("[" + this.getClass().getName() + " (todo remove):");
		System.out.println("Work Structure for office "
				+ officeConfiguration.getOfficeName());
		for (String workName : this.workRegistry.keySet()) {
			System.out.print("    " + workName + " [");
			for (String taskName : this.workRegistry.get(workName)
					.getTaskRegistry().getTaskNames()) {
				System.out.print(" " + taskName);
			}
			System.out.println(" ]");
		}
		System.out.println(":" + this.getClass().getName() + "]");

		// Iterate over the work
		for (WorkConfiguration workConfig : officeConfiguration
				.getWorkConfiguration()) {

			// Obtain the Work name
			String workName = workConfig.getWorkName();

			// Obtain the Work meta-data
			RawWorkMetaData rawWorkMetaData = workRegistry.get(workName);
			if (rawWorkMetaData == null) {
				throw new ConfigurationException("Unknown work '" + workName
						+ "'");
			}
			WorkMetaData<W> workMetaData = (WorkMetaData<W>) rawWorkMetaData
					.getWorkMetaData();

			// Iterate over the tasks of the work
			for (TaskConfiguration taskConfig : workConfig
					.getTaskConfiguration()) {

				// Obtain the Task name
				String taskName = taskConfig.getTaskName();

				// Obtain the Task meta-data to load
				TaskMetaDataImpl<?, W, ?, ?> taskMetaData = (TaskMetaDataImpl<?, W, ?, ?>) this
						.locateTaskMetaData(workName, taskName, workName,
								taskName);

				// Obtain the next task in flow
				TaskMetaData<?, ?, ?, ?> nextTaskInFlow = null;
				TaskNodeReference taskNodeRef = taskConfig.getNextTaskInFlow();
				if (taskNodeRef != null) {
					nextTaskInFlow = this.locateTaskMetaData(taskNodeRef,
							workName, taskName);
				}

				// Obtain the flows (including initial task meta-data) to link
				FlowConfiguration[] flowConfigs = taskConfig
						.getFlowConfiguration();
				FlowMetaData[] flowMetaData = new FlowMetaData[flowConfigs.length];
				for (int i = 0; i < flowMetaData.length; i++) {

					// Obtain the instigation strategy
					FlowInstigationStrategyEnum strategy = flowConfigs[i]
							.getInstigationStrategy();

					// Create Flow Manager if asynchronous
					AssetManager flowManager = null;
					if (strategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
						// Asynchronous instigation requires management
						flowManager = rawAssetRegistry
								.createAssetManager("Flow " + workName + "."
										+ taskName + "[" + i + "]");
					}

					// Create the Flow meta-data
					flowMetaData[i] = new FlowMetaDataImpl(strategy,
							locateTaskMetaData(flowConfigs[i].getInitialTask(),
									workName, taskName), flowManager);
				}

				// Load remaining state to task
				taskMetaData.loadRemainingState(workMetaData, flowMetaData,
						nextTaskInFlow);
			}
		}
	}

	/**
	 * Locates the {@link TaskMetaData} within the Office.
	 * 
	 * @param taskNodeRef
	 *            {@link TaskNodeReference} to find the {@link TaskMetaData}.
	 * @param workNameInContext
	 *            Work requesting the {@link TaskMetaData}.
	 * @param taskNameInContext
	 *            Task requesting the {@link TaskMetaData}.
	 * @return Specific {@link TaskMetaData}.
	 * @throws ConfigurationException
	 *             If fails to find the {@link TaskMetaData}.
	 */
	private TaskMetaDataImpl<?, ?, ?, ?> locateTaskMetaData(
			TaskNodeReference taskNodeRef, String workNameInContext,
			String taskNameInContext) throws ConfigurationException {

		// Obtain the work name
		String workName = taskNodeRef.getWorkName();
		if (workName == null) {
			// Default to work in context
			workName = workNameInContext;
		}

		// Return the located task
		return locateTaskMetaData(workName, taskNodeRef.getTaskName(),
				workNameInContext, taskNameInContext);
	}

	/**
	 * Locates the {@link TaskMetaData} within the Office.
	 * 
	 * @param workNameToLocate
	 *            {@link Work} containing the
	 *            {@link net.officefloor.frame.api.execute.Task} to find the
	 *            {@link TaskMetaData}.
	 * @param taskNameToLocate
	 *            {@link net.officefloor.frame.api.execute.Task} to find the
	 *            {@link TaskMetaData}.
	 * @param workNameInContext
	 *            Work requesting the {@link TaskMetaData}.
	 * @param taskNameInContext
	 *            Task requesting the {@link TaskMetaData}.
	 * @return Specific {@link TaskMetaData}.
	 * @throws ConfigurationException
	 *             If fails to find the {@link TaskMetaData}.
	 */
	private TaskMetaDataImpl<?, ?, ?, ?> locateTaskMetaData(
			String workNameToLocate, String taskNameToLocate,
			String workNameInContext, String taskNameInContext)
			throws ConfigurationException {

		// Obtain the raw work meta-data
		RawWorkMetaData rawWork = this.workRegistry.get(workNameToLocate);
		if (rawWork == null) {
			throw new ConfigurationException("Unknown work '"
					+ workNameToLocate + "' to link into work '"
					+ workNameInContext + "' task '" + taskNameInContext + "'");
		}

		// Obtain the task meta-data of the work in question
		RawTaskMetaData rawTask = rawWork.getTaskRegistry().getRawTaskMetaData(
				taskNameToLocate);
		if (rawTask == null) {
			throw new ConfigurationException("Unknown task '"
					+ taskNameToLocate + "' on work '" + workNameToLocate
					+ "' to link into work '" + workNameInContext + "' task '"
					+ taskNameInContext + "'");
		}

		// Return the located Task meta-data
		return rawTask.getTaskMetaData();
	}

}
