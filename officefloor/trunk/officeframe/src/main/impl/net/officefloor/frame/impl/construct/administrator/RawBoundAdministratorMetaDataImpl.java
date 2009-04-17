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
package net.officefloor.frame.impl.construct.administrator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.administrator.AdministratorIndexImpl;
import net.officefloor.frame.impl.execute.administrator.AdministratorMetaDataImpl;
import net.officefloor.frame.impl.execute.administrator.ExtensionInterfaceMetaDataImpl;
import net.officefloor.frame.impl.execute.duty.DutyMetaDataImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceUnknownPropertyError;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data for the bound {@link Administrator}.
 * 
 * @author Daniel
 */
public class RawBoundAdministratorMetaDataImpl<I, A extends Enum<A>> implements
		RawBoundAdministratorMetaDataFactory,
		RawBoundAdministratorMetaData<I, A> {

	/**
	 * Obtains the {@link RawBoundAdministratorMetaDataFactory}.
	 * 
	 * @return {@link RawBoundAdministratorMetaDataFactory}.
	 */
	@SuppressWarnings("unchecked")
	public static RawBoundAdministratorMetaDataFactory getFactory() {
		return new RawBoundAdministratorMetaDataImpl(null, null, null, null,
				null, null, null);
	}

	/**
	 * Name the {@link Administrator} is bound under.
	 */
	private final String boundAdministratorName;

	/**
	 * {@link AdministratorIndex}.
	 */
	private final AdministratorIndex administratorIndex;

	/**
	 * {@link AdministratorSourceConfiguration}.
	 */
	private final AdministratorSourceConfiguration<A, ?> administratorSourceConfiguration;

	/**
	 * Administered {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData<?>[] administeredRawBoundManagedObjects;

	/**
	 * Class specifying the {@link Duty} keys.
	 */
	private final Class<A> dutyKeyClass;

	/**
	 * {@link Duty} keys.
	 */
	private final A[] dutyKeys;

	/**
	 * {@link AdministratorMetaData}.
	 */
	private final AdministratorMetaDataImpl<I, A> adminMetaData;

	/**
	 * Initiate.
	 * 
	 * @param boundAdministratorName
	 *            Name the {@link Administrator} is bound under.
	 * @param administratorIndex
	 *            {@link AdministratorIndex}.
	 * @param administratorSourceConfiguration
	 *            {@link AdministratorSourceConfiguration}.
	 * @param administeredRawBoundManagedObjects
	 *            Administered {@link RawBoundManagedObjectMetaData}.
	 * @param dutyKeyClass
	 *            Class specifying the {@link Duty} keys.
	 * @param dutyKeys
	 *            Keys to the {@link Duty} instances.
	 * @param adminMetaData
	 *            {@link AdministratorMetaData}.
	 */
	private RawBoundAdministratorMetaDataImpl(
			String boundAdministratorName,
			AdministratorIndex administratorIndex,
			AdministratorSourceConfiguration<A, ?> administratorSourceConfiguration,
			RawBoundManagedObjectMetaData<?>[] administeredRawBoundManagedObjects,
			Class<A> dutyKeyClass, A[] dutyKeys,
			AdministratorMetaDataImpl<I, A> adminMetaData) {
		this.boundAdministratorName = boundAdministratorName;
		this.administratorIndex = administratorIndex;
		this.administratorSourceConfiguration = administratorSourceConfiguration;
		this.administeredRawBoundManagedObjects = administeredRawBoundManagedObjects;
		this.dutyKeyClass = dutyKeyClass;
		this.dutyKeys = dutyKeys;
		this.adminMetaData = adminMetaData;
	}

	/*
	 * =========== RawBoundAdministratorMetaDataFactory ===================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public RawBoundAdministratorMetaData<?, ?>[] constructRawBoundAdministratorMetaData(
			AdministratorSourceConfiguration<?, ?>[] configuration,
			OfficeFloorIssues issues, AdministratorScope administratorScope,
			AssetType assetType, String assetName,
			Map<String, Team> officeTeams,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeMo) {

		// Register the bound administrators
		List<RawBoundAdministratorMetaData<?, ?>> boundAdministrators = new LinkedList<RawBoundAdministratorMetaData<?, ?>>();
		int boundAdminIndex = 0;
		for (AdministratorSourceConfiguration config : configuration) {

			// Create the administrator index
			AdministratorIndex adminIndex = new AdministratorIndexImpl(
					administratorScope, boundAdminIndex++);

			// Construct the bound administrator
			RawBoundAdministratorMetaData<?, ?> rawMetaData = constructRawBoundAdministratorMetaData(
					config, issues, adminIndex, assetType, assetName,
					officeTeams, scopeMo);
			if (rawMetaData != null) {
				boundAdministrators.add(rawMetaData);
			}
		}

		// Return the bound administrators
		return boundAdministrators
				.toArray(new RawBoundAdministratorMetaData[0]);
	}

	/**
	 * Provides typed construction of a {@link RawBoundAdministratorMetaData}.
	 * 
	 * @param configuration
	 *            {@link AdministratorSourceConfiguration} instances.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param administratorIndex
	 *            {@link AdministratorIndex}.
	 * @param assetType
	 *            {@link AssetType} constructing {@link Administrator}
	 *            instances.
	 * @param assetName
	 *            Name of {@link Asset} constructing {@link Administrator}
	 *            instances.
	 * @param officeTeams
	 *            {@link Team} instances by their {@link Office} registered
	 *            names.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @return Constructed {@link RawBoundAdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private <a extends Enum<a>, i, AS extends AdministratorSource<i, a>> RawBoundAdministratorMetaData<i, a> constructRawBoundAdministratorMetaData(
			AdministratorSourceConfiguration<a, AS> configuration,
			OfficeFloorIssues issues, AdministratorIndex administratorIndex,
			AssetType assetType, String assetName,
			Map<String, Team> officeTeams,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeMo) {

		// Obtain the administrator name
		String adminName = configuration.getAdministratorName();
		if (ConstructUtil.isBlank(adminName)) {
			issues.addIssue(assetType, assetName,
					"Administrator added without a name");
			return null; // no name
		}

		// Obtain the administrator source
		Class<AS> adminSourceClass = configuration
				.getAdministratorSourceClass();
		if (adminSourceClass == null) {
			issues.addIssue(assetType, assetName, "Administrator '" + adminName
					+ "' did not provide an "
					+ AdministratorSource.class.getSimpleName() + " class");
			return null; // no class
		}

		// Obtain the administrator source instance
		AS adminSource = (AS) ConstructUtil.newInstance(adminSourceClass,
				AdministratorSource.class, "Administrator '" + adminName + "'",
				assetType, assetName, issues);
		if (adminSource == null) {
			return null; // no instance
		}

		// Obtain context to initialise the administrator source
		Properties properties = configuration.getProperties();
		AdministratorSourceContext context = new AdministratorSourceContextImpl(
				properties);

		try {
			// Initialise the administrator source
			adminSource.init(context);

		} catch (AdministratorSourceUnknownPropertyError ex) {
			issues.addIssue(assetType, assetName, "Property '"
					+ ex.getUnknownPropertyName() + "' must be specified");
			return null; // must have property

		} catch (Throwable ex) {
			issues.addIssue(assetType, assetName,
					"Failed to initialise Administrator " + adminName, ex);
			return null; // not initialised
		}

		// Ensure have the meta-data
		AdministratorSourceMetaData<i, a> metaData = adminSource.getMetaData();
		if (metaData == null) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " must provide "
					+ AdministratorSourceMetaData.class.getSimpleName());
			return null; // must provide meta-data
		}

		// Obtain the team responsible for the duties
		String teamName = configuration.getOfficeTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " must specify team responsible for duties");
			return null; // must have team specified
		}
		Team team = officeTeams.get(teamName);
		if (team == null) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " team '" + teamName + "' can not be found");
			return null; // unknown team
		}

		// Obtain the extension interface
		Class<i> extensionInterfaceType = metaData.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " did not provide extension interface type");
			return null; // no extension interface
		}

		// Obtain the managed objects being administered
		List<RawBoundManagedObjectMetaData<?>> administeredManagedObjects = new LinkedList<RawBoundManagedObjectMetaData<?>>();
		List<ExtensionInterfaceMetaData<i>> eiMetaDatas = new LinkedList<ExtensionInterfaceMetaData<i>>();
		for (String moName : configuration.getAdministeredManagedObjectNames()) {

			// Ensure have managed object name
			if (ConstructUtil.isBlank(moName)) {
				issues.addIssue(assetType, assetName, "Administrator "
						+ adminName + " specifying no name for managed object");
				return null; // unspecified managed object name
			}

			// Obtain the managed object
			RawBoundManagedObjectMetaData<?> mo = scopeMo.get(moName);
			if (mo == null) {
				issues.addIssue(assetType, assetName, "Managed Object '"
						+ moName + "' not available to Administrator "
						+ adminName);
				return null; // unknown managed object
			}

			// Obtain the extension factory for the managed object
			ExtensionInterfaceFactory<i> extensionInterfaceFactory = null;
			ManagedObjectExtensionInterfaceMetaData<?>[] moEiMetaDatas = mo
					.getRawManagedObjectMetaData()
					.getManagedObjectSourceMetaData()
					.getExtensionInterfacesMetaData();
			if (moEiMetaDatas != null) {
				for (ManagedObjectExtensionInterfaceMetaData<?> moEiMetaData : moEiMetaDatas) {

					// Obtain the extension interface
					Class<?> moEiType = moEiMetaData
							.getExtensionInterfaceType();
					if ((moEiType != null)
							&& (extensionInterfaceType
									.isAssignableFrom(moEiType))) {

						// Specify the extension interface factory
						extensionInterfaceFactory = (ExtensionInterfaceFactory<i>) moEiMetaData
								.getExtensionInterfaceFactory();
						if (extensionInterfaceFactory == null) {
							issues
									.addIssue(
											assetType,
											assetName,
											"Managed Object did not provide "
													+ ExtensionInterfaceFactory.class
															.getSimpleName()
													+ " for Administrator "
													+ adminName);
							return null; // managed object invalid
						}
						break; // have extension interface factory
					}
				}
			}
			if (extensionInterfaceFactory == null) {
				issues.addIssue(assetType, assetName, "Managed Object '"
						+ moName + "' does not support extension interface "
						+ extensionInterfaceType.getName()
						+ " required by Administrator " + adminName);
				return null; // managed object invalid
			}

			// Obtain the index of the managed object
			ManagedObjectIndex moIndex = mo.getManagedObjectIndex();

			// Add the administered managed object
			administeredManagedObjects.add(mo);

			// Add the extension interface meta-data
			eiMetaDatas.add(new ExtensionInterfaceMetaDataImpl<i>(moIndex,
					extensionInterfaceFactory));
		}

		// Obtain the duties meta-data
		AdministratorDutyMetaData<a, ?>[] dutyMetaDatas = metaData
				.getAdministratorDutyMetaData();
		if ((dutyMetaDatas == null) || (dutyMetaDatas.length == 0)) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " does not provide duties");
			return null; // must have duties
		}

		// Ensure all duty keys are of the correct type (report on all duties)
		boolean isDutyKeyIssue = false;
		Class<a> dutyKeyClass = null;
		for (int i = 0; i < dutyMetaDatas.length; i++) {
			AdministratorDutyMetaData<a, ?> dutyMetaData = dutyMetaDatas[i];

			// Ensure have the duty key
			a dutyKey = dutyMetaData.getKey();
			if (dutyKey == null) {
				issues.addIssue(assetType, assetName,
						"No key provided for duty " + i);
				isDutyKeyIssue = true;
				continue; // can not process this duty
			}

			// Determine if first duty key which sets the type of duty keys
			if (dutyKeyClass == null) {
				// First duty key
				dutyKeyClass = dutyKey.getDeclaringClass();

			} else {
				// Ensure subsequent duty keys of correct type
				if (!dutyKeyClass.isInstance(dutyKey)) {
					issues.addIssue(assetType, assetName, "Duty key " + dutyKey
							+ " is of incorrect type [type="
							+ dutyKey.getClass().getName() + ", required type="
							+ dutyKeyClass.getName() + "]");
					isDutyKeyIssue = true;
				}
			}
		}
		if (isDutyKeyIssue) {
			return null; // should not be issue in obtaining duty keys
		}

		// Obtain the duty keys ensuring in ordinal order
		a[] dutyKeys = (dutyKeyClass == null ? null : dutyKeyClass
				.getEnumConstants());
		Arrays.sort(dutyKeys, new Comparator<a>() {
			@Override
			public int compare(a objA, a objB) {
				return objA.ordinal() - objB.ordinal();
			}
		});

		// TODO allow configuration of escalation procedure for administrator
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl();

		// Create the administrator meta-data
		AdministratorMetaDataImpl<i, a> adminMetaData = new AdministratorMetaDataImpl<i, a>(
				adminSource, ConstructUtil.toArray(eiMetaDatas,
						new ExtensionInterfaceMetaData[0]), team,
				escalationProcedure);

		// Create the raw bound administrator meta-data
		RawBoundAdministratorMetaData<i, a> rawBoundAdminMetaData = new RawBoundAdministratorMetaDataImpl<i, a>(
				adminName, administratorIndex, configuration,
				administeredManagedObjects
						.toArray(new RawBoundManagedObjectMetaData[0]),
				dutyKeyClass, dutyKeys, adminMetaData);

		// Return the raw bound administrator meta-data
		return rawBoundAdminMetaData;
	}

	/*
	 * ========= RawBoundAdministratorMetaData =========================
	 */

	@Override
	public void linkTasks(OfficeMetaDataLocator taskLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Obtain the duty configurations by duty keys
		Map<A, TaskNodeReference[]> dutyToTaskReferences = new HashMap<A, TaskNodeReference[]>();
		for (DutyConfiguration<A> dutyConfiguration : this.administratorSourceConfiguration
				.getDutyConfiguration()) {

			// Obtain the duty key
			A dutyKey = dutyConfiguration.getDutyKey();
			if (dutyKey == null) {
				issues.addIssue(AssetType.ADMINISTRATOR,
						this.boundAdministratorName,
						"Duty key not provided by duty configuration");
				return; // must have duty key
			}

			// Ensure of duty key correct type
			if (!this.dutyKeyClass.isInstance(dutyKey)) {
				issues.addIssue(AssetType.ADMINISTRATOR,
						this.boundAdministratorName, "Duty key " + dutyKey
								+ " is not of correct type ("
								+ this.dutyKeyClass.getName() + ")");
				return; // must be correct duty key type
			}

			// Obtain the task node references
			TaskNodeReference[] taskNodeReferences = dutyConfiguration
					.getLinkedProcessConfiguration();
			if (taskNodeReferences == null) {
				issues.addIssue(AssetType.ADMINISTRATOR,
						this.boundAdministratorName,
						"Task references not provided for duty " + dutyKey);
				return; // must have task references for duty
			}

			// Register the duty task references
			dutyToTaskReferences.put(dutyKey, taskNodeReferences);
		}

		// Create the map for the duties
		Map<A, DutyMetaData> dutyMetaData = new EnumMap<A, DutyMetaData>(
				this.dutyKeyClass);
		for (A dutyKey : this.dutyKeys) {

			// Obtain the task references for the duty
			TaskNodeReference[] dutyTaskReferences = dutyToTaskReferences
					.get(dutyKey);
			if (dutyTaskReferences == null) {
				issues.addIssue(AssetType.ADMINISTRATOR,
						this.boundAdministratorName,
						"No configuration provided for duty " + dutyKey);
				return; // no configured task references for duty
			}

			// Obtain the flows for the duty
			FlowMetaData<?>[] dutyFlows = new FlowMetaData[dutyTaskReferences.length];
			for (int i = 0; i < dutyFlows.length; i++) {
				TaskNodeReference taskReference = dutyTaskReferences[i];

				// Obtain the task meta-data for the flow
				TaskMetaData<?, ?, ?> taskMetaData = ConstructUtil
						.getTaskMetaData(taskReference, taskLocator, issues,
								AssetType.ADMINISTRATOR,
								this.boundAdministratorName, "Duty " + dutyKey
										+ " Flow " + i, true);
				if (taskMetaData == null) {
					return; // no task
				}

				// Create and register the flow for the duty.
				// All direct duty flows are invoked in parallel.
				dutyFlows[i] = ConstructUtil.newFlowMetaData(
						FlowInstigationStrategyEnum.PARALLEL, taskMetaData,
						assetManagerFactory, AssetType.ADMINISTRATOR,
						this.boundAdministratorName, "Duty " + dutyKey
								+ " Flow " + i, issues);
			}

			// Create and register the duty meta-data
			dutyMetaData.put(dutyKey, new DutyMetaDataImpl(dutyFlows));
		}

		// Load the duties to the administrator meta-data
		this.adminMetaData.loadRemainingState(dutyMetaData);
	}

	@Override
	public String getBoundAdministratorName() {
		return this.boundAdministratorName;
	}

	@Override
	public AdministratorIndex getAdministratorIndex() {
		return this.administratorIndex;
	}

	@Override
	public RawBoundManagedObjectMetaData<?>[] getAdministeredRawBoundManagedObjects() {
		return this.administeredRawBoundManagedObjects;
	}

	@Override
	public A[] getDutyKeys() {
		return this.dutyKeys;
	}

	@Override
	public AdministratorMetaData<I, A> getAdministratorMetaData() {
		return this.adminMetaData;
	}

}