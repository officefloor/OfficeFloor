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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.administrator.AdministrationMetaDataImpl;
import net.officefloor.frame.impl.execute.administrator.ExtensionInterfaceMetaDataImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.AdministrationMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Raw meta-data for the bound {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationMetaDataFactoryImpl implements AdministrationMetaDataFactory {

	/**
	 * Obtains the {@link AdministrationMetaDataFactory}.
	 * 
	 * @return {@link AdministrationMetaDataFactory}.
	 */
	public static AdministrationMetaDataFactory getFactory() {
		return new AdministrationMetaDataFactoryImpl();
	}

	/*
	 * ====================== AdministrationMetaDataFactory ===================
	 */

	@Override
	public AdministrationMetaData<?, ?, ?>[] constructAdministrationMetaData(
			AdministrationConfiguration<?, ?, ?>[] configuration, AssetType assetType, String assetName,
			Map<String, TeamManagement> officeTeams, ManagedFunctionLocator functionLocator,
			Map<String, RawBoundManagedObjectMetaData> scopeMo, GovernanceMetaData<?, ?>[] governanceMetaDatas,
			FunctionLoop functionLoop, OfficeFloorIssues issues) {

		// Create the administrators
		List<AdministrationMetaData<?, ?, ?>> administrations = new LinkedList<AdministrationMetaData<?, ?, ?>>();
		for (AdministrationConfiguration<?, ?, ?> administrationConfiguration : configuration) {

			// Construct the bound administrator
			AdministrationMetaData<?, ?, ?> administration = constructAdministrationMetaData(
					administrationConfiguration, issues, assetType, assetName, officeTeams, functionLocator, scopeMo,
					governanceMetaDatas, functionLoop);
			if (administration != null) {
				administrations.add(administration);
			}
		}

		// Return the administrations
		return administrations.toArray(new AdministrationMetaData[0]);
	}

	/**
	 * Provides typed construction of a {@link AdministrationMetaData}.
	 * 
	 * @param configuration
	 *            {@link AdministrationConfiguration} instances.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param assetType
	 *            {@link AssetType} constructing {@link Administration}
	 *            instances.
	 * @param assetName
	 *            Name of {@link Asset} constructing {@link Administration}
	 *            instances.
	 * @param officeTeams
	 *            {@link TeamManagement} instances by their {@link Office}
	 *            registered names.
	 * @param functionLocator
	 *            {@link ManagedFunctionLocator} for the {@link Office}.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @param governanceMetaDatas
	 *            {@link GovernanceMetaData} instances.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 * @return Constructed {@link AdministrationMetaData}.
	 */
	private <E, F extends Enum<F>, G extends Enum<G>> AdministrationMetaData<E, F, G> constructAdministrationMetaData(
			AdministrationConfiguration<E, F, G> configuration, OfficeFloorIssues issues, AssetType assetType,
			String assetName, Map<String, TeamManagement> officeTeams, ManagedFunctionLocator functionLocator,
			Map<String, RawBoundManagedObjectMetaData> scopeMo, GovernanceMetaData<?, ?>[] governanceMetaDatas,
			FunctionLoop functionLoop) {

		// Obtain the administration name
		String adminName = configuration.getAdministrationName();
		if (ConstructUtil.isBlank(adminName)) {
			issues.addIssue(assetType, assetName, "Administrator added without a name");
			return null; // no name
		}

		// Obtain the administrator factory
		AdministrationFactory<E, F, G> adminFactory = configuration.getAdministrationFactory();
		if (adminFactory == null) {
			issues.addIssue(assetType, assetName, "Administrator '" + adminName + "' did not provide an "
					+ AdministrationFactory.class.getSimpleName());
			return null; // no class
		}

		// Obtain the team responsible for the administration
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
		Class<E> extensionInterfaceType = configuration.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(assetType, assetName,
					"Administrator " + adminName + " did not provide extension interface type");
			return null; // no extension interface
		}

		// Obtain the managed objects being administered
		List<RawBoundManagedObjectMetaData> administeredManagedObjects = new LinkedList<RawBoundManagedObjectMetaData>();
		List<ExtensionInterfaceMetaData<E>> eiMetaDatas = new LinkedList<ExtensionInterfaceMetaData<E>>();
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
			@SuppressWarnings("unchecked")
			ExtensionInterfaceFactory<E>[] extensionInterfaceFactories = new ExtensionInterfaceFactory[moInstances.length];
			boolean isExtensionInterfaceFactoryIssue = false;
			NEXT_INSTANCE: for (int i = 0; i < moInstances.length; i++) {
				RawBoundManagedObjectInstanceMetaData<?> moInstance = moInstances[i];

				// Obtain the extension factory for the managed object instance
				ExtensionInterfaceFactory<E> extensionInterfaceFactory = null;
				ManagedObjectExtensionInterfaceMetaData<?>[] moEiMetaDatas = moInstance.getRawManagedObjectMetaData()
						.getManagedObjectSourceMetaData().getExtensionInterfacesMetaData();
				if (moEiMetaDatas != null) {
					for (ManagedObjectExtensionInterfaceMetaData<?> moEiMetaData : moEiMetaDatas) {

						// Obtain the extension interface
						Class<?> moEiType = moEiMetaData.getExtensionInterfaceType();
						if ((moEiType != null) && (extensionInterfaceType.isAssignableFrom(moEiType))) {

							// Specify the extension interface factory
							extensionInterfaceFactory = (ExtensionInterfaceFactory<E>) moEiMetaData
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
			eiMetaDatas.add(new ExtensionInterfaceMetaDataImpl<E>(moIndex, extensionInterfaceFactories));
		}

		// TODO allow configuration of escalation procedure for administrator
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl();

		// Obtain the function node references
		ManagedFunctionReference[] functionReferences = configuration.getFlowConfiguration();
		if (functionReferences == null) {
			issues.addIssue(AssetType.ADMINISTRATOR, adminName, "Function references not provided");
			return null; // must have function references
		}

		// Obtain the flows
		FlowMetaData[] flows = new FlowMetaData[functionReferences.length];
		for (int i = 0; i < flows.length; i++) {
			ManagedFunctionReference functionReference = functionReferences[i];

			// Obtain the function meta-data for the flow
			ManagedFunctionMetaData<?, ?> functionMetaData = ConstructUtil.getFunctionMetaData(functionReference,
					functionLocator, issues, AssetType.ADMINISTRATOR, adminName, "Flow " + i);
			if (functionMetaData == null) {
				return null; // no function
			}

			// Create and register the flow
			flows[i] = ConstructUtil.newFlowMetaData(functionMetaData, false);
		}

		// Obtain the governance mapping
		AdministrationGovernanceConfiguration<?>[] dutyGovernanceConfigurations = configuration
				.getGovernanceConfiguration();
		int[] governanceMapping = new int[dutyGovernanceConfigurations.length];
		for (AdministrationGovernanceConfiguration<?> administrationGovernanceConfiguration : dutyGovernanceConfigurations) {

			// Index of governance for reference by administration
			int administrationGovernanceIndex = administrationGovernanceConfiguration.getIndex();

			// Obtain the governance
			String governanceName = administrationGovernanceConfiguration.getGovernanceName();
			int processGovernanceIndex = -1;
			for (int i = 0; i < governanceMetaDatas.length; i++) {
				GovernanceMetaData<?, ?> governanceMetaData = governanceMetaDatas[i];
				if (governanceName.equals(governanceMetaData.getGovernanceName())) {
					processGovernanceIndex = i; // found governance
				}
			}
			if (processGovernanceIndex < 0) {
				// Did not find the process governance
				issues.addIssue(AssetType.ADMINISTRATOR, adminName, "Can not find governance '" + governanceName + "'");
			}

			// Specify the governance mapping
			governanceMapping[administrationGovernanceIndex] = processGovernanceIndex;
		}

		// Create and return the administrator meta-data
		AdministrationMetaDataImpl<E, F, G> adminMetaData = new AdministrationMetaDataImpl<E, F, G>(adminName,
				adminFactory, ConstructUtil.toArray(eiMetaDatas, new ExtensionInterfaceMetaData[0]), responsibleTeam,
				flows, governanceMapping, escalationProcedure, functionLoop);
		return adminMetaData;
	}

}