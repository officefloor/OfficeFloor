/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.task.TaskNodeReferenceImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.execute.officefloor.ManagedObjectExecuteContextImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;

/**
 * {@link RawManagingOfficeMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagingOfficeMetaDataImpl<F extends Enum<F>> implements
		RawManagingOfficeMetaData<F> {

	/**
	 * Determines if the {@link ManagedObjectSource} instigates {@link Flow}
	 * instances.
	 * 
	 * @param flowMetaData
	 *            {@link ManagedObjectFlowMetaData} instances of the
	 *            {@link ManagedObjectSource}.
	 * @return <code>true</code> if {@link ManagedObjectSource} instigates
	 *         {@link Flow} instances.
	 */
	public static boolean isRequireFlows(
			ManagedObjectFlowMetaData<?>[] flowMetaData) {
		return ((flowMetaData != null) && (flowMetaData.length > 0));
	}

	/**
	 * Name of the managing {@link Office}.
	 */
	private final String managingOfficeName;

	/**
	 * {@link ProcessState} bound name for the {@link ManagedObject} within the
	 * {@link Office}.
	 */
	private final String processBoundName;

	/**
	 * Name of the {@link Work} to recycle the {@link ManagedObject}.
	 */
	private final String recycleWorkName;

	/**
	 * {@link ManagedObjectFlowMetaData} instances for the
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectFlowMetaData<F>[] flowMetaDatas;

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeConfiguration<F> managingOfficeConfiguration;

	/**
	 * {@link RawManagedObjectMetaDataImpl}.
	 */
	private RawManagedObjectMetaData<?, F> rawManagedObjectMetaData;

	/**
	 * Listing of {@link ManagedObjectMetaData} created before this is managed
	 * by the {@link Office}.
	 */
	private List<ManagedObjectMetaDataImpl<?>> managedObjectMetaDatas = new LinkedList<ManagedObjectMetaDataImpl<?>>();

	/**
	 * {@link OfficeMetaData} of the managing {@link Office}.
	 */
	private OfficeMetaData managingOffice = null;

	/**
	 * {@link FlowMetaData} of the recycle {@link Flow}.
	 */
	private FlowMetaData<?> recycleFlowMetaData = null;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<F> managedObjectExecuteContext = null;

	/**
	 * Initialise.
	 * 
	 * @param managingOfficeName
	 *            Name of the managing {@link Office}.
	 * @param processBoundName
	 *            {@link ProcessState} bound name for the {@link ManagedObject}
	 *            within the {@link Office}.
	 * @param recycleWorkName
	 *            Name of the {@link Work} to recycle the {@link ManagedObject}.
	 * @param flowMetaDatas
	 *            {@link ManagedObjectFlowMetaData} instances for the
	 *            {@link ManagedObjectSource}.
	 * @param managingOfficeConfiguration
	 *            {@link ManagingOfficeConfiguration}.
	 */
	public RawManagingOfficeMetaDataImpl(String managingOfficeName,
			String processBoundName, String recycleWorkName,
			ManagedObjectFlowMetaData<F>[] flowMetaDatas,
			ManagingOfficeConfiguration<F> managingOfficeConfiguration) {
		this.managingOfficeName = managingOfficeName;
		this.processBoundName = processBoundName;
		this.recycleWorkName = recycleWorkName;
		this.flowMetaDatas = flowMetaDatas;
		this.managingOfficeConfiguration = managingOfficeConfiguration;
	}

	/**
	 * Specifies the {@link RawManagedObjectMetaData}.
	 * 
	 * @param rawManagedObjectMetaData
	 *            {@link RawManagedObjectMetaData}.
	 */
	public synchronized void setRawManagedObjectMetaData(
			RawManagedObjectMetaData<?, F> rawManagedObjectMetaData) {
		this.rawManagedObjectMetaData = rawManagedObjectMetaData;
	}

	/**
	 * Adds a {@link ManagedObjectMetaData} to be managed by the managing
	 * {@link Office}.
	 * 
	 * @param moMetaData
	 *            {@link ManagedObjectMetaData} to be managed by the managing
	 *            {@link Office}.
	 */
	public synchronized void manageManagedObject(
			ManagedObjectMetaDataImpl<?> moMetaData) {

		// Determine if being managed by an office
		if (this.managedObjectMetaDatas != null) {
			// Not yet managed by an office
			this.managedObjectMetaDatas.add(moMetaData);

		} else {
			// Already being managed, so load remaining state
			moMetaData.loadRemainingState(this.managingOffice,
					this.recycleFlowMetaData);
		}
	}

	/*
	 * ===================== RawManagingOfficeMetaData ========================
	 */

	@Override
	public String getManagingOfficeName() {
		return this.managingOfficeName;
	}

	@Override
	public String getProcessBoundName() {
		return this.processBoundName;
	}

	@Override
	public synchronized RawManagedObjectMetaData<?, F> getRawManagedObjectMetaData() {
		return this.rawManagedObjectMetaData;
	}

	@Override
	public boolean isRequireFlows() {
		return isRequireFlows(this.flowMetaDatas);
	}

	@Override
	public synchronized void manageByOffice(
			OfficeMetaDataLocator metaDataLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Obtain the name of the managed object source
		String managedObjectSourceName = this.rawManagedObjectMetaData
				.getManagedObjectName();

		// -----------------------------------------------------------
		// Load Remaining State to the Managed Object Meta-Data
		// -----------------------------------------------------------

		// Obtain the office meta-data
		OfficeMetaData officeMetaData = metaDataLocator.getOfficeMetaData();

		// Obtain the recycle task meta-data
		FlowMetaData<?> recycleFlowMetaData = null;
		if (this.recycleWorkName != null) {

			// Locate the work meta-data
			WorkMetaData<?> workMetaData = metaDataLocator
					.getWorkMetaData(this.recycleWorkName);
			if (workMetaData == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						managedObjectSourceName, "Recycle work '"
								+ this.recycleWorkName + "' not found");
				return; // must obtain recycle work
			}

			// Obtain the initial flow of work as recycle flow
			recycleFlowMetaData = workMetaData.getInitialFlowMetaData();
			if (recycleFlowMetaData == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						managedObjectSourceName, "No initial flow on work "
								+ this.recycleWorkName + " for recycle task");
				return; // must obtain recycle task
			}

			// Obtain the parameter type for the recycle task
			TaskMetaData<?, ?, ?> recycleTaskMetaData = recycleFlowMetaData
					.getInitialTaskMetaData();
			Class<?> parameterType = recycleTaskMetaData.getParameterType();
			if (parameterType != null) {
				if (!parameterType
						.isAssignableFrom(RecycleManagedObjectParameter.class)) {
					issues.addIssue(AssetType.MANAGED_OBJECT,
							managedObjectSourceName,
							"Incompatible parameter type for recycle task (parameter="
									+ parameterType.getName()
									+ ", required type="
									+ RecycleManagedObjectParameter.class
											.getName() + ", work="
									+ this.recycleWorkName + ", task="
									+ recycleTaskMetaData.getTaskName() + ")");
					return; // can not be used as recycle task
				}
			}
		}

		// Load remaining state to existing managed object meta-data
		for (ManagedObjectMetaDataImpl<?> moMetaData : this.managedObjectMetaDatas) {
			moMetaData.loadRemainingState(officeMetaData, recycleFlowMetaData);
		}

		// Setup for further managed object meta-data to be managed
		this.managingOffice = officeMetaData;
		this.recycleFlowMetaData = recycleFlowMetaData;
		this.managedObjectMetaDatas = null;

		// -----------------------------------------------------------
		// Create the Managed Object Execute Context
		// -----------------------------------------------------------

		// Obtain the flow configuration
		ManagedObjectFlowConfiguration<F>[] flowConfigurations = this.managingOfficeConfiguration
				.getFlowConfiguration();

		// Determine if flows for the managed object source
		if (!this.isRequireFlows()) {

			// No flows but issue if flows configured
			if ((flowConfigurations != null) && (flowConfigurations.length > 0)) {
				issues
						.addIssue(
								AssetType.MANAGED_OBJECT,
								managedObjectSourceName,
								ManagedObjectSourceMetaData.class
										.getSimpleName()
										+ " specifies no flows but flows configured for it");
				return; // configuration does not align to meta-data
			}

			// No flows, so provide empty execution context
			this.managedObjectExecuteContext = new ManagedObjectExecuteContextImpl<F>(
					-1, null, officeMetaData);
			return;
		}

		// Obtain map of process managed object name to process bound index
		ManagedObjectMetaData<?>[] moMetaDatas = officeMetaData
				.getProcessMetaData().getManagedObjectMetaData();
		Map<String, Integer> processMoNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < moMetaDatas.length; i++) {
			processMoNameToIndex.put(
					moMetaDatas[i].getBoundManagedObjectName(), new Integer(i));
		}

		// Obtain the index of the managed object in the office
		Integer processBoundIndex = processMoNameToIndex
				.get(this.processBoundName);
		if (processBoundIndex == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					ManagedObjectSource.class.getSimpleName()
							+ " by process bound name '"
							+ this.processBoundName
							+ "' not managed by Office "
							+ officeMetaData.getOfficeName());

			// No flows
			return; // managed object not in office
		}

		// Create the flow mappings for the configuration
		Map<Integer, ManagedObjectFlowConfiguration<F>> flowMappings = new HashMap<Integer, ManagedObjectFlowConfiguration<F>>();
		for (int i = 0; i < flowConfigurations.length; i++) {
			ManagedObjectFlowConfiguration<F> flowConfiguration = flowConfigurations[i];

			// Obtain the index to identify the flow
			F flowKey = flowConfiguration.getFlowKey();
			int index = (flowKey != null ? flowKey.ordinal() : i);

			// Load the flow at its index
			flowMappings.put(new Integer(index), flowConfiguration);
		}

		// Create the flows
		FlowMetaData<?>[] flows = new FlowMetaData[this.flowMetaDatas.length];
		for (int i = 0; i < flows.length; i++) {
			ManagedObjectFlowMetaData<F> flowMetaData = this.flowMetaDatas[i];

			// Obtain the index for the flow
			F flowKey = flowMetaData.getKey();
			int index = (flowKey != null ? flowKey.ordinal() : i);

			// Create name to identify flow
			String label = flowMetaData.getLabel();
			String flowLabel = "flow " + index + " (key="
					+ (flowKey != null ? flowKey.toString() : "<indexed>")
					+ ", label="
					+ (!ConstructUtil.isBlank(label) ? label : "<no label>")
					+ ")";

			// Obtain the flow configuration
			ManagedObjectFlowConfiguration<F> flowConfiguration = flowMappings
					.get(new Integer(index));
			if (flowConfiguration == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						managedObjectSourceName, "No flow configured for "
								+ flowLabel);
				return; // flow not configured
			}

			// Remove flow for later check no extra configured
			flowMappings.remove(new Integer(index));

			// Obtain the argument type passed to the task
			Class<?> argumentType = flowMetaData.getArgumentType();

			// Create the task node reference for the task.
			// Override argument type as managed object knows better.
			TaskNodeReference configurationTaskReference = flowConfiguration
					.getTaskNodeReference();
			TaskNodeReference flowTaskReference = new TaskNodeReferenceImpl(
					configurationTaskReference.getWorkName(),
					configurationTaskReference.getTaskName(), argumentType);

			// Obtain the task meta-data of flow meta-data
			TaskMetaData<?, ?, ?> taskMetaData = ConstructUtil.getTaskMetaData(
					flowTaskReference, metaDataLocator, issues,
					AssetType.MANAGED_OBJECT, managedObjectSourceName,
					flowLabel, true);
			if (taskMetaData == null) {
				return; // can not find task of flow
			}

			// Create and specify the flow meta-data
			flows[i] = ConstructUtil.newFlowMetaData(
					FlowInstigationStrategyEnum.ASYNCHRONOUS, taskMetaData,
					assetManagerFactory, AssetType.MANAGED_OBJECT,
					managedObjectSourceName, flowLabel, issues);
		}

		// Ensure no extra flow configurations
		if (flowMappings.size() > 0) {
			issues
					.addIssue(AssetType.MANAGED_OBJECT,
							managedObjectSourceName,
							"Extra flows configured than specified by "
									+ ManagedObjectSourceMetaData.class
											.getSimpleName());
			return; // should only have configurations for meta-data required
		}

		// Specify the managed object execute context
		this.managedObjectExecuteContext = new ManagedObjectExecuteContextImpl<F>(
				processBoundIndex.intValue(), flows, officeMetaData);
	}

	@Override
	public synchronized ManagedObjectExecuteContext<F> getManagedObjectExecuteContext() {
		return this.managedObjectExecuteContext;
	}

}