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
package net.officefloor.frame.impl.construct.work;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedWorkAdministratorConfiguration;
import net.officefloor.frame.internal.configuration.LinkedWorkManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawWorkManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaDataFactory;
import net.officefloor.frame.internal.construct.TaskMetaDataLocator;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link RawWorkMetaData} implementation.
 * 
 * @author Daniel
 */
public class RawWorkMetaDataImpl<W extends Work> implements
		RawWorkMetaDataFactory, RawWorkMetaData<W> {

	/**
	 * Obtains the {@link RawWorkMetaDataFactory}.
	 * 
	 * @return {@link RawWorkMetaDataFactory}.
	 */
	@SuppressWarnings("unchecked")
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
	private final RawBoundManagedObjectMetaData<?>[] workManagedObjects;

	/**
	 * {@link RawBoundManagedObjectMetaData} instances by the
	 * {@link ManagedObject} scope names used by the {@link Task} instances of
	 * this {@link Work}.
	 */
	private final Map<String, RawBoundManagedObjectMetaData<?>> taskScopeManagedObjects;

	/**
	 * {@link RawBoundManagedObjectMetaData} of the {@link Administrator}
	 * instances bound to this {@link Work}.
	 */
	private final RawBoundAdministratorMetaData<?, ?>[] workAdministrators;

	/**
	 * {@link RawBoundAdministratorMetaData} instances by the
	 * {@link Administrator} scope names used by the {@link Task} instances of
	 * this {@link Work}.
	 */
	private final Map<String, RawBoundAdministratorMetaData<?, ?>> taskScopeAdministrators;

	/**
	 * {@link RawWorkManagedObjectMetaData} in the order the {@link Task}
	 * instances expect to obtain {@link ManagedObject} instances from this
	 * {@link Work}.
	 */
	private final List<RawWorkManagedObjectMetaDataImpl> workRequiredManagedObjects = new LinkedList<RawWorkManagedObjectMetaDataImpl>();

	/**
	 * Index of the next {@link RawWorkManagedObjectMetaData}.
	 */
	private int nextWorkRequiredManagedObjectIndex = 0;

	/**
	 * {@link RawWorkAdministratorMetaData} in the order the {@link Task}
	 * instances expect to obtain {@link Administrator} instances from this
	 * {@link Work}.
	 */
	private final List<RawWorkAdministratorMetaDataImpl> workRequiredAdministrators = new LinkedList<RawWorkAdministratorMetaDataImpl>();

	/**
	 * Index of the next {@link RawWorkAdministratorMetaData}.
	 */
	private int nextWorkRequiredAdministratorIndex = 0;

	/**
	 * {@link RawTaskMetaData} instances of this {@link Work}.
	 */
	private List<RawTaskMetaData<?, W, ?, ?>> rawTaskMetaDatas = null;

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
	 * @param taskScopeManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} instances by the
	 *            {@link ManagedObject} scope names used by the {@link Task}
	 *            instances of this {@link Work}.
	 * @param workAdministrators
	 *            {@link RawBoundManagedObjectMetaData} of the
	 *            {@link Administrator} instances bound to this {@link Work}.
	 * @param taskScopeAdministrators
	 *            {@link RawBoundAdministratorMetaData} instances by the
	 *            {@link Administrator} scope names used by the {@link Task}
	 *            instances of this {@link Work}.
	 */
	public RawWorkMetaDataImpl(
			String workName,
			RawOfficeMetaData rawOfficeMetaData,
			RawBoundManagedObjectMetaData<?>[] workManagedObjects,
			Map<String, RawBoundManagedObjectMetaData<?>> taskScopeManagedObjects,
			RawBoundAdministratorMetaData<?, ?>[] workAdministrators,
			Map<String, RawBoundAdministratorMetaData<?, ?>> taskScopeAdministrators) {
		this.workName = workName;
		this.rawOfficeMetaData = rawOfficeMetaData;
		this.workManagedObjects = workManagedObjects;
		this.taskScopeManagedObjects = taskScopeManagedObjects;
		this.workAdministrators = workAdministrators;
		this.taskScopeAdministrators = taskScopeAdministrators;
	}

	/*
	 * ================ RawWorkMetaDataFactory =========================
	 */

	@Override
	public <w extends Work> RawWorkMetaData<w> constructRawWorkMetaData(
			WorkConfiguration<w> configuration, OfficeFloorIssues issues,
			RawOfficeMetaData rawOfficeMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory,
			RawTaskMetaDataFactory rawTaskFactory) {

		// Obtain the work name
		String workName = configuration.getWorkName();
		if (ConstructUtil.isBlank(workName)) {
			issues.addIssue(AssetType.OFFICE,
					rawOfficeMetaData.getOfficeName(),
					"Work added to office without name");
			return null; // no work name
		}

		// Obtain the work factory
		WorkFactory<w> workFactory = configuration.getWorkFactory();
		if (workFactory == null) {
			issues.addIssue(AssetType.WORK, workName, WorkFactory.class
					.getSimpleName()
					+ " not provided");
			return null; // no work factory
		}

		// Obtain the office scoped managed objects
		Map<String, RawBoundManagedObjectMetaData<?>> officeScopeMo = rawOfficeMetaData
				.getOfficeScopeManagedObjects();

		// Obtain the work bound managed objects
		ManagedObjectConfiguration<?>[] moConfiguration = configuration
				.getManagedObjectConfiguration();
		RawBoundManagedObjectMetaData<?>[] workBoundMo;
		if ((moConfiguration == null) || (moConfiguration.length == 0)) {
			workBoundMo = new RawBoundManagedObjectMetaData[0];
		} else {
			workBoundMo = rawBoundManagedObjectFactory
					.constructBoundManagedObjectMetaData(moConfiguration,
							issues, ManagedObjectScope.WORK, AssetType.WORK,
							workName, rawOfficeMetaData
									.getManagedObjectMetaData(), officeScopeMo);
		}
		Map<String, RawBoundManagedObjectMetaData<?>> workMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		for (RawBoundManagedObjectMetaData<?> mo : workBoundMo) {
			workMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Create the work scope managed objects available to tasks
		Map<String, RawBoundManagedObjectMetaData<?>> taskScopeMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		for (LinkedWorkManagedObjectConfiguration linkMoConfiguration : configuration
				.getLinkedManagedObjectConfiguration()) {

			// Obtain the work managed object name
			String workManagedObjectName = linkMoConfiguration
					.getWorkManagedObjectName();
			// TODO handle no name

			// Obtain the bound managed object name
			String boundManagedObjectName = linkMoConfiguration
					.getBoundManagedObjectName();
			// TODO handle no name

			// Obtain the bound managed object from office scope
			RawBoundManagedObjectMetaData<?> mo = officeScopeMo
					.get(boundManagedObjectName);
			// TODO handle no managed object

			// Bound to work under work managed object name
			taskScopeMo.put(workManagedObjectName, mo);
		}
		taskScopeMo.putAll(workMo); // second as may override names

		// Obtain the office scope administrators
		Map<String, RawBoundAdministratorMetaData<?, ?>> officeScopeAdmin = rawOfficeMetaData
				.getOfficeScopeAdministrators();

		// Create the scoped managed objects available to the administrators
		Map<String, RawBoundManagedObjectMetaData<?>> adminScopeMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		adminScopeMo.putAll(officeScopeMo);
		adminScopeMo.putAll(workMo); // second as may override names

		// Obtain the work bound administrators
		AdministratorSourceConfiguration<?, ?>[] adminConfiguration = configuration
				.getAdministratorConfiguration();
		RawBoundAdministratorMetaData<?, ?>[] workBoundAdmins;
		if ((adminConfiguration == null) || (adminConfiguration.length == 0)) {
			workBoundAdmins = new RawBoundAdministratorMetaData[0];
		} else {
			workBoundAdmins = rawBoundAdministratorFactory
					.constructRawBoundAdministratorMetaData(adminConfiguration,
							issues, AdministratorScope.WORK, AssetType.WORK,
							workName, rawOfficeMetaData.getTeams(),
							adminScopeMo);
		}
		Map<String, RawBoundAdministratorMetaData<?, ?>> workAdmin = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();
		for (RawBoundAdministratorMetaData<?, ?> admin : workBoundAdmins) {
			workAdmin.put(admin.getAdministratorName(), admin);
		}

		// Create the work scope administrators available to tasks
		Map<String, RawBoundAdministratorMetaData<?, ?>> taskScopeAdmin = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();
		for (LinkedWorkAdministratorConfiguration linkAdminConfiguration : configuration
				.getLinkedAdministratorConfiguration()) {

			// Obtain work administrator name
			String workAdministratorName = linkAdminConfiguration
					.getWorkAdministratorName();
			// TODO handle no name

			// Obtain the bound administrator name
			String boundAdministratorName = linkAdminConfiguration
					.getBoundAdministratorName();
			// TODO handle no name

			// Obtain the bound administrator from office scope
			RawBoundAdministratorMetaData<?, ?> admin = officeScopeAdmin
					.get(boundAdministratorName);
			// TODO handle unknown admin

			// Bound to work under work administrator name
			taskScopeAdmin.put(workAdministratorName, admin);
		}
		taskScopeAdmin.putAll(workAdmin); // second as may override names

		// Create the raw work meta-data
		RawWorkMetaDataImpl<w> rawWorkMetaData = new RawWorkMetaDataImpl<w>(
				workName, rawOfficeMetaData, workBoundMo, taskScopeMo,
				workBoundAdmins, taskScopeAdmin);

		// Obtain the name of the task for the initial flow of work
		String initialTaskName = configuration.getInitialTaskName();
		TaskMetaData<?, w, ?, ?> initialTaskMetaData = null;

		// Construct the task meta-data of this work (also find initial task)
		rawWorkMetaData.rawTaskMetaDatas = new LinkedList<RawTaskMetaData<?, w, ?, ?>>();
		List<TaskMetaData<?, w, ?, ?>> taskMetaDatas = new LinkedList<TaskMetaData<?, w, ?, ?>>();
		for (TaskConfiguration<?, w, ?, ?> taskConfiguration : configuration
				.getTaskConfiguration()) {

			// Construct and register the raw task meta-data
			RawTaskMetaData<?, w, ?, ?> rawTaskMetaData = rawTaskFactory
					.constructRawTaskMetaData(taskConfiguration, issues,
							rawWorkMetaData);
			if (rawTaskMetaData == null) {
				continue; // failed to construct the task
			}
			rawWorkMetaData.rawTaskMetaDatas.add(rawTaskMetaData);

			// Construct and register the task meta-data
			TaskMetaData<?, w, ?, ?> taskMetaData = rawTaskMetaData
					.getTaskMetaData();
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
				issues.addIssue(AssetType.WORK, workName,
						"No initial task by name '" + initialTaskName
								+ "' on work");
				return null; // must have initial task meta-data
			}

			// Construct the initial flow meta-data
			// TODO obtain the asset manager for the flow
			initialFlowMetaData = new FlowMetaDataImpl<w>(
					FlowInstigationStrategyEnum.ASYNCHRONOUS,
					initialTaskMetaData, null);
		}

		// Create the listing of managed object indexes
		ManagedObjectIndex[] managedObjectIndexes = new ManagedObjectIndex[rawWorkMetaData.workRequiredManagedObjects
				.size()];
		for (int i = 0; i < managedObjectIndexes.length; i++) {
			managedObjectIndexes[i] = rawWorkMetaData.workRequiredManagedObjects
					.get(i).getManagedObjectIndex();
		}

		// Create the listing of work bound managed object meta-data
		ManagedObjectMetaData<?>[] managedObjectMetaData = new ManagedObjectMetaData[rawWorkMetaData.workManagedObjects.length];
		for (int i = 0; i < managedObjectMetaData.length; i++) {
			managedObjectMetaData[i] = rawWorkMetaData.workManagedObjects[i]
					.getManagedObjectMetaData();
			// TODO handle managed object meta-data not available
		}

		// Create the listing of administrator indexes
		AdministratorIndex[] administratorIndexes = new AdministratorIndex[rawWorkMetaData.workRequiredAdministrators
				.size()];
		for (int i = 0; i < administratorIndexes.length; i++) {
			administratorIndexes[i] = rawWorkMetaData.workRequiredAdministrators
					.get(i).getAdministratorIndex();
		}

		// Create the listing of work bound administrator meta-data
		AdministratorMetaData<?, ?>[] administratorMetaData = new AdministratorMetaData[rawWorkMetaData.workAdministrators.length];
		for (int i = 0; i < administratorMetaData.length; i++) {
			administratorMetaData[i] = rawWorkMetaData.workAdministrators[i]
					.getAdministratorMetaData();
		}

		// Create the work meta-data
		rawWorkMetaData.workMetaData = new WorkMetaDataImpl<w>(
				rawWorkMetaData.workName, workFactory, managedObjectIndexes,
				managedObjectMetaData, administratorIndexes,
				administratorMetaData, initialFlowMetaData, ConstructUtil
						.toArray(taskMetaDatas, new TaskMetaData[0]));

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
	public RawWorkAdministratorMetaData constructRawWorkAdministratorMetaData(
			String workAdministratorName, OfficeFloorIssues issues) {

		// Work meta-data should not yet be created
		if (this.workMetaData != null) {
			throw new IllegalStateException(
					"Work meta-data already constructed when trying to add another administrator");
		}

		// Obtain the bound administrator
		RawBoundAdministratorMetaData<?, ?> boundAdmin = this.taskScopeAdministrators
				.get(workAdministratorName);
		// TODO handle unknown

		// Obtain the index details for the administrator
		AdministratorIndex adminIndex = boundAdmin.getAdministratorIndex();

		// Create the key for the administrator
		RawWorkAdministratorMetaDataImpl key = new RawWorkAdministratorMetaDataImpl(
				adminIndex, -1);

		// Obtain the work required administrator
		RawWorkAdministratorMetaDataImpl workRequiredAdministrator;
		if (this.workRequiredAdministrators.contains(key)) {
			// Already created
			workRequiredAdministrator = this.workRequiredAdministrators
					.get(this.workRequiredAdministrators.indexOf(key));

		} else {
			// Create the work required administrator
			workRequiredAdministrator = new RawWorkAdministratorMetaDataImpl(
					adminIndex, this.nextWorkRequiredAdministratorIndex++);

			// Ensure load as work required administrator
			this.workRequiredAdministrators.add(workRequiredAdministrator);
		}

		// Return the work bound administrator meta-data
		return workRequiredAdministrator;
	}

	@Override
	public RawWorkManagedObjectMetaData constructRawWorkManagedObjectMetaData(
			String workManagedObjectName, OfficeFloorIssues issues) {

		// Work meta-data should not yet be created
		if (this.workMetaData != null) {
			throw new IllegalStateException(
					"Work meta-data already constructed when trying to add another managed object");
		}

		// Obtain the bound managed object
		RawBoundManagedObjectMetaData<?> boundMo = this.taskScopeManagedObjects
				.get(workManagedObjectName);
		// TODO handle unknown

		// Construct and return the work managed object
		RawWorkManagedObjectMetaData workMo = this
				.constructRawWorkManagedObjectMetaData(boundMo, issues);
		return workMo;
	}

	@Override
	public WorkMetaData<W> getWorkMetaData(OfficeFloorIssues issues) {
		return this.workMetaData;
	}

	@Override
	public void linkTasks(TaskMetaDataLocator taskMetaDataLocator,
			OfficeFloorIssues issues) {

		// Link tasks of work bound managed objects
		for (RawBoundManagedObjectMetaData<?> rawBoundMoMetaData : this.workManagedObjects) {
			rawBoundMoMetaData.linkTasks(taskMetaDataLocator, issues);
		}

		// Link tasks of work bound administrators
		for (RawBoundAdministratorMetaData<?, ?> rawBoundAdminMetaData : this.workAdministrators) {
			rawBoundAdminMetaData.linkTasks(taskMetaDataLocator, issues);
		}

		// Link the tasks of this work
		for (RawTaskMetaData<?, W, ?, ?> rawTaskMetaData : this.rawTaskMetaDatas) {
			rawTaskMetaData.linkTasks(taskMetaDataLocator, this.workMetaData,
					issues);
		}
	}

	/**
	 * Constructs the {@link RawWorkManagedObjectMetaDataImpl} and its
	 * dependencies.
	 * 
	 * @param managedObject
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	private <D extends Enum<D>> RawWorkManagedObjectMetaDataImpl constructRawWorkManagedObjectMetaData(
			RawBoundManagedObjectMetaData<D> managedObject,
			OfficeFloorIssues issues) {

		// Obtain the index details for the managed object
		ManagedObjectIndex managedObjectIndex = managedObject
				.getManagedObjectIndex();

		// Create the key for the managed object
		RawWorkManagedObjectMetaDataImpl key = new RawWorkManagedObjectMetaDataImpl(
				managedObjectIndex, -1);

		// Obtain the work required managed object
		RawWorkManagedObjectMetaDataImpl workRequiredManagedObject;
		if (this.workRequiredManagedObjects.contains(key)) {
			// Already created
			workRequiredManagedObject = this.workRequiredManagedObjects
					.get(this.workRequiredManagedObjects.indexOf(key));

		} else {

			// Create the work required managed object meta-data
			workRequiredManagedObject = new RawWorkManagedObjectMetaDataImpl(
					managedObjectIndex,
					this.nextWorkRequiredManagedObjectIndex++);

			// Ensure load as work required managed object
			this.workRequiredManagedObjects.add(workRequiredManagedObject);

			// Load the dependencies
			for (D dependencyKey : managedObject.getDependencyKeys()) {

				// Obtain the dependency
				RawBoundManagedObjectMetaData<?> dependency = managedObject
						.getDependency(dependencyKey);
				if (dependency == null) {
					issues.addIssue(AssetType.WORK, workName,
							"Dependency for key " + dependencyKey
									+ " of managed object "
									+ managedObject.getBoundManagedObjectName()
									+ " not available");
					continue; // dependency not available
				}

				// Recursively load dependencies
				RawWorkManagedObjectMetaDataImpl rawWorkMoDependency = this
						.constructRawWorkManagedObjectMetaData(dependency,
								issues);

				// Add all the dependencies
				workRequiredManagedObject.dependencies.add(rawWorkMoDependency);
				workRequiredManagedObject.dependencies
						.addAll(rawWorkMoDependency.dependencies);
			}
		}

		// Return the work bound managed object meta-data
		return workRequiredManagedObject;
	}

	/**
	 * {@link RawWorkManagedObjectMetaData} implementation.
	 */
	public static class RawWorkManagedObjectMetaDataImpl implements
			RawWorkManagedObjectMetaData {

		/**
		 * {@link ManagedObjectIndex}.
		 */
		private final ManagedObjectIndex managedObjectIndex;

		/**
		 * Index of this {@link RawWorkManagedObjectMetaDataImpl} within the
		 * {@link Work}.
		 */
		private final int workManagedObjectIndex;

		/**
		 * All recursive dependencies for this
		 * {@link RawWorkManagedObjectMetaDataImpl}.
		 */
		private final Set<RawWorkManagedObjectMetaDataImpl> dependencies = new HashSet<RawWorkManagedObjectMetaDataImpl>();

		/**
		 * Initiate.
		 * 
		 * @param managedObjectIndex
		 *            {@link ManagedObjectIndex}.
		 * @param workManagedObjectIndex
		 *            Index of this {@link RawWorkManagedObjectMetaDataImpl}
		 *            within the {@link Work}.
		 */
		public RawWorkManagedObjectMetaDataImpl(
				ManagedObjectIndex managedObjectIndex,
				int workManagedObjectIndex) {
			this.managedObjectIndex = managedObjectIndex;
			this.workManagedObjectIndex = workManagedObjectIndex;
		}

		/*
		 * ================== Object =================================
		 */

		@Override
		public boolean equals(Object obj) {

			// Same object
			if (obj == this) {
				return true;
			}

			// Ensure same type
			if (!(obj instanceof RawWorkManagedObjectMetaDataImpl)) {
				return false;
			}
			RawWorkManagedObjectMetaDataImpl that = (RawWorkManagedObjectMetaDataImpl) obj;

			// Return if same index
			return (this.managedObjectIndex.getManagedObjectScope() == that.managedObjectIndex
					.getManagedObjectScope())
					&& (this.managedObjectIndex
							.getIndexOfManagedObjectWithinScope() == that.managedObjectIndex
							.getIndexOfManagedObjectWithinScope());
		}

		@Override
		public int hashCode() {
			int hash = this.managedObjectIndex.getManagedObjectScope()
					.hashCode()
					+ this.managedObjectIndex
							.getIndexOfManagedObjectWithinScope();
			return hash;
		}

		/*
		 * ================ RawWorkManagedObjectMetaData =====================
		 */

		@Override
		public ManagedObjectIndex getManagedObjectIndex() {
			return this.managedObjectIndex;
		}

		@Override
		public int getWorkManagedObjectIndex() {
			return this.workManagedObjectIndex;
		}

		@Override
		public RawWorkManagedObjectMetaData[] getDependencies() {
			return this.dependencies
					.toArray(new RawWorkManagedObjectMetaData[0]);
		}
	}

	/**
	 * {@link RawWorkAdministratorMetaData} implementation.
	 */
	private static class RawWorkAdministratorMetaDataImpl implements
			RawWorkAdministratorMetaData {

		/**
		 * {@link AdministratorIndex}.
		 */
		private final AdministratorIndex administratorIndex;

		/**
		 * Index of this {@link RawWorkAdministratorMetaDataImpl} within the
		 * {@link Work}.
		 */
		private final int workAdministratorIndex;

		/**
		 * Initiate.
		 * 
		 * @param administratorIndex
		 *            {@link AdministratorIndex}.
		 * @param workAdministratorIndex
		 *            Index of this {@link RawWorkAdministratorMetaDataImpl}
		 *            within the {@link Work}.
		 */
		public RawWorkAdministratorMetaDataImpl(
				AdministratorIndex administratorIndex,
				int workAdministratorIndex) {
			this.administratorIndex = administratorIndex;
			this.workAdministratorIndex = workAdministratorIndex;
		}

		/*
		 * ============== Object ===========================================
		 */

		@Override
		public boolean equals(Object obj) {

			// Same object
			if (obj == this) {
				return true;
			}

			// Ensure same type
			if (!(obj instanceof RawWorkManagedObjectMetaDataImpl)) {
				return false;
			}
			RawWorkAdministratorMetaDataImpl that = (RawWorkAdministratorMetaDataImpl) obj;

			// Return if same index
			return (this.administratorIndex.getAdministratorScope() == that.administratorIndex
					.getAdministratorScope())
					&& (this.administratorIndex
							.getIndexOfAdministratorWithinScope() == that.administratorIndex
							.getIndexOfAdministratorWithinScope());
		}

		@Override
		public int hashCode() {
			int hash = this.administratorIndex.getAdministratorScope()
					.hashCode()
					+ this.administratorIndex
							.getIndexOfAdministratorWithinScope();
			return hash;
		}

		/*
		 * =============== RawWorkAdministratorMetaData ======================
		 */

		@Override
		public AdministratorIndex getAdministratorIndex() {
			return this.administratorIndex;
		}

		@Override
		public int getWorkAdministratorIndex() {
			return this.workAdministratorIndex;
		}
	}

}