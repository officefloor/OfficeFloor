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
package net.officefloor.frame.impl.construct.administrator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.compile.spi.administration.source.AdministratorSource;
import net.officefloor.compile.spi.administration.source.AdministratorSourceContext;
import net.officefloor.compile.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.administration.DutyKey;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.administrator.AdministratorIndexImpl;
import net.officefloor.frame.impl.execute.administrator.AdministrationMetaDataImpl;
import net.officefloor.frame.impl.execute.administrator.ExtensionInterfaceMetaDataImpl;
import net.officefloor.frame.impl.execute.duty.DutyKeyImpl;
import net.officefloor.frame.impl.execute.duty.DutyMetaDataImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.ManagedFunctionLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AdministrationDuty;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Raw meta-data for the bound {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundAdministratorMetaDataImpl<I, A extends Enum<A>>
		implements RawBoundAdministratorMetaDataFactory, RawBoundAdministratorMetaData<I, A> {

	/**
	 * Obtains the {@link RawBoundAdministratorMetaDataFactory}.
	 * 
	 * @return {@link RawBoundAdministratorMetaDataFactory}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static RawBoundAdministratorMetaDataFactory getFactory() {
		return new RawBoundAdministratorMetaDataImpl(null, null, null, null, null, null, null);
	}

	/**
	 * Name the {@link Administration} is bound under.
	 */
	private final String boundAdministratorName;

	/**
	 * {@link AdministratorIndex}.
	 */
	private final AdministratorIndex administratorIndex;

	/**
	 * {@link AdministrationConfiguration}.
	 */
	private final AdministrationConfiguration<A, ?> administratorSourceConfiguration;

	/**
	 * Administered {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData[] administeredRawBoundManagedObjects;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaDataImpl<I, A> adminMetaData;

	/**
	 * Map of {@link DutyKey} instances by name.
	 */
	private final Map<String, DutyKey<A>> dutyKeysByName;

	/**
	 * Map of {@link DutyKey} instances by key.
	 */
	private final Map<A, DutyKey<A>> dutyKeysByKey;

	/**
	 * {@link DutyKey} instances of the {@link AdministrationDuty} instances linked to a
	 * {@link ManagedFunction}.
	 */
	private final Set<DutyKey<A>> linkedDutyKeys = new HashSet<DutyKey<A>>();

	/**
	 * Initiate.
	 * 
	 * @param boundAdministratorName
	 *            Name the {@link Administration} is bound under.
	 * @param administratorIndex
	 *            {@link AdministratorIndex}.
	 * @param administratorSourceConfiguration
	 *            {@link AdministrationConfiguration}.
	 * @param administeredRawBoundManagedObjects
	 *            Administered {@link RawBoundManagedObjectMetaData}.
	 * @param dutyKeysByName
	 *            Map of {@link DutyKey} instances by name.
	 * @param dutyKeysByKey
	 *            Map of {@link DutyKey} instances by key.
	 * @param adminMetaData
	 *            {@link AdministrationMetaData}.
	 */
	private RawBoundAdministratorMetaDataImpl(String boundAdministratorName, AdministratorIndex administratorIndex,
			AdministrationConfiguration<A, ?> administratorSourceConfiguration,
			RawBoundManagedObjectMetaData[] administeredRawBoundManagedObjects, Map<String, DutyKey<A>> dutyKeysByName,
			Map<A, DutyKey<A>> dutyKeysByKey, AdministrationMetaDataImpl<I, A> adminMetaData) {
		this.boundAdministratorName = boundAdministratorName;
		this.administratorIndex = administratorIndex;
		this.administratorSourceConfiguration = administratorSourceConfiguration;
		this.administeredRawBoundManagedObjects = administeredRawBoundManagedObjects;
		this.dutyKeysByName = dutyKeysByName;
		this.dutyKeysByKey = dutyKeysByKey;
		this.adminMetaData = adminMetaData;
	}

	/*
	 * =========== RawBoundAdministratorMetaDataFactory ===================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RawBoundAdministratorMetaData<?, ?>[] constructRawBoundAdministratorMetaData(
			AdministrationConfiguration<?, ?>[] configuration, SourceContext sourceContext, OfficeFloorIssues issues,
			AdministratorScope administratorScope, AssetType assetType, String assetName,
			Map<String, TeamManagement> officeTeams, Map<String, RawBoundManagedObjectMetaData> scopeMo,
			FunctionLoop functionLoop) {

		// Register the bound administrators
		List<RawBoundAdministratorMetaData<?, ?>> boundAdministrators = new LinkedList<RawBoundAdministratorMetaData<?, ?>>();
		int boundAdminIndex = 0;
		for (AdministrationConfiguration config : configuration) {

			// Create the administrator index
			AdministratorIndex adminIndex = new AdministratorIndexImpl(administratorScope, boundAdminIndex++);

			// Construct the bound administrator
			RawBoundAdministratorMetaData<?, ?> rawMetaData = constructRawBoundAdministratorMetaData(config,
					sourceContext, issues, adminIndex, assetType, assetName, officeTeams, scopeMo, functionLoop);
			if (rawMetaData != null) {
				boundAdministrators.add(rawMetaData);
			}
		}

		// Return the bound administrators
		return boundAdministrators.toArray(new RawBoundAdministratorMetaData[0]);
	}

	/**
	 * Provides typed construction of a {@link RawBoundAdministratorMetaData}.
	 * 
	 * @param configuration
	 *            {@link AdministrationConfiguration} instances.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param administratorIndex
	 *            {@link AdministratorIndex}.
	 * @param assetType
	 *            {@link AssetType} constructing {@link Administration}
	 *            instances.
	 * @param assetName
	 *            Name of {@link Asset} constructing {@link Administration}
	 *            instances.
	 * @param officeTeams
	 *            {@link TeamManagement} instances by their {@link Office}
	 *            registered names.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 * @return Constructed {@link RawBoundAdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private <a extends Enum<a>, i, AS extends AdministratorSource<i, a>> RawBoundAdministratorMetaData<i, a> constructRawBoundAdministratorMetaData(
			AdministrationConfiguration<a, AS> configuration, SourceContext sourceContext, OfficeFloorIssues issues,
			AdministratorIndex administratorIndex, AssetType assetType, String assetName,
			Map<String, TeamManagement> officeTeams, Map<String, RawBoundManagedObjectMetaData> scopeMo,
			FunctionLoop functionLoop) {

		// Obtain the administrator name
		String adminName = configuration.getAdministratorName();
		if (ConstructUtil.isBlank(adminName)) {
			issues.addIssue(assetType, assetName, "Administrator added without a name");
			return null; // no name
		}

		// Obtain the administrator source
		Class<AS> adminSourceClass = configuration.getAdministratorSourceClass();
		if (adminSourceClass == null) {
			issues.addIssue(assetType, assetName, "Administrator '" + adminName + "' did not provide an "
					+ AdministratorSource.class.getSimpleName() + " class");
			return null; // no class
		}

		// Obtain the administrator source instance
		AS adminSource = (AS) ConstructUtil.newInstance(adminSourceClass, AdministratorSource.class,
				"Administrator '" + adminName + "'", assetType, assetName, issues);
		if (adminSource == null) {
			return null; // no instance
		}

		// Obtain context to initialise the administrator source
		SourceProperties properties = configuration.getProperties();
		AdministratorSourceContext context = new AdministratorSourceContextImpl(false, properties, sourceContext);

		try {
			// Initialise the administrator source
			adminSource.init(context);

		} catch (UnknownPropertyError ex) {
			issues.addIssue(assetType, assetName, "Property '" + ex.getUnknownPropertyName() + "' must be specified");
			return null; // must have property

		} catch (UnknownClassError ex) {
			issues.addIssue(assetType, assetName, "Can not load class '" + ex.getUnknownClassName() + "'");
			return null; // must have class

		} catch (UnknownResourceError ex) {
			issues.addIssue(assetType, assetName,
					"Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "'");
			return null; // must have resource

		} catch (Throwable ex) {
			issues.addIssue(assetType, assetName, "Failed to initialise Administrator " + adminName, ex);
			return null; // not initialised
		}

		// Ensure have the meta-data
		AdministratorSourceMetaData<i, a> metaData = adminSource.getMetaData();
		if (metaData == null) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName + " must provide "
					+ AdministratorSourceMetaData.class.getSimpleName());
			return null; // must provide meta-data
		}

		// Obtain the team responsible for the duties
		String teamName = configuration.getOfficeTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(assetType, assetName,
					"Administrator " + adminName + " must specify team responsible for duties");
			return null; // must have team specified
		}
		TeamManagement responsibleTeam = officeTeams.get(teamName);
		if (responsibleTeam == null) {
			issues.addIssue(assetType, assetName,
					"Administrator " + adminName + " team '" + teamName + "' can not be found");
			return null; // unknown team
		}

		// Obtain the extension interface
		Class<i> extensionInterfaceType = metaData.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(assetType, assetName,
					"Administrator " + adminName + " did not provide extension interface type");
			return null; // no extension interface
		}

		// Obtain the managed objects being administered
		List<RawBoundManagedObjectMetaData> administeredManagedObjects = new LinkedList<RawBoundManagedObjectMetaData>();
		List<ExtensionInterfaceMetaData<i>> eiMetaDatas = new LinkedList<ExtensionInterfaceMetaData<i>>();
		for (String moName : configuration.getAdministeredManagedObjectNames()) {

			// Ensure have managed object name
			if (ConstructUtil.isBlank(moName)) {
				issues.addIssue(assetType, assetName,
						"Administrator " + adminName + " specifying no name for managed object");
				return null; // unspecified managed object name
			}

			// Obtain the managed object
			RawBoundManagedObjectMetaData mo = scopeMo.get(moName);
			if (mo == null) {
				issues.addIssue(assetType, assetName,
						"Managed Object '" + moName + "' not available to Administrator " + adminName);
				return null; // unknown managed object
			}

			// Obtain the extension factories for the managed object instances.
			// (Keeping order of factories as order of managed object instances)
			RawBoundManagedObjectInstanceMetaData<?>[] moInstances = mo.getRawBoundManagedObjectInstanceMetaData();
			ExtensionInterfaceFactory<i>[] extensionInterfaceFactories = new ExtensionInterfaceFactory[moInstances.length];
			boolean isExtensionInterfaceFactoryIssue = false;
			NEXT_INSTANCE: for (int i = 0; i < moInstances.length; i++) {
				RawBoundManagedObjectInstanceMetaData<?> moInstance = moInstances[i];

				// Obtain the extension factory for the managed object instance
				ExtensionInterfaceFactory<i> extensionInterfaceFactory = null;
				ManagedObjectExtensionInterfaceMetaData<?>[] moEiMetaDatas = moInstance.getRawManagedObjectMetaData()
						.getManagedObjectSourceMetaData().getExtensionInterfacesMetaData();
				if (moEiMetaDatas != null) {
					for (ManagedObjectExtensionInterfaceMetaData<?> moEiMetaData : moEiMetaDatas) {

						// Obtain the extension interface
						Class<?> moEiType = moEiMetaData.getExtensionInterfaceType();
						if ((moEiType != null) && (extensionInterfaceType.isAssignableFrom(moEiType))) {

							// Specify the extension interface factory
							extensionInterfaceFactory = (ExtensionInterfaceFactory<i>) moEiMetaData
									.getExtensionInterfaceFactory();
							if (extensionInterfaceFactory == null) {
								// Managed Object invalid
								isExtensionInterfaceFactoryIssue = true;
								issues.addIssue(assetType, assetName, "Managed Object did not provide "
										+ ExtensionInterfaceFactory.class.getSimpleName() + " for Administrator "
										+ adminName + " (ManagedObjectSource="
										+ moInstance.getRawManagedObjectMetaData().getManagedObjectName() + ")");
								continue NEXT_INSTANCE; // invalid
							}

							// Have extension interface factory for instance
							extensionInterfaceFactories[i] = extensionInterfaceFactory;
							continue NEXT_INSTANCE;
						}
					}
				}
				if (extensionInterfaceFactory == null) {
					// Managed Object invalid
					isExtensionInterfaceFactoryIssue = true;
					issues.addIssue(assetType, assetName,
							"Managed Object '" + moName + "' does not support extension interface "
									+ extensionInterfaceType.getName() + " required by Administrator " + adminName
									+ " (ManagedObjectSource="
									+ moInstance.getRawManagedObjectMetaData().getManagedObjectName() + ")");
					continue NEXT_INSTANCE; // invalid
				}
			}
			if (isExtensionInterfaceFactoryIssue) {
				return null; // managed object invalid
			}

			// Obtain the index of the managed object
			ManagedObjectIndex moIndex = mo.getManagedObjectIndex();

			// Add the administered managed object
			administeredManagedObjects.add(mo);

			// Add the extension interface meta-data
			eiMetaDatas.add(new ExtensionInterfaceMetaDataImpl<i>(moIndex, extensionInterfaceFactories));
		}

		// Obtain the duties meta-data
		AdministratorDutyMetaData<a, ?>[] dutyMetaDatas = metaData.getAdministratorDutyMetaData();
		if ((dutyMetaDatas == null) || (dutyMetaDatas.length == 0)) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName + " does not provide duties");
			return null; // must have duties
		}

		// Ensure all duty keys are of the correct type (report on all duties)
		boolean isDutyKeyIssue = false;
		Class<a> dutyKeyClass = null;
		Map<String, DutyKey<a>> dutyKeysByName = new HashMap<String, DutyKey<a>>();
		Map<a, DutyKey<a>> dutyKeysByKey = new HashMap<a, DutyKey<a>>();
		for (int i = 0; i < dutyMetaDatas.length; i++) {
			AdministratorDutyMetaData<a, ?> dutyMetaData = dutyMetaDatas[i];

			// Ensure have the duty name
			String dutyName = dutyMetaData.getDutyName();
			if (ConstructUtil.isBlank(dutyName)) {
				issues.addIssue(assetType, assetName, "No name provided for duty " + i);
				isDutyKeyIssue = true;
				continue; // can not process this duty
			}
			if (dutyKeysByName.containsKey(dutyName)) {
				issues.addIssue(assetType, assetName, "Duplicate duty by name " + dutyName);
				isDutyKeyIssue = true;
				continue; // can not process duplicate duty
			}

			// Obtain the duty key
			a dutyKey = dutyMetaData.getKey();
			if ((dutyKey != null) && (dutyKeysByKey.containsKey(dutyKey))) {
				issues.addIssue(assetType, assetName, "Duplicate duty by key " + dutyKey);
				isDutyKeyIssue = true;
				continue; // can not process duplicate duty
			}

			// Determine if should have duty key
			if (i == 0) {
				// First duty, so provide duty key class (if available)
				dutyKeyClass = (dutyKey == null ? null : dutyKey.getDeclaringClass());
			} else {
				// Ensure consistency of further duties in having key
				if (dutyKeyClass == null) {
					if (dutyKey != null) {
						issues.addIssue(assetType, assetName, "Duty meta-data provides only keys for some duties");
						return null; // can not load duties
					}
				} else {
					if (dutyKey == null) {
						issues.addIssue(assetType, assetName, "Duty meta-data provides only keys for some duties");
						return null; // can not load duties
					}

					// Ensure of duty key correct type
					if (!dutyKeyClass.isInstance(dutyKey)) {
						issues.addIssue(assetType, assetName,
								"Duty key " + dutyKey + " is of incorrect type [type="
										+ dutyKey.getDeclaringClass().getName() + ", required type="
										+ dutyKeyClass.getName() + "]");
						isDutyKeyIssue = true;
						continue; // must be correct duty key type
					}
				}
			}

			// Create the duty key
			DutyKey<a> key = (dutyKey == null ? new DutyKeyImpl<a>(i) : new DutyKeyImpl<a>(dutyKey));

			// Register the duty
			dutyKeysByName.put(dutyName, key);
			if (dutyKey != null) {
				dutyKeysByKey.put(dutyKey, key);
			}
		}
		if (isDutyKeyIssue) {
			return null; // should not be issue in obtaining duty keys
		}

		// TODO allow configuration of escalation procedure for administrator
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl();

		// Create the administrator meta-data
		AdministrationMetaDataImpl<i, a> adminMetaData = new AdministrationMetaDataImpl<i, a>(adminSource,
				ConstructUtil.toArray(eiMetaDatas, new ExtensionInterfaceMetaData[0]), responsibleTeam,
				escalationProcedure, functionLoop);

		// Create the raw bound administrator meta-data
		RawBoundAdministratorMetaData<i, a> rawBoundAdminMetaData = new RawBoundAdministratorMetaDataImpl<i, a>(
				adminName, administratorIndex, configuration,
				administeredManagedObjects.toArray(new RawBoundManagedObjectMetaData[0]), dutyKeysByName, dutyKeysByKey,
				adminMetaData);

		// Return the raw bound administrator meta-data
		return rawBoundAdminMetaData;
	}

	/*
	 * ========= RawBoundAdministratorMetaData =========================
	 */

	@Override
	public void linkOfficeMetaData(OfficeMetaData officeMetaData, ManagedFunctionLocator functionLocator,
			OfficeFloorIssues issues) {

		// Create the set of required duties to ensure they are configured
		Set<DutyKey<A>> requiredDuties = new HashSet<DutyKey<A>>(this.linkedDutyKeys);

		// Obtain the governance meta-data
		GovernanceMetaData<?, ?>[] governanceMetaDatas = officeMetaData.getProcessMetaData().getThreadMetaData()
				.getGovernanceMetaData();

		// Obtain the duty meta-data by its duty key index
		Map<Integer, DutyMetaData> dutyMetaData = new HashMap<Integer, DutyMetaData>();
		for (DutyConfiguration<A> dutyConfiguration : this.administratorSourceConfiguration.getDutyConfiguration()) {

			// Obtain the duty name
			String dutyName = dutyConfiguration.getDutyName();
			if (ConstructUtil.isBlank(dutyName)) {
				issues.addIssue(AssetType.ADMINISTRATOR, this.boundAdministratorName,
						"Duty name not provided by duty configuration");
				return; // must have duty name
			}

			// Obtain the duty key
			DutyKey<?> dutyKey = this.dutyKeysByName.get(dutyName);
			if (dutyKey == null) {
				issues.addIssue(AssetType.ADMINISTRATOR, this.boundAdministratorName, "No duty by name " + dutyName);
				return; // must have duty
			}

			// Remove the required duty as have configuration for it
			requiredDuties.remove(dutyKey);

			// Obtain the task node references
			ManagedFunctionReference[] dutyFunctionReferences = dutyConfiguration.getLinkedProcessConfiguration();
			if (dutyFunctionReferences == null) {
				issues.addIssue(AssetType.ADMINISTRATOR, this.boundAdministratorName,
						"Task references not provided for duty " + dutyName);
				return; // must have task references for duty
			}

			// Obtain the flows for the duty
			FlowMetaData[] dutyFlows = new FlowMetaData[dutyFunctionReferences.length];
			for (int i = 0; i < dutyFlows.length; i++) {
				ManagedFunctionReference functionReference = dutyFunctionReferences[i];

				// Obtain the function meta-data for the flow
				ManagedFunctionMetaData<?, ?> functionMetaData = ConstructUtil.getFunctionMetaData(functionReference,
						functionLocator, issues, AssetType.ADMINISTRATOR, this.boundAdministratorName,
						"Duty " + dutyName + " Flow " + i);
				if (functionMetaData == null) {
					return; // no function
				}

				// Create and register the flow for the duty.
				dutyFlows[i] = ConstructUtil.newFlowMetaData(functionMetaData, false);
			}

			// Obtain the governance mapping for the duty
			AdministrationGovernanceConfiguration<?>[] dutyGovernanceConfigurations = dutyConfiguration
					.getGovernanceConfiguration();
			int[] governanceMapping = new int[dutyGovernanceConfigurations.length];
			for (AdministrationGovernanceConfiguration<?> dutyGovernanceConfiguration : dutyGovernanceConfigurations) {

				// Index of governance for the duty
				int dutyGovernanceIndex = dutyGovernanceConfiguration.getIndex();

				// Obtain the governance
				String governanceName = dutyGovernanceConfiguration.getGovernanceName();
				int processGovernanceIndex = -1;
				for (int i = 0; i < governanceMetaDatas.length; i++) {
					GovernanceMetaData<?, ?> governanceMetaData = governanceMetaDatas[i];
					if (governanceName.equals(governanceMetaData.getGovernanceName())) {
						processGovernanceIndex = i; // found governance
					}
				}
				if (processGovernanceIndex < 0) {
					// Did not find the process governance
					issues.addIssue(AssetType.ADMINISTRATOR, this.boundAdministratorName,
							"Can not find governance '" + governanceName + "' for duty '" + dutyName + "'");
				}

				// Specify the governance mapping
				governanceMapping[dutyGovernanceIndex] = processGovernanceIndex;
			}

			// Create and register the duty meta-data
			dutyMetaData.put(new Integer(dutyKey.getIndex()), new DutyMetaDataImpl(dutyFlows, governanceMapping));
		}

		// Must have configuration for each required duty
		for (DutyKey<A> requiredDuty : requiredDuties) {
			issues.addIssue(AssetType.ADMINISTRATOR, this.boundAdministratorName,
					"Must provide configuration for duty [index=" + requiredDuty.getIndex() + ", key="
							+ requiredDuty.getKey() + "]");
			return; // must have configuration for each duty
		}

		// Obtain the listing of duty meta-data
		DutyMetaData[] metaData = ConstructUtil.toArray(dutyMetaData, new DutyMetaData[0]);

		// Load the duties to the administrator meta-data
		this.adminMetaData.loadRemainingState(metaData);
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
	public RawBoundManagedObjectMetaData[] getAdministeredRawBoundManagedObjects() {
		return this.administeredRawBoundManagedObjects;
	}

	@Override
	public AdministrationMetaData<I, A> getAdministratorMetaData() {
		return this.adminMetaData;
	}

	@Override
	public DutyKey<A> getDutyKey(Enum<?> key) {
		DutyKey<A> dutyKey = this.dutyKeysByKey.get(key);
		if (dutyKey != null) {
			this.linkedDutyKeys.add(dutyKey);
		}
		return dutyKey;
	}

	@Override
	public DutyKey<A> getDutyKey(String dutyName) {
		DutyKey<A> dutyKey = this.dutyKeysByName.get(dutyName);
		if (dutyKey != null) {
			this.linkedDutyKeys.add(dutyKey);
		}
		return dutyKey;
	}

}