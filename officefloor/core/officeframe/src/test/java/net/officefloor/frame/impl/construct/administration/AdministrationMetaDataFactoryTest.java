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
package net.officefloor.frame.impl.construct.administration;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.administration.AdministrationMetaDataFactoryImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.AdministrationMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AdministrationMetaDataFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationMetaDataFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link AdministrationConfiguration}.
	 */
	private final AdministrationConfiguration<?, ?, ?> configuration = this
			.createMock(AdministrationConfiguration.class);

	/**
	 * {@link AdministrationFactory}.
	 */
	private final AdministrationFactory<?, ?, ?> administrationFactory = this.createMock(AdministrationFactory.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * {@link AssetType}.
	 */
	private final AssetType assetType = AssetType.OFFICE;

	/**
	 * Name of the {@link Asset}.
	 */
	private final String assetName = "OFFICE";

	/**
	 * {@link TeamManagement} instances by their {@link Office} registered
	 * names.
	 */
	private final Map<String, TeamManagement> officeTeams = new HashMap<String, TeamManagement>();

	/**
	 * Responsible {@link TeamManagement}.
	 */
	private final TeamManagement responsibleTeam = this.createMock(TeamManagement.class);

	/**
	 * Scope bound {@link RawBoundManagedObjectMetaData} instances by their
	 * scope name.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> scopeMo = new HashMap<String, RawBoundManagedObjectMetaData>();

	/**
	 * {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData rawBoundMoMetaData = this
			.createMock(RawBoundManagedObjectMetaData.class);

	/**
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	private final RawBoundManagedObjectInstanceMetaData<?> rawBoundMoInstanceMetaData = this
			.createMock(RawBoundManagedObjectInstanceMetaData.class);

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	private final RawManagedObjectMetaData<?, ?> rawMoMetaData = this.createMock(RawManagedObjectMetaData.class);

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private final ManagedObjectSourceMetaData<?, ?> managedObjectSourceMetaData = this
			.createMock(ManagedObjectSourceMetaData.class);

	/**
	 * {@link ManagedObjectExtensionInterfaceMetaData}.
	 */
	private final ManagedObjectExtensionInterfaceMetaData<?> extensionInterfaceMetaData = this
			.createMock(ManagedObjectExtensionInterfaceMetaData.class);

	/**
	 * {@link ManagedObjectIndex}.
	 */
	private final ManagedObjectIndex managedObjectIndex = this.createMock(ManagedObjectIndex.class);

	/**
	 * {@link ExtensionInterfaceFactory}.
	 */
	private final ExtensionInterfaceFactory<?> extensionInterfaceFactory = this
			.createMock(ExtensionInterfaceFactory.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this.createMock(OfficeMetaData.class);

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator functionLocator = this.createMock(ManagedFunctionLocator.class);

	/**
	 * Ensures issue if not {@link Administration} name.
	 */
	public void testNoAdministrationName() {

		// Record no name
		this.recordReturn(this.configuration, this.configuration.getAdministrationName(), "");
		this.issues.addIssue(this.assetType, this.assetName, "Administration added without a name");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link AdministrationFactory}.
	 */
	public void testNoAdministrationFactory() {

		// Record no administration factory
		this.recordReturn(this.configuration, this.configuration.getAdministrationName(), "ADMIN");
		this.recordReturn(this.configuration, this.configuration.getAdministrationFactory(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"Administration 'ADMIN' did not provide an AdministrationFactory");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures any {@link Team} responsible.
	 */
	public void testAnyTeam() {

		// Record any team
		this.record_init();
		this.recordReturn(this.configuration, this.configuration.getOfficeTeamName(), null);
		this.record_managedObject();
		this.record_noFlows();
		this.record_governanceMetaData(0);
		this.record_noGovernance();

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(1, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if unknown {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record no team
		this.record_init();
		this.recordReturn(this.configuration, this.configuration.getOfficeTeamName(), "TEAM");
		this.issues.addIssue(this.assetType, this.assetName, "Administration ADMIN team 'TEAM' can not be found");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no extension interface.
	 */
	public void testNoExtensionInterface() {

		// Record no extension interface
		this.record_init();
		this.record_team();
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"Administration ADMIN did not provide extension interface type");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedObject} name.
	 */
	public void testNoManagedObjectName() {

		// Record no managed object name
		this.record_init();
		this.record_team();
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), Object.class);
		this.recordReturn(this.configuration, this.configuration.getAdministeredManagedObjectNames(),
				new String[] { null });
		this.issues.addIssue(this.assetType, this.assetName,
				"Administration ADMIN specifying no name for managed object");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link RawBoundManagedObjectMetaData}.
	 */
	public void testNoBoundManagedObject() {

		// Record no bound managed object
		this.record_init();
		this.record_team();
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), Object.class);
		this.recordReturn(this.configuration, this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
		this.issues.addIssue(this.assetType, this.assetName,
				"Managed Object 'MO' not available to Administration ADMIN");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if {@link RawBoundManagedObjectMetaData} does not have
	 * extension interfaces.
	 */
	public void testBoundManagedObjectHasNoExtensionInterfaces() {

		// Record managed object without extension interfaces
		this.record_init();
		this.record_team();
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), Object.class);
		this.recordReturn(this.configuration, this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { this.rawBoundMoInstanceMetaData });
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getExtensionInterfacesMetaData(), null);
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData.getManagedObjectName(), "MANAGED_OBJECT_SOURCE");
		this.issues.addIssue(this.assetType, this.assetName,
				"Managed Object 'MO' does not support extension interface " + Object.class.getName()
						+ " required by Administration ADMIN (ManagedObjectSource=MANAGED_OBJECT_SOURCE)");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if {@link RawBoundManagedObjectMetaData} does not support
	 * extension interface.
	 */
	public void testBoundManagedObjectNotSupportExtensionInterface() {

		// Record managed object not support extension interface
		this.record_init();
		this.record_team();
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { this.rawBoundMoInstanceMetaData });
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { this.extensionInterfaceMetaData });
		this.recordReturn(this.extensionInterfaceMetaData, this.extensionInterfaceMetaData.getExtensionInterfaceType(),
				Integer.class);
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData.getManagedObjectName(), "MANAGED_OBJECT_SOURCE");
		this.issues.addIssue(this.assetType, this.assetName,
				"Managed Object 'MO' does not support extension interface " + String.class.getName()
						+ " required by Administration ADMIN (ManagedObjectSource=MANAGED_OBJECT_SOURCE)");

		// Construct the administrators
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link AdministrationMetaData}.
	 */
	public void testConstructAdministratorMetaData() {

		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final ManagedObjectMetaData<?> moMetaData = this.createMock(ManagedObjectMetaData.class);
		final Object extensionInterface = "EXTENSION_INTERFACE";

		// Record successfully create bound administrator
		this.record_init();
		this.record_team();
		this.record_managedObject();

		// Record ei extraction to verify the extension interface factory
		this.recordReturn(moMetaData, moMetaData.getInstanceIndex(), 0);
		this.recordReturn(this.extensionInterfaceFactory,
				this.extensionInterfaceFactory.createExtensionInterface(managedObject), extensionInterface);

		// No flows or governance
		this.record_noFlows();
		this.record_governanceMetaData(0);
		this.record_noGovernance();

		// Test
		this.replayMockObjects();

		// Construct the administrators
		AdministrationMetaData<?, ?, ?>[] adminMetaDatas = this.constructAdministration(1, this.configuration);
		AdministrationMetaData<?, ?, ?> adminMetaData = adminMetaDatas[0];

		// Verify extension interface factory by extracting ei
		assertEquals("Incorrect number of administered managed objects", 1,
				adminMetaData.getExtensionInterfaceMetaData().length);
		ExtensionInterfaceMetaData<?> moEiMetaData = adminMetaData.getExtensionInterfaceMetaData()[0];
		assertEquals("Incorrect extracted extension interface", extensionInterface,
				moEiMetaData.getExtensionInterfaceExtractor().extractExtensionInterface(managedObject, moMetaData));

		// Verify mocks
		this.verifyMockObjects();

		// Verify administration meta-data
		assertEquals("Incorrect name", "ADMIN", adminMetaData.getAdministrationName());
		assertNotNull("Must have admin factory", adminMetaData.getAdministrationFactory());
		assertEquals("Incorrect responsible team", this.responsibleTeam, adminMetaData.getResponsibleTeam());
		assertEquals("Incorrect administered managed object", this.managedObjectIndex,
				moEiMetaData.getManagedObjectIndex());
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionReference} instances for
	 * {@link Administration}.
	 */
	public void testNoFunctionReferences() {

		// Record construction of administration meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), null);
		this.record_issue("ManagedFunction references not provided");

		// Construct the administration and link functions
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} is found for the
	 * {@link Administration} {@link Flow}.
	 */
	public void testNoFunctionForFlow() {

		final ManagedFunctionReference functionReference = this.createMock(ManagedFunctionReference.class);

		// Record construction of administration meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new ManagedFunctionReference[] { functionReference });
		this.recordReturn(functionReference, functionReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), null);
		this.record_issue("Can not find function meta-data TASK for Flow 0");

		// Construct the administration and link functions
		this.replayMockObjects();
		this.constructAdministration(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link FlowMetaData} to {@link DutyMetaData}.
	 */
	public void testLinkFlows() {

		final ManagedFunctionReference functionReference = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> functionMetaData = this.createMock(ManagedFunctionMetaData.class);

		// Record construction of administration meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();

		// Record linking tasks
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new ManagedFunctionReference[] { functionReference });
		this.recordReturn(functionReference, functionReference.getFunctionName(), "TASK");
		this.recordReturn(functionReference, functionReference.getArgumentType(), Connection.class);
		this.recordReturn(functionMetaData, functionMetaData.getParameterType(), Connection.class);
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"),
				functionMetaData);
		this.record_governanceMetaData(0);
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new AdministrationGovernanceConfiguration[0]);

		// Construct the administration
		this.replayMockObjects();
		AdministrationMetaData<?, ?, ?>[] adminMetaDatas = this.constructAdministration(1, this.configuration);
		AdministrationMetaData<?, ?, ?> adminMetaData = adminMetaDatas[0];
		this.verifyMockObjects();

		// Verify the flow
		FlowMetaData flow = adminMetaData.getFlow(0);
		assertFalse("Should not spawn thread state", flow.isSpawnThreadState());
		assertEquals("Incorrect task meta-data", functionMetaData, flow.getInitialFunctionMetaData());
	}

	/**
	 * Ensure issue if unknown {@link Governance} linked to
	 * {@link Administration}.
	 */
	public void testLinkUnkownGovernance() {

		final int GOVERNANCE_INDEX = 0;
		final AdministrationGovernanceConfiguration<?> governanceConfiguration = this
				.createMock(AdministrationGovernanceConfiguration.class);

		// Record construction of administration meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.record_noFlows();

		// Record governance configuration
		GovernanceMetaData<?, ?>[] governanceMetaData = this.record_governanceMetaData(1);
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new AdministrationGovernanceConfiguration<?>[] { governanceConfiguration });
		this.recordReturn(governanceConfiguration, governanceConfiguration.getIndex(), GOVERNANCE_INDEX);
		this.recordReturn(governanceConfiguration, governanceConfiguration.getGovernanceName(), "GOVERNANCE");

		// Record unknown governance
		this.recordReturn(governanceMetaData[0], governanceMetaData[0].getGovernanceName(), "NOT MATCH");
		this.record_issue("Can not find governance 'GOVERNANCE'");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		AdministrationMetaData<?, ?, ?>[] adminMetaDatas = this.constructAdministration(1, this.configuration);
		AdministrationMetaData<?, ?, ?> adminMetaData = adminMetaDatas[0];
		this.verifyMockObjects();

		// Verify the governance not mapped
		int threadIndex = adminMetaData.translateGovernanceIndexToThreadIndex(GOVERNANCE_INDEX);
		assertEquals("Thread index should not be translated", -1, threadIndex);
	}

	/**
	 * Ensure can link {@link Governance}.
	 */
	public void testLinkGovernance() {

		final int GOVERNANCE_INDEX = 0;
		final AdministrationGovernanceConfiguration<?> governanceConfiguration = this
				.createMock(AdministrationGovernanceConfiguration.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.record_noFlows();
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new AdministrationGovernanceConfiguration<?>[] { governanceConfiguration });
		this.recordReturn(governanceConfiguration, governanceConfiguration.getIndex(), GOVERNANCE_INDEX);
		this.recordReturn(governanceConfiguration, governanceConfiguration.getGovernanceName(), "GOVERNANCE");

		// Record configuring links
		GovernanceMetaData<?, ?>[] governanceMetaData = this.record_governanceMetaData(2);
		this.recordReturn(governanceMetaData[0], governanceMetaData[0].getGovernanceName(), "NOT MATCH");
		this.recordReturn(governanceMetaData[1], governanceMetaData[1].getGovernanceName(), "GOVERNANCE");

		// Construct the administration
		this.replayMockObjects();
		AdministrationMetaData<?, ?, ?>[] adminMetaDatas = this.constructAdministration(1, this.configuration);
		AdministrationMetaData<?, ?, ?> adminMetaData = adminMetaDatas[0];
		this.verifyMockObjects();

		// Verify the governance
		int threadIndex = adminMetaData.translateGovernanceIndexToThreadIndex(GOVERNANCE_INDEX);
		assertEquals("Incorrect thread index", 1, threadIndex);
	}

	/**
	 * Records initiating the {@link Administration}.
	 */
	private void record_init() {
		this.recordReturn(this.configuration, this.configuration.getAdministrationName(), "ADMIN");
		this.recordReturn(this.configuration, this.configuration.getAdministrationFactory(),
				this.administrationFactory);
	}

	/**
	 * Records obtaining the {@link Team}.
	 */
	private void record_team() {
		final String TEAM_NAME = "TEAM";
		this.recordReturn(this.configuration, this.configuration.getOfficeTeamName(), TEAM_NAME);
		this.officeTeams.put(TEAM_NAME, this.responsibleTeam);
	}

	/**
	 * Records the administering of {@link ManagedObject} instances.
	 */
	private void record_managedObject() {
		this.recordReturn(this.configuration, this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { this.rawBoundMoInstanceMetaData });
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { this.extensionInterfaceMetaData });
		this.recordReturn(this.extensionInterfaceMetaData, this.extensionInterfaceMetaData.getExtensionInterfaceType(),
				String.class);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData.getManagedObjectIndex(),
				this.managedObjectIndex);
		this.recordReturn(this.extensionInterfaceMetaData,
				this.extensionInterfaceMetaData.getExtensionInterfaceFactory(), this.extensionInterfaceFactory);
	}

	/**
	 * Records no flows.
	 */
	private void record_noFlows() {
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new ManagedFunctionReference[0]);
	}

	/**
	 * Records obtaining the {@link GovernanceMetaData}.
	 * 
	 * @param governanceCount
	 *            Number of {@link GovernanceMetaData}.
	 * @return Mock {@link GovernanceMetaData}.
	 */
	private GovernanceMetaData<?, ?>[] record_governanceMetaData(int governanceCount) {
		ProcessMetaData processMetaData = this.createMock(ProcessMetaData.class);
		ThreadMetaData threadMetaData = this.createMock(ThreadMetaData.class);
		GovernanceMetaData<?, ?>[] governanceMetaData = new GovernanceMetaData<?, ?>[governanceCount];
		for (int i = 0; i < governanceCount; i++) {
			governanceMetaData[i] = this.createMock(GovernanceMetaData.class);
		}
		this.recordReturn(this.officeMetaData, this.officeMetaData.getProcessMetaData(), processMetaData);
		this.recordReturn(processMetaData, processMetaData.getThreadMetaData(), threadMetaData);
		this.recordReturn(threadMetaData, threadMetaData.getGovernanceMetaData(), governanceMetaData);
		return governanceMetaData;
	}

	/**
	 * Records no {@link Governance}.
	 */
	private void record_noGovernance() {
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new AdministrationGovernanceConfiguration[0]);
	}

	/**
	 * Records an issue regarding the {@link Administration}.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.ADMINISTRATOR, "ADMIN", issueDescription);
	}

	/**
	 * Constructs the {@link AdministrationMetaData}.
	 * 
	 * @param expectedCreateCount
	 *            Expected number to be created.
	 * @param configuration
	 *            {@link AdministrationConfiguration} instances.
	 * @return {@link AdministrationMetaData}.
	 */
	private AdministrationMetaData<?, ?, ?>[] constructAdministration(int expectedCreateCount,
			AdministrationConfiguration<?, ?, ?>... configuration) {

		// Construct the meta-data
		AdministrationMetaData<?, ?, ?>[] metaData = AdministrationMetaDataFactoryImpl.getFactory()
				.constructAdministrationMetaData(configuration, this.assetType, this.assetName, this.officeMetaData,
						this.officeTeams, this.scopeMo, this.issues);

		// Ensure correct number created
		assertEquals("Incorrect number of created meta-data", expectedCreateCount, metaData.length);

		// Return the meta-data
		return metaData;
	}

}
