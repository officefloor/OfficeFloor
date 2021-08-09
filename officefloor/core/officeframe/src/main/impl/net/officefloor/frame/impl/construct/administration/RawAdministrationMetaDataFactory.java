/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.construct.administration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.administration.AdministrationMetaDataImpl;
import net.officefloor.frame.impl.execute.administration.ManagedObjectExtensionExtractorMetaDataImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractorMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Factory to create the {@link RawAdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawAdministrationMetaDataFactory {

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link FlowMetaDataFactory}.
	 */
	private final FlowMetaDataFactory flowMetaDataFactory;

	/**
	 * {@link EscalationFlowFactory}.
	 */
	private final EscalationFlowFactory escalationFlowFactory;

	/**
	 * {@link TeamManagement} instances by their {@link Office} registered names.
	 */
	private final Map<String, TeamManagement> officeTeams;

	/**
	 * Instantiate.
	 * 
	 * @param officeMetaData        {@link OfficeMetaData}.
	 * @param flowMetaDataFactory   {@link FlowMetaDataFactory}.
	 * @param escalationFlowFactory {@link EscalationFlowFactory}.
	 * @param officeTeams           {@link TeamManagement} instances by their
	 *                              {@link Office} registered names.
	 */
	public RawAdministrationMetaDataFactory(OfficeMetaData officeMetaData, FlowMetaDataFactory flowMetaDataFactory,
			EscalationFlowFactory escalationFlowFactory, Map<String, TeamManagement> officeTeams) {
		this.officeMetaData = officeMetaData;
		this.flowMetaDataFactory = flowMetaDataFactory;
		this.escalationFlowFactory = escalationFlowFactory;
		this.officeTeams = officeTeams;
	}

	/**
	 * Creates the {@link RawAdministrationMetaData} instances.
	 * 
	 * @param administeredAssetName          Name of {@link Asset} adding this
	 *                                       {@link Administration}.
	 * @param administrationQualifier        {@link Administration} qualifier.
	 * @param configuration                  {@link AdministrationConfiguration}
	 *                                       instances.
	 * @param scopeMo                        {@link RawBoundManagedObjectMetaData}
	 *                                       by their scope names.
	 * @param assetType                      {@link AssetType} constructing
	 *                                       {@link Administration} instances.
	 * @param assetName                      Name of {@link Asset} constructing
	 *                                       {@link Administration} instances.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 * @return {@link RawAdministrationMetaData} instances.
	 */
	public RawAdministrationMetaData[] constructRawAdministrationMetaData(String administeredAssetName,
			String administrationQualifier, AdministrationConfiguration<?, ?, ?>[] configuration,
			Map<String, RawBoundManagedObjectMetaData> scopeMo, AssetType assetType, String assetName,
			AssetManagerRegistry assetManagerRegistry, long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Create the administrators
		RawAdministrationMetaData[] rawAdministrations = new RawAdministrationMetaData[configuration.length];
		for (int i = 0; i < rawAdministrations.length; i++) {
			AdministrationConfiguration<?, ?, ?> administrationConfiguration = configuration[i];

			// Construct the raw administrator
			RawAdministrationMetaData rawAdministration = this.constructRawAdministrationMetaData(administeredAssetName,
					administrationQualifier, administrationConfiguration, scopeMo, assetType, assetName,
					assetManagerRegistry, defaultAsynchronousFlowTimeout, issues);
			if (rawAdministration == null) {
				return null; // failed to create the administration
			}

			// Load the administration
			rawAdministrations[i] = rawAdministration;
		}

		// Return the raw administrations
		return rawAdministrations;
	}

	/**
	 * Provides typed construction of a {@link AdministrationMetaData}.
	 * 
	 * @param administeredAssetName          Name of {@link Asset} adding this
	 *                                       {@link Administration}.
	 * @param administrationQualifier        {@link Administration} qualifier.
	 * @param configuration                  {@link AdministrationConfiguration}
	 *                                       instances.
	 * @param scopeMo                        {@link RawBoundManagedObjectMetaData}
	 *                                       by their scope names.
	 * @param assetType                      {@link AssetType} constructing
	 *                                       {@link Administration} instances.
	 * @param assetName                      Name of {@link Asset} constructing
	 *                                       {@link Administration} instances.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 * @return Constructed {@link RawAdministrationMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private <E, F extends Enum<F>, G extends Enum<G>> RawAdministrationMetaData constructRawAdministrationMetaData(
			String administeredAssetName, String administrationQualifier,
			AdministrationConfiguration<E, F, G> configuration, Map<String, RawBoundManagedObjectMetaData> scopeMo,
			AssetType assetType, String assetName, AssetManagerRegistry assetManagerRegistry,
			long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Obtain the administration name
		String adminName = configuration.getAdministrationName();
		if (ConstructUtil.isBlank(adminName)) {
			issues.addIssue(assetType, assetName, "Administration added without a name");
			return null; // no name
		}

		// Obtain the administrator factory
		AdministrationFactory<E, F, G> adminFactory = configuration.getAdministrationFactory();
		if (adminFactory == null) {
			issues.addIssue(AssetType.ADMINISTRATOR, adminName, "Administration " + adminName + " did not provide an "
					+ AdministrationFactory.class.getSimpleName());
			return null; // no class
		}

		// Obtain the team responsible for the administration
		String teamName = configuration.getResponsibleTeamName();
		TeamManagement responsibleTeam = null; // any team
		if (!ConstructUtil.isBlank(teamName)) {
			responsibleTeam = this.officeTeams.get(teamName);
			if (responsibleTeam == null) {
				issues.addIssue(AssetType.ADMINISTRATOR, adminName,
						"Administration " + adminName + " team " + teamName + " can not be found");
				return null; // must have team
			}
		}

		// Obtain the extension type
		Class<E> extensionType = configuration.getExtensionType();
		if (extensionType == null) {
			issues.addIssue(AssetType.ADMINISTRATOR, adminName,
					"Administration " + adminName + " did not provide extension type");
			return null; // no extension type
		}

		// Obtain the managed objects being administered
		List<RawBoundManagedObjectMetaData> administeredManagedObjects = new LinkedList<>();
		List<ManagedObjectExtensionExtractorMetaData<E>> eiMetaDatas = new LinkedList<>();
		for (String moName : configuration.getAdministeredManagedObjectNames()) {

			// Ensure have managed object name
			if (ConstructUtil.isBlank(moName)) {
				issues.addIssue(AssetType.ADMINISTRATOR, adminName,
						"Administration " + adminName + " specifying no name for managed object");
				return null; // unspecified managed object name
			}

			// Obtain the managed object
			RawBoundManagedObjectMetaData mo = scopeMo.get(moName);
			if (mo == null) {
				issues.addIssue(AssetType.ADMINISTRATOR, adminName,
						"Managed Object " + moName + " not available to Administration " + adminName);
				return null; // unknown managed object
			}

			// Obtain the extension factories for the managed object instances.
			// (Keeping order of factories as order of managed object instances)
			RawBoundManagedObjectInstanceMetaData<?>[] moInstances = mo.getRawBoundManagedObjectInstanceMetaData();
			ExtensionFactory<E>[] extensionInterfaceFactories = new ExtensionFactory[moInstances.length];
			boolean isExtensionInterfaceFactoryIssue = false;
			NEXT_INSTANCE: for (int i = 0; i < moInstances.length; i++) {
				RawBoundManagedObjectInstanceMetaData<?> moInstance = moInstances[i];

				// Obtain the extension factory for the managed object instance
				ExtensionFactory<E> extensionInterfaceFactory = null;
				ManagedObjectExtensionMetaData<?>[] moEiMetaDatas = moInstance.getRawManagedObjectMetaData()
						.getManagedObjectSourceMetaData().getExtensionInterfacesMetaData();
				if (moEiMetaDatas != null) {
					for (ManagedObjectExtensionMetaData<?> moEiMetaData : moEiMetaDatas) {

						// Obtain the extension interface
						Class<?> moEiType = moEiMetaData.getExtensionType();
						if ((moEiType != null) && (extensionType.isAssignableFrom(moEiType))) {

							// Specify the extension interface factory
							extensionInterfaceFactory = (ExtensionFactory<E>) moEiMetaData.getExtensionFactory();
							if (extensionInterfaceFactory == null) {
								// Managed Object invalid
								isExtensionInterfaceFactoryIssue = true;
								issues.addIssue(AssetType.ADMINISTRATOR, adminName,
										"Managed Object did not provide " + ExtensionFactory.class.getSimpleName()
												+ " for Administration " + adminName + " (ManagedObjectSource="
												+ moInstance.getRawManagedObjectMetaData().getManagedObjectName()
												+ ")");
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
					issues.addIssue(AssetType.ADMINISTRATOR, adminName,
							"Managed Object " + moName + " does not support extension type " + extensionType.getName()
									+ " required by Administration " + adminName + " (ManagedObjectSource="
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
			eiMetaDatas.add(new ManagedObjectExtensionExtractorMetaDataImpl<E>(moIndex, extensionInterfaceFactories));
		}

		// Obtain the flows
		FlowMetaData[] flows = this.flowMetaDataFactory.createFlowMetaData(configuration.getFlowConfiguration(),
				AssetType.ADMINISTRATOR, adminName, issues);

		// Obtain the escalation procedure
		EscalationFlow[] escalationFlows = this.escalationFlowFactory
				.createEscalationFlows(configuration.getEscalations(), AssetType.ADMINISTRATOR, adminName, issues);
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(escalationFlows);

		// Obtain the governance meta-data
		GovernanceMetaData<?, ?>[] governanceMetaDatas = this.officeMetaData.getProcessMetaData().getThreadMetaData()
				.getGovernanceMetaData();

		// Obtain the governance mapping
		AdministrationGovernanceConfiguration<?>[] administrationGovernanceConfigurations = configuration
				.getGovernanceConfiguration();
		int[] governanceMapping = new int[administrationGovernanceConfigurations.length];
		for (AdministrationGovernanceConfiguration<?> administrationGovernanceConfiguration : administrationGovernanceConfigurations) {

			// Index of governance for reference by administration
			int administrationGovernanceIndex = administrationGovernanceConfiguration.getIndex();

			// Obtain the governance
			String governanceName = administrationGovernanceConfiguration.getGovernanceName();
			if (governanceName == null) {
				// Must have governance name
				issues.addIssue(AssetType.ADMINISTRATOR, adminName, "Governance linked without a name");
				return null;
			}
			int processGovernanceIndex = -1;
			for (int i = 0; i < governanceMetaDatas.length; i++) {
				GovernanceMetaData<?, ?> governanceMetaData = governanceMetaDatas[i];
				if (governanceName.equals(governanceMetaData.getGovernanceName())) {
					processGovernanceIndex = i; // found governance
				}
			}
			if (processGovernanceIndex < 0) {
				// Did not find the process governance
				issues.addIssue(AssetType.ADMINISTRATOR, adminName, "Can not find governance " + governanceName);
				return null;
			}

			// Specify the governance mapping
			governanceMapping[administrationGovernanceIndex] = processGovernanceIndex;
		}

		// Obtain the asynchronous flow timeout
		long asynchronousFlowTimeout = configuration.getAsynchronousFlowTimeout();
		if (asynchronousFlowTimeout <= 0) {
			asynchronousFlowTimeout = defaultAsynchronousFlowTimeout;
		}

		// Create the asynchronous flow asset manager
		AssetManagerReference asynchronousFlowAssetManagerReference = assetManagerRegistry.createAssetManager(
				AssetType.ADMINISTRATOR, adminName,
				Administration.class.getSimpleName() + administrationQualifier + "-" + administeredAssetName, issues);

		// Create the administrator meta-data
		AdministrationMetaDataImpl<E, F, G> adminMetaData = new AdministrationMetaDataImpl<E, F, G>(adminName,
				adminFactory, extensionType,
				ConstructUtil.toArray(eiMetaDatas, new ManagedObjectExtensionExtractorMetaData[0]), responsibleTeam,
				asynchronousFlowTimeout, asynchronousFlowAssetManagerReference, flows, governanceMapping,
				escalationProcedure, this.officeMetaData);

		// Create the listing of administered managed objects
		RawBoundManagedObjectMetaData[] rawBoundAdministeredManagedObjects = administeredManagedObjects
				.toArray(new RawBoundManagedObjectMetaData[0]);

		// Return the raw administration meta-data
		return new RawAdministrationMetaData(rawBoundAdministeredManagedObjects, adminMetaData);
	}

}
