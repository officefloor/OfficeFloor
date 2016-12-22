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

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.impl.execute.duty.DutyKeyImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.DutyGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownResourceError;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawBoundAdministratorMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundAdministratorMetaDataTest extends OfficeFrameTestCase {

	/**
	 * {@link AdministratorSourceConfiguration}.
	 */
	private final AdministratorSourceConfiguration<?, ?> configuration = this
			.createMock(AdministratorSourceConfiguration.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this
			.createMock(SourceContext.class);

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
	 * {@link TeamManagement} instances by their {@link Office} registered
	 * names.
	 */
	private final Map<String, TeamManagement> officeTeams = new HashMap<String, TeamManagement>();

	/**
	 * Responsible {@link TeamManagement}.
	 */
	private final TeamManagement responsibleTeam = this
			.createMock(TeamManagement.class);

	/**
	 * Continue {@link Team}.
	 */
	private final Team continueTeam = this.createMock(Team.class);

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
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorName(), "");
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
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorName(), "ADMIN");
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorSourceClass(), null);
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
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorName(), "ADMIN");
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorSourceClass(), Object.class);
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
	 * Ensures issue if missing required property.
	 */
	public void testMissingProperty() {

		// Record fail instantiate due to missing property
		this.record_init();
		this.issues.addIssue(this.assetType, this.assetName,
				"Property 'required.property' must be specified");

		// Attempt to construct administrator
		this.replayMockObjects();
		MockAdministratorSource.requiredPropertyName = "required.property";
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if missing {@link Class}.
	 */
	public void testClassLoaderAndMissingClass() {

		final ClassLoader classLoader = new URLClassLoader(new URL[0]);
		final String CLASS_NAME = "UNKONWN CLASS";

		// Record fail instantiate due to missing class
		this.record_init();
		this.recordReturn(this.sourceContext,
				this.sourceContext.getClassLoader(), classLoader);
		this.sourceContext.loadClass(CLASS_NAME);
		this.control(this.sourceContext).setThrowable(
				new UnknownClassError("TEST ERROR", CLASS_NAME));
		this.issues.addIssue(this.assetType, this.assetName,
				"Can not load class '" + CLASS_NAME + "'");

		// Attempt to construct administrator
		this.replayMockObjects();
		MockAdministratorSource.classLoader = classLoader;
		MockAdministratorSource.requiredClassName = CLASS_NAME;
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if missing resource.
	 */
	public void testMissingResource() {

		final String RESOURCE_LOCATION = "UNKONWN RESOURCE";

		// Record fail instantiate due to missing resource
		this.record_init();
		this.sourceContext.getResource(RESOURCE_LOCATION);
		this.control(this.sourceContext).setThrowable(
				new UnknownResourceError("TEST ERROR", RESOURCE_LOCATION));
		this.issues.addIssue(this.assetType, this.assetName,
				"Can not obtain resource at location '" + RESOURCE_LOCATION
						+ "'");

		// Attempt to construct administrator
		this.replayMockObjects();
		MockAdministratorSource.requiredResourceLocation = RESOURCE_LOCATION;
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
		this.recordReturn(this.configuration,
				this.configuration.getOfficeTeamName(), null);
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
		this.recordReturn(this.configuration,
				this.configuration.getOfficeTeamName(), "TEAM");
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
		this.recordReturn(this.configuration,
				this.configuration.getAdministeredManagedObjectNames(),
				new String[] { null });
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
		this.recordReturn(this.configuration,
				this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
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
		this.recordReturn(this.configuration,
				this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(
				this.rawBoundMoMetaData,
				this.rawBoundMoMetaData
						.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { this.rawBoundMoInstanceMetaData });
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(),
				this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData,
				this.rawMoMetaData.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData
						.getExtensionInterfacesMetaData(), null);
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(),
				this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData,
				this.rawMoMetaData.getManagedObjectName(),
				"MANAGED_OBJECT_SOURCE");
		this.issues
				.addIssue(
						this.assetType,
						this.assetName,
						"Managed Object 'MO' does not support extension interface "
								+ Object.class.getName()
								+ " required by Administrator ADMIN (ManagedObjectSource=MANAGED_OBJECT_SOURCE)");

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
		this.recordReturn(this.configuration,
				this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(
				this.rawBoundMoMetaData,
				this.rawBoundMoMetaData
						.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { this.rawBoundMoInstanceMetaData });
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(),
				this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData,
				this.rawMoMetaData.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(
				this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData
						.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { this.extensionInterfaceMetaData });
		this.recordReturn(this.extensionInterfaceMetaData,
				this.extensionInterfaceMetaData.getExtensionInterfaceType(),
				Integer.class);
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(),
				this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData,
				this.rawMoMetaData.getManagedObjectName(),
				"MANAGED_OBJECT_SOURCE");
		this.issues
				.addIssue(
						this.assetType,
						this.assetName,
						"Managed Object 'MO' does not support extension interface "
								+ String.class.getName()
								+ " required by Administrator ADMIN (ManagedObjectSource=MANAGED_OBJECT_SOURCE)");

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
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"Administrator ADMIN does not provide duties");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no name for {@link Duty}.
	 */
	public void testNoDutyName() {

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		// Record no duties
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(), null);
		this.issues.addIssue(this.assetType, this.assetName,
				"No name provided for duty 0");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no keys for {@link Duty} instances are not of same type.
	 */
	public void testDutyKeysOfDifferentTypes() {

		final AdministratorDutyMetaData<?, ?> dutyOne = this
				.createMock(AdministratorDutyMetaData.class);
		final AdministratorDutyMetaData<?, ?> dutyTwo = this
				.createMock(AdministratorDutyMetaData.class);

		// Record no duties
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyOne, dutyTwo });
		this.recordReturn(dutyOne, dutyOne.getDutyName(), DutyKey.ONE.name());
		this.recordReturn(dutyOne, dutyOne.getKey(), DutyKey.ONE);
		this.recordReturn(dutyTwo, dutyTwo.getDutyName(),
				WrongDutyKeyType.WRONG_DUTY_KEY_TYPE.name());
		this.recordReturn(dutyTwo, dutyTwo.getKey(),
				WrongDutyKeyType.WRONG_DUTY_KEY_TYPE);
		this.issues.addIssue(this.assetType, this.assetName,
				"Duty key " + WrongDutyKeyType.WRONG_DUTY_KEY_TYPE
						+ " is of incorrect type [type="
						+ WrongDutyKeyType.class.getName() + ", required type="
						+ DutyKey.class.getName() + "]");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministrator(0, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link RawBoundAdministratorMetaData}.
	 */
	public void testConstructRawBoundAdministratorMetaData() {

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);
		final ManagedObjectMetaData<?> moMetaData = this
				.createMock(ManagedObjectMetaData.class);
		final Object extensionInterface = "EXTENSION_INTERFACE";

		// Record successfully create bound administrator
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyMetaData, dutyMetaData.getKey(), DutyKey.ONE);

		// Record ei extraction to verify the extension interface factory
		this.recordReturn(moMetaData, moMetaData.getInstanceIndex(), 0);
		this.recordReturn(this.extensionInterfaceFactory,
				this.extensionInterfaceFactory
						.createExtensionInterface(managedObject),
				extensionInterface);

		// Test
		this.replayMockObjects();

		// Construct the administrators
		RawBoundAdministratorMetaData<?, ?>[] rawAdminMetaDatas = this
				.constructRawAdministrator(1, this.configuration);
		RawBoundAdministratorMetaData<?, ?> rawAdminMetaData = rawAdminMetaDatas[0];
		AdministratorMetaData<?, ?> adminMetaData = rawAdminMetaData
				.getAdministratorMetaData();

		// Verify extension interface factory by extracting ei
		assertEquals("Incorrect number of administered managed objects", 1,
				adminMetaData.getExtensionInterfaceMetaData().length);
		ExtensionInterfaceMetaData<?> moEiMetaData = adminMetaData
				.getExtensionInterfaceMetaData()[0];
		assertEquals("Incorrect extracted extension interface",
				extensionInterface, moEiMetaData
						.getExtensionInterfaceExtractor()
						.extractExtensionInterface(managedObject, moMetaData));

		// Verify mocks
		this.verifyMockObjects();

		// Verify bound administrator meta-data
		assertEquals("Incorrect name", "ADMIN",
				rawAdminMetaData.getBoundAdministratorName());
		assertEquals("Incorrect scope", this.administratorScope,
				rawAdminMetaData.getAdministratorIndex()
						.getAdministratorScope());
		assertEquals("Incorrect bound index", 0, rawAdminMetaData
				.getAdministratorIndex().getIndexOfAdministratorWithinScope());
		assertEquals("Incorrect first duty key", DutyKey.ONE, rawAdminMetaData
				.getDutyKey(DutyKey.ONE.name()).getKey());
		assertNull("Should only have one duty",
				rawAdminMetaData.getDutyKey(DutyKey.TWO));

		// Verify remaining administrator meta-data
		assertNotNull("Must have admin source",
				adminMetaData.getAdministratorSource());
		assertEquals("Incorrect responsible team", this.responsibleTeam,
				adminMetaData.getResponsibleTeam());
		assertEquals("Incorrect continue team", this.continueTeam,
				adminMetaData.getContinueTeam());
		assertEquals("Incorrect administered managed object",
				this.managedObjectIndex, moEiMetaData.getManagedObjectIndex());

		// Verify the administrator meta-data
		assertNotNull("Must have admin source",
				adminMetaData.getAdministratorSource());
	}

	/**
	 * Ensure issue if no {@link Duty} name is provided by the
	 * {@link DutyConfiguration}.
	 */
	public void testNoConfiguredDutyName() {

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyMetaData, dutyMetaData.getKey(), DutyKey.ONE);

		// Record linking tasks
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getDutyName(), null);
		this.record_issue("Duty name not provided by duty configuration");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkOfficeMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if {@link Duty} name does not correspond to a {@link Duty}.
	 */
	public void testIncorrectDutyName() {

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyMetaData, dutyMetaData.getKey(), DutyKey.ONE);

		// Record linking tasks
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getDutyName(),
				WrongDutyKeyType.WRONG_DUTY_KEY_TYPE.name());
		this.record_issue("No duty by name "
				+ WrongDutyKeyType.WRONG_DUTY_KEY_TYPE);

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkOfficeMetaData();
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

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyMetaData, dutyMetaData.getKey(), DutyKey.ONE);

		// Record linking tasks
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getDutyName(), DutyKey.ONE.name());
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getLinkedProcessConfiguration(), null);
		this.record_issue("Task references not provided for duty "
				+ DutyKey.ONE);

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkOfficeMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link DutyConfiguration} for a {@link Duty}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testNoDutyConfiguration() {

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyMetaData, dutyMetaData.getKey(), DutyKey.ONE);

		// Record linking tasks
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration[0]);
		this.record_issue("Must provide configuration for duty [index="
				+ DutyKey.ONE.ordinal() + ", key=" + DutyKey.ONE.name() + "]");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		RawBoundAdministratorMetaData metaData = this
				.constructRawAdministrator(1, this.configuration)[0];
		metaData.getDutyKey(DutyKey.ONE); // flags linked to a task
		metaData.linkOfficeMetaData(this.taskMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Test only required {@link DutyConfiguration} for a {@link Duty} if linked
	 * to a {@link ManagedFunction}.
	 */
	public void testNotRequireDutyConfiguration() {

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyMetaData, dutyMetaData.getKey(), DutyKey.ONE);

		// Record not linked to tasks
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration[0]);

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkOfficeMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} is found for the {@link Duty}
	 * {@link Flow}.
	 */
	public void testNoTaskForDutyFlow() {

		final AdministratorDutyMetaData<?, ?> dutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyMetaData });
		this.recordReturn(dutyMetaData, dutyMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyMetaData, dutyMetaData.getKey(), DutyKey.ONE);

		// Record linking tasks
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getDutyName(), DutyKey.ONE.name());
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getLinkedProcessConfiguration(),
				new TaskNodeReference[] { taskReference });
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(this.taskMetaDataLocator,
				this.taskMetaDataLocator.getTaskMetaData("WORK", "TASK"), null);
		this.record_issue("Can not find task meta-data (work=WORK, task=TASK) for Duty "
				+ DutyKey.ONE + " Flow 0");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		this.constructRawAdministratorAndLinkOfficeMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link FlowMetaData} to {@link DutyMetaData}.
	 */
	public void testLinkFlowsToDuty() {

		final AdministratorDutyMetaData<?, ?> dutyOneMetaData = this
				.createMock(AdministratorDutyMetaData.class);
		final AdministratorDutyMetaData<?, ?> dutyTwoMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);
		final ManagedFunctionMetaData<?, ?, ?> taskMetaData = this
				.createMock(ManagedFunctionMetaData.class);

		final OfficeMetaData officeMetaData = this
				.createMock(OfficeMetaData.class);
		final ProcessMetaData processMetaData = this
				.createMock(ProcessMetaData.class);
		final ThreadMetaData threadMetaData = this
				.createMock(ThreadMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyOneMetaData,
						dutyTwoMetaData });
		this.recordReturn(dutyOneMetaData, dutyOneMetaData.getDutyName(),
				DutyKey.ONE.name());
		this.recordReturn(dutyOneMetaData, dutyOneMetaData.getKey(),
				DutyKey.ONE);
		this.recordReturn(dutyTwoMetaData, dutyTwoMetaData.getDutyName(),
				DutyKey.TWO.name());
		this.recordReturn(dutyTwoMetaData, dutyTwoMetaData.getKey(),
				DutyKey.TWO);

		// Record linking tasks
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration[] { this.dutyOneConfiguration,
						this.dutyTwoConfiguration });
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getDutyName(), DutyKey.ONE.name());
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getLinkedProcessConfiguration(),
				new TaskNodeReference[] { taskReference });
		this.recordReturn(this.dutyTwoConfiguration,
				this.dutyTwoConfiguration.getDutyName(), DutyKey.TWO.name());
		this.recordReturn(this.dutyTwoConfiguration,
				this.dutyTwoConfiguration.getLinkedProcessConfiguration(),
				new TaskNodeReference[0]);
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(taskReference, taskReference.getArgumentType(),
				Connection.class);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				Connection.class);
		this.recordReturn(this.taskMetaDataLocator,
				this.taskMetaDataLocator.getTaskMetaData("WORK", "TASK"),
				taskMetaData);

		// Record governance for first duty
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getGovernanceConfiguration(),
				new DutyGovernanceConfiguration[0]);
		this.recordReturn(this.taskMetaDataLocator,
				this.taskMetaDataLocator.getOfficeMetaData(), officeMetaData);
		this.recordReturn(officeMetaData, officeMetaData.getProcessMetaData(),
				processMetaData);
		this.recordReturn(processMetaData, processMetaData.getThreadMetaData(),
				threadMetaData);
		this.recordReturn(threadMetaData,
				threadMetaData.getGovernanceMetaData(),
				new GovernanceMetaData[0]);

		// Record governance for second duty
		this.recordReturn(this.dutyTwoConfiguration,
				this.dutyTwoConfiguration.getGovernanceConfiguration(),
				new DutyGovernanceConfiguration[0]);
		this.recordReturn(this.taskMetaDataLocator,
				this.taskMetaDataLocator.getOfficeMetaData(), officeMetaData);
		this.recordReturn(officeMetaData, officeMetaData.getProcessMetaData(),
				processMetaData);
		this.recordReturn(processMetaData, processMetaData.getThreadMetaData(),
				threadMetaData);
		this.recordReturn(threadMetaData,
				threadMetaData.getGovernanceMetaData(),
				new GovernanceMetaData[0]);

		// Construct the administrator and link tasks
		this.replayMockObjects();
		RawBoundAdministratorMetaData<?, DutyKey> rawAdminMetaData = this
				.constructRawAdministratorAndLinkOfficeMetaData();
		AdministratorMetaData<?, DutyKey> adminMetaData = rawAdminMetaData
				.getAdministratorMetaData();
		this.verifyMockObjects();

		// Verify the duties
		DutyMetaData dutyOne = adminMetaData
				.getDutyMetaData(new DutyKeyImpl<DutyKey>(DutyKey.ONE));
		assertNotNull("Must have first duty", dutyOne);
		assertNotNull("Must have second duty",
				adminMetaData.getDutyMetaData(new DutyKeyImpl<DutyKey>(
						DutyKey.TWO)));

		// Verify the flow
		FlowMetaData<?> flow = dutyOne.getFlow(0);
		assertEquals("Incorrect instigation strategy",
				FlowInstigationStrategyEnum.PARALLEL,
				flow.getInstigationStrategy());
		assertEquals("Incorrect task meta-data", taskMetaData,
				flow.getInitialTaskMetaData());
		assertNull(
				"Parallel instigation so should not have flow asset manager",
				flow.getFlowManager());
	}

	/**
	 * Ensure issue if unknown {@link Governance} linked to {@link Duty}.
	 */
	public void testLinkUnkownGovernance() {

		final AdministratorDutyMetaData<?, ?> adminDutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		final DutyGovernanceConfiguration<?> governanceConfiguration = this
				.createMock(DutyGovernanceConfiguration.class);
		final int DUTY_GOVERNANCE_INDEX = 0;

		final GovernanceMetaData<?, ?> governanceMetaData = this
				.createMock(GovernanceMetaData.class);

		final OfficeMetaData officeMetaData = this
				.createMock(OfficeMetaData.class);
		final ProcessMetaData processMetaData = this
				.createMock(ProcessMetaData.class);
		final ThreadMetaData threadMetaData = this
				.createMock(ThreadMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { adminDutyMetaData });
		this.recordReturn(adminDutyMetaData, adminDutyMetaData.getDutyName(),
				"DUTY");
		this.recordReturn(adminDutyMetaData, adminDutyMetaData.getKey(),
				DutyKey.ONE);
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration<?>[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getDutyName(), "DUTY");
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getLinkedProcessConfiguration(),
				new TaskNodeReference[0]);

		// Record governance configuration
		this.recordReturn(
				this.dutyOneConfiguration,
				this.dutyOneConfiguration.getGovernanceConfiguration(),
				new DutyGovernanceConfiguration<?>[] { governanceConfiguration });
		this.recordReturn(governanceConfiguration,
				governanceConfiguration.getIndex(), DUTY_GOVERNANCE_INDEX);
		this.recordReturn(governanceConfiguration,
				governanceConfiguration.getGovernanceName(), "GOVERNANCE");

		// Record unknown governance
		this.recordReturn(this.taskMetaDataLocator,
				this.taskMetaDataLocator.getOfficeMetaData(), officeMetaData);
		this.recordReturn(officeMetaData, officeMetaData.getProcessMetaData(),
				processMetaData);
		this.recordReturn(processMetaData, processMetaData.getThreadMetaData(),
				threadMetaData);
		this.recordReturn(threadMetaData,
				threadMetaData.getGovernanceMetaData(),
				new GovernanceMetaData<?, ?>[] { governanceMetaData });
		this.recordReturn(governanceMetaData,
				governanceMetaData.getGovernanceName(), "NOT MATCH");
		this.record_issue("Can not find governance 'GOVERNANCE' for duty 'DUTY'");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		RawBoundAdministratorMetaData<?, DutyKey> rawAdminMetaData = this
				.constructRawAdministratorAndLinkOfficeMetaData();
		AdministratorMetaData<?, DutyKey> adminMetaData = rawAdminMetaData
				.getAdministratorMetaData();
		this.verifyMockObjects();

		// Verify the duty
		DutyMetaData duty = adminMetaData
				.getDutyMetaData(new DutyKeyImpl<DutyKey>(DutyKey.ONE));
		assertNotNull("Must have duty", duty);

		// Verify the governance not mapped
		int threadIndex = duty
				.translateGovernanceIndexToThreadIndex(DUTY_GOVERNANCE_INDEX);
		assertEquals("Thread index should not be translated", -1, threadIndex);
	}

	/**
	 * Ensure can link {@link Governance} to {@link DutyMetaData}.
	 */
	public void testLinkGovernanceToDuty() {

		final AdministratorDutyMetaData<?, ?> adminDutyMetaData = this
				.createMock(AdministratorDutyMetaData.class);

		final DutyGovernanceConfiguration<?> governanceConfiguration = this
				.createMock(DutyGovernanceConfiguration.class);
		final int DUTY_GOVERNANCE_INDEX = 0;

		final GovernanceMetaData<?, ?> governanceMetaDataOne = this
				.createMock(GovernanceMetaData.class);
		final GovernanceMetaData<?, ?> governanceMetaDataTwo = this
				.createMock(GovernanceMetaData.class);

		final OfficeMetaData officeMetaData = this
				.createMock(OfficeMetaData.class);
		final ProcessMetaData processMetaData = this
				.createMock(ProcessMetaData.class);
		final ThreadMetaData threadMetaData = this
				.createMock(ThreadMetaData.class);

		// Record construction of bound administrator meta-data
		this.record_init();
		this.record_team();
		this.record_managedObject();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { adminDutyMetaData });
		this.recordReturn(adminDutyMetaData, adminDutyMetaData.getDutyName(),
				"DUTY");
		this.recordReturn(adminDutyMetaData, adminDutyMetaData.getKey(),
				DutyKey.ONE);
		this.recordReturn(this.configuration,
				this.configuration.getDutyConfiguration(),
				new DutyConfiguration<?>[] { this.dutyOneConfiguration });
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getDutyName(), "DUTY");
		this.recordReturn(this.dutyOneConfiguration,
				this.dutyOneConfiguration.getLinkedProcessConfiguration(),
				new TaskNodeReference[0]);

		// Record governance configuration
		this.recordReturn(
				this.dutyOneConfiguration,
				this.dutyOneConfiguration.getGovernanceConfiguration(),
				new DutyGovernanceConfiguration<?>[] { governanceConfiguration });
		this.recordReturn(governanceConfiguration,
				governanceConfiguration.getIndex(), DUTY_GOVERNANCE_INDEX);
		this.recordReturn(governanceConfiguration,
				governanceConfiguration.getGovernanceName(), "GOVERNANCE");

		// Record configuring links
		this.recordReturn(this.taskMetaDataLocator,
				this.taskMetaDataLocator.getOfficeMetaData(), officeMetaData);
		this.recordReturn(officeMetaData, officeMetaData.getProcessMetaData(),
				processMetaData);
		this.recordReturn(processMetaData, processMetaData.getThreadMetaData(),
				threadMetaData);
		this.recordReturn(threadMetaData,
				threadMetaData.getGovernanceMetaData(),
				new GovernanceMetaData<?, ?>[] { governanceMetaDataOne,
						governanceMetaDataTwo });
		this.recordReturn(governanceMetaDataOne,
				governanceMetaDataOne.getGovernanceName(), "NOT MATCH");
		this.recordReturn(governanceMetaDataTwo,
				governanceMetaDataTwo.getGovernanceName(), "GOVERNANCE");

		// Construct the administrator and link tasks
		this.replayMockObjects();
		RawBoundAdministratorMetaData<?, DutyKey> rawAdminMetaData = this
				.constructRawAdministratorAndLinkOfficeMetaData();
		AdministratorMetaData<?, DutyKey> adminMetaData = rawAdminMetaData
				.getAdministratorMetaData();
		this.verifyMockObjects();

		// Verify the duty
		DutyMetaData duty = adminMetaData
				.getDutyMetaData(new DutyKeyImpl<DutyKey>(DutyKey.ONE));
		assertNotNull("Must have duty", duty);

		// Verify the governance
		int threadIndex = duty
				.translateGovernanceIndexToThreadIndex(DUTY_GOVERNANCE_INDEX);
		assertEquals("Incorrect thread index", 1, threadIndex);
	}

	/**
	 * Records initiating the {@link AdministratorSource}.
	 */
	private void record_init() {
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorName(), "ADMIN");
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorSourceClass(),
				MockAdministratorSource.class);
		this.recordReturn(this.configuration,
				this.configuration.getProperties(), new SourcePropertiesImpl());
	}

	/**
	 * Records obtaining the {@link Team}.
	 */
	private void record_team() {
		final String TEAM_NAME = "TEAM";
		this.recordReturn(this.configuration,
				this.configuration.getOfficeTeamName(), TEAM_NAME);
		this.officeTeams.put(TEAM_NAME, this.responsibleTeam);
	}

	/**
	 * Records the administering of {@link ManagedObject} instances.
	 */
	private void record_managedObject() {
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				String.class);
		this.recordReturn(this.configuration,
				this.configuration.getAdministeredManagedObjectNames(),
				new String[] { "MO" });
		this.scopeMo.put("MO", this.rawBoundMoMetaData);
		this.recordReturn(
				this.rawBoundMoMetaData,
				this.rawBoundMoMetaData
						.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { this.rawBoundMoInstanceMetaData });
		this.recordReturn(this.rawBoundMoInstanceMetaData,
				this.rawBoundMoInstanceMetaData.getRawManagedObjectMetaData(),
				this.rawMoMetaData);
		this.recordReturn(this.rawMoMetaData,
				this.rawMoMetaData.getManagedObjectSourceMetaData(),
				this.managedObjectSourceMetaData);
		this.recordReturn(
				this.managedObjectSourceMetaData,
				this.managedObjectSourceMetaData
						.getExtensionInterfacesMetaData(),
				new ManagedObjectExtensionInterfaceMetaData[] { this.extensionInterfaceMetaData });
		this.recordReturn(this.extensionInterfaceMetaData,
				this.extensionInterfaceMetaData.getExtensionInterfaceType(),
				String.class);
		this.recordReturn(this.rawBoundMoMetaData,
				this.rawBoundMoMetaData.getManagedObjectIndex(),
				this.managedObjectIndex);
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
	@TestSource
	public static class MockAdministratorSource implements
			AdministratorSource<Object, DutyKey> {

		/**
		 * Property name that must be available.
		 */
		public static String requiredPropertyName = null;

		/**
		 * {@link ClassLoader}.
		 */
		public static ClassLoader classLoader = null;

		/**
		 * {@link Class} that must be loaded.
		 */
		public static String requiredClassName = null;

		/**
		 * Location of a required resource.
		 */
		public static String requiredResourceLocation = null;

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
			requiredPropertyName = null;
			classLoader = null;
			requiredClassName = null;
			requiredResourceLocation = null;
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

			// Obtain the required property
			if (requiredPropertyName != null) {
				context.getProperty(requiredPropertyName);
			}

			// Obtain the class loader
			if (classLoader != null) {
				assertSame("Incorrect class loader", classLoader,
						context.getClassLoader());
			}

			// Load the required class
			if (requiredClassName != null) {
				context.loadClass(requiredClassName);
			}

			// Obtain the required resource
			if (requiredResourceLocation != null) {
				context.getResource(requiredResourceLocation);
			}

			// Ensure can obtain defaulted property
			assertEquals("Must default property", "DEFAULT",
					context.getProperty("property to default", "DEFAULT"));

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
						configuration, this.sourceContext, this.issues,
						this.administratorScope, this.assetType,
						this.assetName, this.officeTeams, this.continueTeam,
						this.scopeMo);

		// Ensure correct number created
		assertEquals("Incorrect number of created meta-data",
				expectedCreateCount, metaData.length);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Constructs the {@link RawBoundAdministratorMetaData} including linking
	 * its {@link ManagedFunction} instances.
	 * 
	 * @return Constructed {@link RawBoundAdministratorMetaData}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RawBoundAdministratorMetaData<?, DutyKey> constructRawAdministratorAndLinkOfficeMetaData() {

		// Construct the raw administrator meta-data
		RawBoundAdministratorMetaData metaData = this
				.constructRawAdministrator(1, this.configuration)[0];

		// Link the tasks
		metaData.linkOfficeMetaData(this.taskMetaDataLocator,
				this.assetManagerFactory, this.issues);

		// Return the raw bound administrator meta-data
		return (RawBoundAdministratorMetaData<?, DutyKey>) metaData;
	}
}
