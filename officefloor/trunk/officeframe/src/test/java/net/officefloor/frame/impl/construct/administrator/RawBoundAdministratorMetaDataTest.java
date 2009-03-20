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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawBoundAdministratorMetaDataImpl}.
 * 
 * @author Daniel
 */
public class RawBoundAdministratorMetaDataTest extends OfficeFrameTestCase {

	/**
	 * {@link AdministratorSourceConfiguration}.
	 */
	private final AdministratorSourceConfiguration<?, ?> configuration = this
			.createMock(AdministratorSourceConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link AdministratorScope}.
	 */
	private final AdministratorScope administratorScope = AdministratorScope.PROCESS;

	/**
	 * {@link AssetType}.
	 */
	private final AssetType assetType = AssetType.OFFICE;

	/**
	 * Name of the {@link Asset}.
	 */
	private final String assetName = "OFFICE";

	/**
	 * {@link Team} instances by their {@link Office} registered names.
	 */
	private final Map<String, Team> officeTeams = new HashMap<String, Team>();

	/**
	 * {@link Team}.
	 */
	private final Team team = this.createMock(Team.class);

	/**
	 * Scope bound {@link RawBoundManagedObjectMetaData} instances by their
	 * scope name.
	 */
	private final Map<String, RawBoundManagedObjectMetaData<?>> scopeMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();

	/**
	 * {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData<?> rawBoundMoMetaData = this
			.createMock(RawBoundManagedObjectMetaData.class);

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	private final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
			.createMock(RawManagedObjectMetaData.class);

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
	private final ManagedObjectIndex managedObjectIndex = this
			.createMock(ManagedObjectIndex.class);

	/**
	 * {@link ExtensionInterfaceFactory}.
	 */
	private final ExtensionInterfaceFactory<?> extensionInterfaceFactory = this
			.createMock(ExtensionInterfaceFactory.class);

	/**
	 * {@link AdministratorSourceMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final AdministratorSourceMetaData<Object, DutyKey> metaData = this
			.createMock(AdministratorSourceMetaData.class);

	/**
	 * {@link OfficeMetaDataLocator}.
	 */
	private final OfficeMetaDataLocator taskMetaDataLocator = this
			.createMock(OfficeMetaDataLocator.class);

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

	/**
	 * {@link DutyConfiguration} for {@link DutyKey#ONE}.
	 */
	private final DutyConfiguration<?> dutyOneConfiguration = this
			.createMock(DutyConfiguration.class);

	/**
	 * {@link DutyConfiguration} for {@link DutyKey#TWO}.
	 */
	private final DutyConfiguration<?> dutyTwoConfiguration = this
			.createMock(DutyConfiguration.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Reset the state of the mock administrator source
		MockAdministratorSource.reset(this.metaData);
	}

	/**
	 * Ensures issue if not {@link Administrator} name.
	 */
	public void testNoAdministratorName() {

		// Record no name
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorName(), "");
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator added without a name");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link AdministratorSource}.
	 */
	public void testNoAdministratorSource() {

		// Record no administrator source
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorName(), "ADMIN");
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorSourceClass(), null);
		this.issues
				.addIssue(this.assetType, this.assetName,
						"Administrator 'ADMIN' did not provide an AdministratorSource class");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if class not {@link AdministratorSource}.
	 */
	public void testClassNotAdministratorSource() {

		// Record no administrator source
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorName(), "ADMIN");
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorSourceClass(), Object.class);
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator 'ADMIN' class must implement AdministratorSource (class="
						+ Object.class.getName() + ")");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fail to initialise {@link AdministratorSource}.
	 */
	public void testFailInitAdministratorSource() {

		Exception failure = new Exception("Init failure");

		// Record fail initialise administrator source
		this.record_init();
		this.issues.addIssue(this.assetType, this.assetName,
				"Failed to initialise Administrator ADMIN", failure);

		// Construct the administrators
		this.replayMockObjects();
		MockAdministratorSource.initFailure = failure;
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link AdministratorSourceMetaData}.
	 */
	public void testNoAdministratorSourceMetaData() {

		// Record no administrator source meta-data
		this.record_init();
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator ADMIN must provide AdministratorSourceMetaData");

		// Construct the administrators
		this.replayMockObjects();
		MockAdministratorSource.metaData = null;
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no name of {@link Team} responsible.
	 */
	public void testNoTeamName() {

		// Record no team
		this.record_init();
		this.recordReturn(this.configuration, this.configuration
				.getOfficeTeamName(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator ADMIN must specify team responsible for duties");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if unknown {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record no team
		this.record_init();
		this.recordReturn(this.configuration, this.configuration
				.getOfficeTeamName(), "TEAM");
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator ADMIN team 'TEAM' can not be found");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no extension interface.
	 */
	public void testNoExtensionInterface() {

		// Record no extension interface
		this.record_init();
		this.record_team();
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				null);
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator ADMIN did not provide extension interface type");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedObject} name.
	 */
	public void testNoManagedObjectName() {

		// Record no managed object name
		this.record_init();
		this.record_team();
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				Object.class);
		this.recordReturn(this.configuration, this.configuration
				.getAdministeredManagedObjectNames(), new String[] { null });
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator ADMIN specifying no name for managed object");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link RawBoundManagedObjectMetaData}.
	 */
	public void testNoBoundManagedObject() {

		// Record no bound managed object
		this.record_init();
		this.record_team();
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				Object.class);
		this.recordReturn(this.configuration, this.configuration
				.getAdministeredManagedObjectNames(), new String[] { "MO" });
		this.issues.addIssue(this.assetType, this.assetName,
				"Managed Object 'MO' not available to Administrator ADMIN");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
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
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				Object.class);
		this.recordReturn(this.configuration, this.configuration
				.getAdministeredManagedObjectNames(), new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData
				.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData
						.getExtensionInterfacesMetaData(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"Managed Object 'MO' does not support extension interface "
						+ Object.class.getName()
						+ " required by Administrator ADMIN");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
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
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				String.class);
		this.recordReturn(this.configuration, this.configuration
				.getAdministeredManagedObjectNames(), new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData
				.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this
				.recordReturn(
						this.managedObjectSourceMetaData,
						this.managedObjectSourceMetaData
								.getExtensionInterfacesMetaData(),
						new ManagedObjectExtensionInterfaceMetaData[] { this.extensionInterfaceMetaData });
		this.recordReturn(this.extensionInterfaceMetaData,
				this.extensionInterfaceMetaData.getExtensionInterfaceType(),
				Integer.class);
		this.issues.addIssue(this.assetType, this.assetName,
				"Managed Object 'MO' does not support extension interface "
						+ String.class.getName()
						+ " required by Administrator ADMIN");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link Duty} instances.
	 */
	public void testNoDuties() {

		// Record no duties
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator ADMIN does not provide duties");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link RawBoundAdministratorMetaData}.
	 */
	public void testConstructRawBoundAdministratorMetaData() {

		// Record successfully create bound administrator
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), DutyKey.class);

		// Construct the administrators
		this.replayMockObjects();
		RawBoundAdministratorMetaData<?, ?>[] rawAdminMetaDatas = this
				.constructRawAdministrator(1, this.configuration);
		RawBoundAdministratorMetaData<?, ?> rawAdminMetaData = rawAdminMetaDatas[0];
		AdministratorMetaData<?, ?> adminMetaData = rawAdminMetaData
				.getAdministratorMetaData();
		this.verifyMockObjects();

		// Verify bound administrator meta-data
		assertEquals("Incorrect name", "ADMIN", rawAdminMetaData
				.getBoundAdministratorName());
		assertEquals("Incorrect scope", this.administratorScope,
				rawAdminMetaData.getAdministratorIndex()
						.getAdministratorScope());
		assertEquals("Incorrect bound index", 0, rawAdminMetaData
				.getAdministratorIndex().getIndexOfAdministratorWithinScope());
		assertEquals("Incorrect first duty key", DutyKey.ONE, rawAdminMetaData
				.getDutyKeys()[DutyKey.ONE.ordinal()]);
		assertEquals("Incorrect second duty key", DutyKey.TWO, rawAdminMetaData
				.getDutyKeys()[DutyKey.TWO.ordinal()]);

		// Verify administrator meta-data
		assertNotNull("Must have admin source", adminMetaData
				.getAdministratorSource());
		assertEquals("Incorrect team", this.team, adminMetaData.getTeam());
		assertEquals("Incorrect number of administered managed objects", 1,
				adminMetaData.getExtensionInterfaceMetaData().length);
		ExtensionInterfaceMetaData<?> moEiMetaData = adminMetaData
				.getExtensionInterfaceMetaData()[0];
		assertEquals("Incorrect administered managed object",
				this.managedObjectIndex, moEiMetaData.getManagedObjectIndex());
		assertEquals("Incorrect extension interface factory",
				this.extensionInterfaceFactory, moEiMetaData
						.getExtensionInterfaceFactory());

		// Verify the administrator meta-data
		assertNotNull("Must have admin source", adminMetaData
				.getAdministratorSource());
	}

	/**
	 * Ensure issue if no {@link Duty} key is provided by the
	 * {@link DutyConfiguration}.
	 */
	public void testNoConfiguredDutyKey() {

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), DutyKey.class);

		// Record linking tasks
		this.recordReturn(this.configuration, this.configuration
				.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getDutyKey(), null);
		this.record_issue("Duty key not provided by duty configuration");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkTasks();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if {@link Duty} key configured is of the wrong type.
	 */
	public void testIncorrectDutyKeyType() {

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), DutyKey.class);

		// Record linking tasks
		this.recordReturn(this.configuration, this.configuration
				.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getDutyKey(), WrongDutyKeyType.WRONG_DUTY_KEY_TYPE);
		this.record_issue("Duty key " + WrongDutyKeyType.WRONG_DUTY_KEY_TYPE
				+ " is not of correct type (" + DutyKey.class.getName() + ")");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkTasks();
		this.verifyMockObjects();
	}

	/**
	 * {@link Duty} of the wrong type.
	 */
	private enum WrongDutyKeyType {
		WRONG_DUTY_KEY_TYPE
	}

	/**
	 * Ensure issue if no {@link TaskNodeReference} instances for {@link Duty}.
	 */
	public void testNoTaskReferencesForDuty() {

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), DutyKey.class);

		// Record linking tasks
		this.recordReturn(this.configuration, this.configuration
				.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getDutyKey(), DutyKey.ONE);
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getLinkedProcessConfiguration(), null);
		this.record_issue("Task references not provided for duty "
				+ DutyKey.ONE);

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkTasks();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link DutyConfiguration} for a {@link Duty}.
	 */
	public void testNoDutyConfiguration() {

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), DutyKey.class);

		// Record linking tasks
		this.recordReturn(this.configuration, this.configuration
				.getDutyConfiguration(), new DutyConfiguration[0]);
		this.record_issue("No configuration provided for duty " + DutyKey.ONE);

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkTasks();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Task} is found for the {@link Duty}
	 * {@link Flow}.
	 */
	public void testNoTaskForDutyFlow() {

		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), DutyKey.class);

		// Record linking tasks
		this.recordReturn(this.configuration, this.configuration
				.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getDutyKey(), DutyKey.ONE);
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getLinkedProcessConfiguration(),
				new TaskNodeReference[] { taskReference });
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getTaskMetaData("WORK", "TASK"), null);
		this
				.record_issue("Can not find task meta-data (work=WORK, task=TASK) for Duty "
						+ DutyKey.ONE + " Flow 0");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkTasks();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link FlowMetaData} to {@link DutyMetaData}.
	 */
	public void testLinkFlowsToDuty() {

		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData, this.metaData
				.getAministratorDutyKeys(), DutyKey.class);

		// Record linking tasks
		this.recordReturn(this.configuration, this.configuration
				.getDutyConfiguration(), new DutyConfiguration[] {
				this.dutyOneConfiguration, this.dutyTwoConfiguration });
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getDutyKey(), DutyKey.ONE);
		this.recordReturn(this.dutyOneConfiguration, this.dutyOneConfiguration
				.getLinkedProcessConfiguration(),
				new TaskNodeReference[] { taskReference });
		this.recordReturn(this.dutyTwoConfiguration, this.dutyTwoConfiguration
				.getDutyKey(), DutyKey.TWO);
		this.recordReturn(this.dutyTwoConfiguration, this.dutyTwoConfiguration
				.getLinkedProcessConfiguration(), new TaskNodeReference[0]);
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(taskReference, taskReference.getArgumentType(),
				Connection.class);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				Connection.class);
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getTaskMetaData("WORK", "TASK"), taskMetaData);

		// Construct the administrator and link tasks
		this.replayMockObjects();
		RawBoundAdministratorMetaData<?, DutyKey> rawAdminMetaData = this
				.constructRawAdministratorAndLinkTasks();
		AdministratorMetaData<?, DutyKey> adminMetaData = rawAdminMetaData
				.getAdministratorMetaData();
		this.verifyMockObjects();

		// Verify the duties
		DutyMetaData dutyOne = adminMetaData.getDutyMetaData(DutyKey.ONE);
		assertNotNull("Must have first duty", dutyOne);
		assertNotNull("Must have second duty", adminMetaData
				.getDutyMetaData(DutyKey.TWO));

		// Verify the flow
		FlowMetaData<?> flow = dutyOne.getFlow(0);
		assertEquals("Incorrect instigation strategy",
				FlowInstigationStrategyEnum.PARALLEL, flow
						.getInstigationStrategy());
		assertEquals("Incorrect task meta-data", taskMetaData, flow
				.getInitialTaskMetaData());
		assertNull(
				"Parallel instigation so should not have flow asset manager",
				flow.getFlowManager());
	}

	/**
	 * Records initiating the {@link AdministratorSource}.
	 */
	private void record_init() {
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorName(), "ADMIN");
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorSourceClass(), MockAdministratorSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new Properties());
	}

	/**
	 * Records obtaining the {@link Team}.
	 */
	private void record_team() {
		final String TEAM_NAME = "TEAM";
		this.recordReturn(this.configuration, this.configuration
				.getOfficeTeamName(), TEAM_NAME);
		this.officeTeams.put(TEAM_NAME, this.team);
	}

	/**
	 * Records the administering of {@link ManagedObject} instances.
	 */
	private void record_managedObject() {
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				String.class);
		this.recordReturn(this.configuration, this.configuration
				.getAdministeredManagedObjectNames(), new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData
				.getRawManagedObjectMetaData(), this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData
				.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this
				.recordReturn(
						this.managedObjectSourceMetaData,
						this.managedObjectSourceMetaData
								.getExtensionInterfacesMetaData(),
						new ManagedObjectExtensionInterfaceMetaData[] { this.extensionInterfaceMetaData });
		this.recordReturn(this.extensionInterfaceMetaData,
				this.extensionInterfaceMetaData.getExtensionInterfaceType(),
				String.class);
		this.recordReturn(this.rawBoundMoMetaData, this.rawBoundMoMetaData
				.getManagedObjectIndex(), this.managedObjectIndex);
		this.recordReturn(this.extensionInterfaceMetaData,
				this.extensionInterfaceMetaData.getExtensionInterfaceFactory(),
				this.extensionInterfaceFactory);
	}

	/**
	 * Records an issue regarding the {@link Administrator}.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues
				.addIssue(AssetType.ADMINISTRATOR, "ADMIN", issueDescription);
	}

	/**
	 * Duty keys.
	 */
	private static enum DutyKey {
		ONE, TWO
	}

	/**
	 * Mock {@link AdministratorSource}.
	 */
	public static class MockAdministratorSource implements
			AdministratorSource<Object, DutyKey> {

		/**
		 * Property name.
		 */
		public static String propertyName = null;

		/**
		 * Initialise {@link Exception}.
		 */
		public static Exception initFailure = null;

		/**
		 * {@link AdministratorSourceMetaData}.
		 */
		public static AdministratorSourceMetaData<Object, DutyKey> metaData;

		/**
		 * Resets the state for next test.
		 * 
		 * @param metaData
		 *            {@link AdministratorSourceMetaData}.
		 */
		public static void reset(
				AdministratorSourceMetaData<Object, DutyKey> metaData) {
			propertyName = null;
			initFailure = null;
			MockAdministratorSource.metaData = metaData;
		}

		/*
		 * =========== AdministratorSource ==========================
		 */

		@Override
		public AdministratorSourceSpecification getSpecification() {
			fail("Should not be invoked");
			return null;
		}

		@Override
		public void init(AdministratorSourceContext context) throws Exception {

			// Throw initialise failure
			if (initFailure != null) {
				throw initFailure;
			}
		}

		@Override
		public AdministratorSourceMetaData<Object, DutyKey> getMetaData() {
			return metaData;
		}

		@Override
		public Administrator<Object, DutyKey> createAdministrator() {
			fail("Should not be invoked");
			return null;
		}
	}

	/**
	 * Constructs the {@link RawBoundAdministratorMetaDataImpl}.
	 * 
	 * @param expectedCreateCount
	 *            Expected number to be created.
	 * @param configuration
	 *            {@link AdministratorSourceConfiguration} instances.
	 * @return {@link RawBoundAdministratorMetaData}.
	 */
	private RawBoundAdministratorMetaData<?, ?>[] constructRawAdministrator(
			int expectedCreateCount,
			AdministratorSourceConfiguration<?, ?>... configuration) {

		// Construct the meta-data
		RawBoundAdministratorMetaData<?, ?>[] metaData = RawBoundAdministratorMetaDataImpl
				.getFactory().constructRawBoundAdministratorMetaData(
						configuration, this.issues, this.administratorScope,
						this.assetType, this.assetName, this.officeTeams,
						this.scopeMo);

		// Ensure correct number created
		assertEquals("Incorrect number of created meta-data",
				expectedCreateCount, metaData.length);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Constructs the {@link RawBoundAdministratorMetaData} including linking
	 * its {@link Task} instances.
	 * 
	 * @return Constructed {@link RawBoundAdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private RawBoundAdministratorMetaData<?, DutyKey> constructRawAdministratorAndLinkTasks() {

		// Construct the raw administrator meta-data
		RawBoundAdministratorMetaData metaData = this
				.constructRawAdministrator(1, this.configuration)[0];

		// Link the tasks
		metaData.linkTasks(this.taskMetaDataLocator, this.assetManagerFactory,
				this.issues);

		// Return the raw bound administrator meta-data
		return (RawBoundAdministratorMetaData<?, DutyKey>) metaData;
	}
}
