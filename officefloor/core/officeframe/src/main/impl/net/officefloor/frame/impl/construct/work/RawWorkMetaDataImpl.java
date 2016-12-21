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
package net.officefloor.frame.impl.construct.work;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaDataFactory;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.source.SourceContext;

/**
 * {@link RawWorkMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawWorkMetaDataImpl<W extends Work> implements RawWorkMetaDataFactory, RawWorkMetaData<W> {

	/**
	 * Obtains the {@link RawWorkMetaDataFactory}.
	 * 
	 * @return {@link RawWorkMetaDataFactory}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static RawWorkMetaDataFactory getFactory() {
		return new RawWorkMetaDataImpl(null, null, null, null, null, null);
	}

	/**
	 * Name of the {@link Work}.
	 */
	private final String workName;

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaData rawOfficeMetaData;

	/**
	 * {@link RawBoundManagedObjectMetaData} of the {@link ManagedObject}
	 * instances bound to this {@link Work}.
	 */
	private final RawBoundManagedObjectMetaData[] workManagedObjects;

	/**
	 * {@link RawBoundManagedObjectMetaData} instances by the
	 * {@link ManagedObjectScope} names.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects;

	/**
	 * {@link RawBoundManagedObjectMetaData} of the {@link Administrator}
	 * instances bound to this {@link Work}.
	 */
	private final RawBoundAdministratorMetaData<?, ?>[] workAdministrators;

	/**
	 * {@link RawBoundAdministratorMetaData} instances by the
	 * {@link AdministratorScope} names.
	 */
	private final Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdministrators;

	/**
	 * {@link RawTaskMetaData} instances of this {@link Work}.
	 */
	private List<RawTaskMetaData<W, ?, ?>> rawTaskMetaDatas = null;

	/**
	 * {@link WorkMetaData}.
	 */
	private WorkMetaData<W> workMetaData = null;

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param rawOfficeMetaData
	 *            {@link RawOfficeMetaData}.
	 * @param workManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} of the
	 *            {@link ManagedObject} instances bound to this {@link Work}.
	 * @param scopeManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} instances by the
	 *            {@link ManagedObjectScope} names.
	 * @param workAdministrators
	 *            {@link RawBoundManagedObjectMetaData} of the
	 *            {@link Administrator} instances bound to this {@link Work}.
	 * @param scopeAdministrators
	 *            {@link RawBoundAdministratorMetaData} instances by the
	 *            {@link AdministratorScope} names.
	 */
	private RawWorkMetaDataImpl(String workName, RawOfficeMetaData rawOfficeMetaData,
			RawBoundManagedObjectMetaData[] workManagedObjects,
			Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects,
			RawBoundAdministratorMetaData<?, ?>[] workAdministrators,
			Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdministrators) {
		this.workName = workName;
		this.rawOfficeMetaData = rawOfficeMetaData;
		this.workManagedObjects = workManagedObjects;
		this.scopeManagedObjects = scopeManagedObjects;
		this.workAdministrators = workAdministrators;
		this.scopeAdministrators = scopeAdministrators;
	}

	/*
	 * ================ RawWorkMetaDataFactory =========================
	 */

	@Override
	public <w extends Work> RawWorkMetaData<w> constructRawWorkMetaData(WorkConfiguration<w> configuration,
			SourceContext sourceContext, OfficeFloorIssues issues, RawOfficeMetaData rawOfficeMetaData,
			AssetManagerFactory assetManagerFactory, RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory, RawTaskMetaDataFactory rawTaskFactory,
			FunctionLoop functionLoop) {

		// Obtain the work name
		String workName = configuration.getWorkName();
		if (ConstructUtil.isBlank(workName)) {
			issues.addIssue(AssetType.OFFICE, rawOfficeMetaData.getOfficeName(), "Work added to office without name");
			return null; // no work name
		}

		// Obtain the work factory
		WorkFactory<w> workFactory = configuration.getWorkFactory();
		if (workFactory == null) {
			issues.addIssue(AssetType.WORK, workName, WorkFactory.class.getSimpleName() + " not provided");
			return null; // no work factory
		}

		// Obtain the office scoped managed objects
		Map<String, RawBoundManagedObjectMetaData> officeScopeMo = rawOfficeMetaData.getOfficeScopeManagedObjects();

		// Obtain the work bound managed objects
		ManagedObjectConfiguration<?>[] moConfiguration = configuration.getManagedObjectConfiguration();
		RawBoundManagedObjectMetaData[] workBoundMo;
		if ((moConfiguration == null) || (moConfiguration.length == 0)) {
			workBoundMo = new RawBoundManagedObjectMetaData[0];
		} else {
			workBoundMo = rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(moConfiguration, issues,
					ManagedObjectScope.WORK, AssetType.WORK, workName, assetManagerFactory,
					rawOfficeMetaData.getManagedObjectMetaData(), officeScopeMo, null, null,
					rawOfficeMetaData.getGovernanceMetaData());
		}

		// Create the work scope managed objects available to tasks
		Map<String, RawBoundManagedObjectMetaData> workScopeMo = new HashMap<String, RawBoundManagedObjectMetaData>();
		workScopeMo.putAll(officeScopeMo); // include all office scoped
		for (RawBoundManagedObjectMetaData mo : workBoundMo) {
			workScopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Obtain the office scope administrators
		Map<String, RawBoundAdministratorMetaData<?, ?>> officeScopeAdmin = rawOfficeMetaData
				.getOfficeScopeAdministrators();

		// Obtain the work bound administrators
		AdministratorSourceConfiguration<?, ?>[] adminConfiguration = configuration.getAdministratorConfiguration();
		RawBoundAdministratorMetaData<?, ?>[] workBoundAdmins;
		if ((adminConfiguration == null) || (adminConfiguration.length == 0)) {
			workBoundAdmins = new RawBoundAdministratorMetaData[0];
		} else {
			workBoundAdmins = rawBoundAdministratorFactory.constructRawBoundAdministratorMetaData(adminConfiguration,
					sourceContext, issues, AdministratorScope.WORK, AssetType.WORK, workName,
					rawOfficeMetaData.getTeams(), workScopeMo, functionLoop);
		}

		// Create the work scope administrators available to tasks
		Map<String, RawBoundAdministratorMetaData<?, ?>> workScopeAdmin = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();
		workScopeAdmin.putAll(officeScopeAdmin); // include all office scoped
		for (RawBoundAdministratorMetaData<?, ?> admin : workBoundAdmins) {
			workScopeAdmin.put(admin.getBoundAdministratorName(), admin);
		}

		// Create the raw work meta-data
		RawWorkMetaDataImpl<w> rawWorkMetaData = new RawWorkMetaDataImpl<w>(workName, rawOfficeMetaData, workBoundMo,
				workScopeMo, workBoundAdmins, workScopeAdmin);

		// Obtain the name of the task for the initial flow of work
		String initialTaskName = configuration.getInitialTaskName();
		TaskMetaData<w, ?, ?> initialTaskMetaData = null;

		// Construct the task meta-data of this work (also find initial task)
		rawWorkMetaData.rawTaskMetaDatas = new LinkedList<RawTaskMetaData<w, ?, ?>>();
		List<TaskMetaData<w, ?, ?>> taskMetaDatas = new LinkedList<TaskMetaData<w, ?, ?>>();
		for (TaskConfiguration<w, ?, ?> taskConfiguration : configuration.getTaskConfiguration()) {

			// Construct and register the raw task meta-data
			RawTaskMetaData<w, ?, ?> rawTaskMetaData = rawTaskFactory.constructRawTaskMetaData(taskConfiguration,
					issues, rawWorkMetaData, functionLoop);
			if (rawTaskMetaData == null) {
				continue; // failed to construct the task
			}
			rawWorkMetaData.rawTaskMetaDatas.add(rawTaskMetaData);

			// Construct and register the task meta-data
			TaskMetaData<w, ?, ?> taskMetaData = rawTaskMetaData.getTaskMetaData();
			taskMetaDatas.add(taskMetaData);

			// Determine if the initial task for the work
			if (initialTaskName != null) {
				if (initialTaskName.equals(rawTaskMetaData.getTaskName())) {
					initialTaskMetaData = taskMetaData;
				}
			}
		}

		// Create the initial flow meta-data for the work
		FlowMetaData<w> initialFlowMetaData = null;
		if (initialTaskName != null) {

			// Ensure have the initial task meta-data
			if (initialTaskMetaData == null) {
				issues.addIssue(AssetType.WORK, workName, "No initial task by name '" + initialTaskName + "' on work");
				return null; // must have initial task meta-data
			}

			// Construct the initial flow meta-data
			initialFlowMetaData = ConstructUtil.newFlowMetaData(FlowInstigationStrategyEnum.ASYNCHRONOUS,
					initialTaskMetaData, assetManagerFactory, AssetType.WORK, workName, "InitialFlow", issues);
		}

		// Create the listing of default work bound managed object meta-data
		ManagedObjectMetaData<?>[] managedObjectMetaData = new ManagedObjectMetaData[rawWorkMetaData.workManagedObjects.length];
		for (int i = 0; i < managedObjectMetaData.length; i++) {
			RawBoundManagedObjectMetaData moMetaData = rawWorkMetaData.workManagedObjects[i];

			// Obtain the default managed object instance meta-data
			int defaultInstanceIndex = moMetaData.getDefaultInstanceIndex();
			RawBoundManagedObjectInstanceMetaData<?> moInstanceMetaData = moMetaData
					.getRawBoundManagedObjectInstanceMetaData()[defaultInstanceIndex];

			// Obtain the default managed object meta-data
			managedObjectMetaData[i] = moInstanceMetaData.getManagedObjectMetaData();
			if (managedObjectMetaData[i] == null) {
				issues.addIssue(AssetType.WORK, workName, "No managed object meta-data for work managed object "
						+ rawWorkMetaData.workManagedObjects[i].getBoundManagedObjectName());
			}
		}

		// Create the listing of work bound administrator meta-data
		AdministratorMetaData<?, ?>[] administratorMetaData = new AdministratorMetaData[rawWorkMetaData.workAdministrators.length];
		for (int i = 0; i < administratorMetaData.length; i++) {
			administratorMetaData[i] = rawWorkMetaData.workAdministrators[i].getAdministratorMetaData();
		}

		// Create the work meta-data
		rawWorkMetaData.workMetaData = new WorkMetaDataImpl<w>(rawWorkMetaData.workName, workFactory,
				managedObjectMetaData, administratorMetaData, initialFlowMetaData,
				ConstructUtil.toArray(taskMetaDatas, new TaskMetaData[0]));

		// Return the raw work meta-data
		return rawWorkMetaData;
	}

	/*
	 * ===================== RawWorkMetaData ===================================
	 */

	@Override
	public String getWorkName() {
		return this.workName;
	}

	@Override
	public RawOfficeMetaData getRawOfficeMetaData() {
		return this.rawOfficeMetaData;
	}

	@Override
	public RawBoundManagedObjectMetaData getScopeManagedObjectMetaData(String scopeManagedObjectName) {
		return this.scopeManagedObjects.get(scopeManagedObjectName);
	}

	@Override
	public RawBoundAdministratorMetaData<?, ?> getScopeAdministratorMetaData(String scopeAdministratorName) {
		return this.scopeAdministrators.get(scopeAdministratorName);
	}

	@Override
	public WorkMetaData<W> getWorkMetaData() {
		return this.workMetaData;
	}

	@Override
	public void linkOfficeMetaData(OfficeMetaData officeMetaData, OfficeMetaDataLocator taskMetaDataLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Link tasks of work bound administrators
		for (RawBoundAdministratorMetaData<?, ?> rawBoundAdminMetaData : this.workAdministrators) {
			rawBoundAdminMetaData.linkOfficeMetaData(officeMetaData, taskMetaDataLocator, assetManagerFactory, issues);
		}

		// Link the tasks of this work
		for (RawTaskMetaData<W, ?, ?> rawTaskMetaData : this.rawTaskMetaDatas) {
			rawTaskMetaData.linkTasks(taskMetaDataLocator, this.workMetaData, assetManagerFactory, issues);
		}
	}

}