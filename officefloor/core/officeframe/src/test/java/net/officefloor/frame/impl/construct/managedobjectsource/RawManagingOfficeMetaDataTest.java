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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.MockOfficeMetaDataBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.MockRawBoundManagedObjectInstanceMetaDataBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.MockRawBoundManagedObjectMetaDataBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.MockRawManagedObjectMetaDataBuilder;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectCleanupImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawManagingOfficeMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagingOfficeMetaDataTest extends OfficeFrameTestCase {

	/**
	 * {@link Flow} key.
	 */
	private enum Flows {
		KEY, WRONG_KEY
	}

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private static final String MANAGED_OBJECT_SOURCE_NAME = "MANAGED_OBJECT_SOURCE";

	/**
	 * Name of the managing {@link Office}.
	 */
	private static final String MANAGING_OFFICE_NAME = "MANAGING_OFFICE";

	/**
	 * Input name of the {@link ManagedObject}.
	 */
	private static final String INPUT_MANAGED_OBJECT_NAME = "INPUT_MANAGED_OBJECT_NAME";

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	private final MockRawManagedObjectMetaDataBuilder<Indexed, Flows> rawMoMetaData = MockConstruct
			.mockRawManagedObjectMetaData(MANAGED_OBJECT_SOURCE_NAME);

	/**
	 * Name of the recylce {@link ManagedFunction}.
	 */
	private String recycleFunctionName = null;

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeBuilderImpl<Flows> configuration = new ManagingOfficeBuilderImpl<>(
			MANAGING_OFFICE_NAME);

	/**
	 * {@link InputManagedObjectConfiguration}.
	 */
	private InputManagedObjectConfiguration<Flows> inputConfiguration = new DependencyMappingBuilderImpl<>(
			INPUT_MANAGED_OBJECT_NAME);

	/**
	 * {@link Office} {@link TeamManagement} instances.
	 */
	@SuppressWarnings("unchecked")
	private final Map<String, TeamManagement> officeTeams = this.createMock(Map.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final MockOfficeMetaDataBuilder officeMetaData = MockConstruct.mockOfficeMetaData(MANAGING_OFFICE_NAME);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensure issue if no {@link ManagedFunctionMetaData} for recycle
	 * {@link ManagedFunction}.
	 */
	public void testNoFunctionForRecycleFunction() {

		// Record no function for recycle
		this.recycleFunctionName = "RECYCLE_FUNCTION";
		this.record_issue("Recycle function 'RECYCLE_FUNCTION' not found");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incompatible parameter type for recycle
	 * {@link ManagedFunction}.
	 */
	public void testIncompatibleRecycleFunction() {

		this.recycleFunctionName = "RECYCLE_FUNCTION";
		this.officeMetaData.addManagedFunction(this.recycleFunctionName, Integer.class);
		this.record_issue("Incompatible parameter type for recycle function (parameter=" + Integer.class.getName()
				+ ", required type=" + RecycleManagedObjectParameter.class.getName() + ", function="
				+ this.recycleFunctionName + ")");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link the recycle {@link ManagedFunction} before managing.
	 */
	public void testLinkRecycleFunctionBeforeManaging() {

		// Record
		this.recycleFunctionName = "RECYCLE_FUNCTION";
		this.officeMetaData.addManagedFunction(this.recycleFunctionName, RecycleManagedObjectParameter.class);
		final ManagedObjectMetaDataImpl<?> moMetaData = MockConstruct.mockManagedObjectMetaData("BOUND", String.class);
		final ManagedObjectCleanup cleanup = new ManagedObjectCleanupImpl(null, this.officeMetaData.build());
		final ManagedObject managedObject = this.createMock(ManagedObject.class);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> rawOffice = this.createRawManagingOffice();

		// Have managed before managed by office.
		// This would be the possible case that used by same office.
		rawOffice.manageManagedObject(moMetaData); // undertake first
		this.run_manageByOffice(rawOffice, true);
		FunctionState function = moMetaData.recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure have recycle function
		assertNotNull("Should have recycle function", function);
	}

	/**
	 * Ensure able to link the recycle {@link ManagedFunction} after managing.
	 */
	public void testLinkRecycleFunctionAfterManaging() {

		// Record
		this.recycleFunctionName = "RECYCLE_FUNCTION";
		this.officeMetaData.addManagedFunction(this.recycleFunctionName, RecycleManagedObjectParameter.class);
		final ManagedObjectMetaDataImpl<?> moMetaData = MockConstruct.mockManagedObjectMetaData("BOUND", String.class);
		final ManagedObjectCleanup cleanup = new ManagedObjectCleanupImpl(null, this.officeMetaData.build());
		final ManagedObject managedObject = this.createMock(ManagedObject.class);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> rawOffice = this.createRawManagingOffice();
		this.run_manageByOffice(rawOffice, true);
		rawOffice.manageManagedObject(moMetaData); // undertake afterwards
		FunctionState function = moMetaData.recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure have recycle function
		assertNotNull("Should have recycle function", function);
	}

	/**
	 * Ensure able to not have a recycle {@link ManagedFunction}.
	 */
	public void testNoRecycleFunction() {

		final ManagedObjectMetaDataImpl<?> moMetaData = MockConstruct.mockManagedObjectMetaData("BOUND", String.class);
		final ManagedObjectCleanup cleanup = new ManagedObjectCleanupImpl(null, this.officeMetaData.build());
		final ManagedObject managedObject = this.createMock(ManagedObject.class);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<?> rawOffice = this.run_manageByOffice(true);

		// Ensure not obtain recycle function
		rawOffice.manageManagedObject(moMetaData);
		FunctionState function = moMetaData.recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure no recycle function
		assertNull("Should be no recycle function", function);
	}

	/**
	 * Ensures issues if no {@link ProcessState} bound name for
	 * {@link ManagedObject}.
	 */
	public void testNoBoundInputManagedObjectName() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, null);
		this.inputConfiguration = new DependencyMappingBuilderImpl<>(null);
		this.record_issue("ManagedObjectSource invokes flows but does not provide input Managed Object binding name");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if {@link ManagedObject} is not managed by the
	 * {@link Office}.
	 */
	public void testNotManagedByOffice() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, null);
		this.record_issue("ManagedObjectSource by input name '" + INPUT_MANAGED_OBJECT_NAME + "' not managed by Office "
				+ MANAGING_OFFICE_NAME);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> office = this.createRawManagingOffice();
		MockRawBoundManagedObjectMetaDataBuilder<?, ?> rawBoundManagedObjectMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData("BOUND", this.rawMoMetaData.build(office));
		this.run_manageByOffice(office, false, rawBoundManagedObjectMetaData.build());
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if no {@link RawBoundManagedObjectInstanceMetaData} for
	 * the {@link ManagedObjectSource}.
	 */
	public void testNoInstanceForManagedObjectSource() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, null);
		this.record_issue("ManagedObjectSource by input name '" + INPUT_MANAGED_OBJECT_NAME + "' not managed by Office "
				+ MANAGING_OFFICE_NAME);

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, "ANOTHER");
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if {@link Flow} instances configured but no {@link Flow}
	 * instances required.
	 */
	public void testNoFlowsButFlowsConfigured() {

		// Record flows configured but none required
		this.configuration.linkFlow(0, "test");
		this.record_issue("ManagedObjectSourceMetaData specifies no flows but flows configured for it");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link Flow} is configured.
	 */
	public void testNoFlowConfigured() {

		// Record no flow configured
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, Flows.KEY);
		this.record_issue("No flow configured for flow 0 (key=" + Flows.KEY + ", label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedFunctionMetaData} for the {@link Flow}.
	 */
	public void testNoFlowFunction() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, Flows.KEY);
		this.configuration.getBuilder().linkFlow(Flows.KEY, "FUNCTION");
		this.record_issue(
				"Can not find function meta-data FUNCTION for flow 0 (key=" + Flows.KEY + ", label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if incompatible argument to {@link flow}
	 * {@link ManagedFunction}.
	 */
	public void testIncompatibleFlowArgument() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(Integer.class, Flows.KEY);
		this.configuration.getBuilder().linkFlow(Flows.KEY, "FUNCTION");
		this.officeMetaData.addManagedFunction("FUNCTION", String.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + Integer.class.getName()
				+ ", parameter=" + String.class.getName()
				+ ", function=FUNCTION) for flow 0 (key=KEY, label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if extra {@link Flow} configured.
	 */
	public void testExtraFlowConfigured() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, Flows.KEY);
		this.configuration.getBuilder().linkFlow(Flows.KEY, "FUNCTION");
		this.configuration.getBuilder().linkFlow(Flows.WRONG_KEY, "FUNCTION");
		this.officeMetaData.addManagedFunction("FUNCTION", null);
		this.record_issue("Extra flows configured than specified by ManagedObjectSourceMetaData");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link Flow} for a {@link ManagedObject}.
	 */
	public void testConstructFlow() throws Exception {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, Flows.KEY);

		// Configure function through managed object source context
		ManagedObjectSourceContextImpl<Flows> context = new ManagedObjectSourceContextImpl<Flows>(false,
				INPUT_MANAGED_OBJECT_NAME, this.configuration, null, null, this.configuration,
				this.officeMetaData.getBuilder());
		context.addManagedFunction("FUNCTION", null);
		context.getFlow(Flows.KEY).linkFunction("FUNCTION");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(true, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();

		// Ensure function name spaced to managed object
		ManagedFunctionMetaData<?, ?> function = this.officeMetaData.build().getManagedFunctionLocator()
				.getManagedFunctionMetaData(INPUT_MANAGED_OBJECT_NAME + ".FUNCTION");
		assertNotNull("Should have managed object name spaced function", function);
	}

	/**
	 * Ensures able to construct {@link Flow} for a {@link ManagedObject} that
	 * is not the first bound or first instance.
	 */
	public void testConstructFlowOfNotFirstBoundOrInstance() throws Exception {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, Flows.KEY);

		// Configure function through managed object source context
		ManagedObjectSourceContextImpl<Flows> context = new ManagedObjectSourceContextImpl<Flows>(false,
				INPUT_MANAGED_OBJECT_NAME, this.configuration, null, null, this.configuration,
				this.officeMetaData.getBuilder());
		context.addManagedFunction("FUNCTION", null);
		context.getFlow(Flows.KEY).linkFunction("FUNCTION");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(true, "NOT_MATCH_INDEX_0", "NOT_MATCH_INDEX_1", INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();

		// Ensure function name spaced to managed object
		ManagedFunctionMetaData<?, ?> function = this.officeMetaData.build().getManagedFunctionLocator()
				.getManagedFunctionMetaData(INPUT_MANAGED_OBJECT_NAME + ".FUNCTION");
		assertNotNull("Should have managed object name spaced function", function);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_SOURCE_NAME, issueDescription);
	}

	/**
	 * Creates the {@link RawManagingOfficeMetaData} for testing.
	 * 
	 * @return New {@link RawManagingOfficeMetaData}.
	 */
	private RawManagingOfficeMetaData<Flows> createRawManagingOffice() {
		// Create and return the raw managing office meta-data
		RawManagingOfficeMetaData<Flows> rawManagingOffice = new RawManagingOfficeMetaData<>(MANAGING_OFFICE_NAME,
				this.recycleFunctionName, this.inputConfiguration,
				this.rawMoMetaData.getManagedObjectSourceMetaData().getFlowMetaData(), this.configuration);
		rawManagingOffice.setRawManagedObjectMetaData(this.rawMoMetaData.build(rawManagingOffice));
		return rawManagingOffice;
	}

	/**
	 * Creates the {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param rawManaingOfficeMetaData
	 *            {@link RawManagingOfficeMetaData}.
	 * @return {@link RawBoundManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaData createRawBoundManagedObjectMetaData(String managedObjectSourceName,
			RawManagingOfficeMetaData<Flows> rawManaingOfficeMetaData) {
		MockRawBoundManagedObjectMetaDataBuilder<Indexed, Flows> rawBoundManagedObjectMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData(managedObjectSourceName,
						this.rawMoMetaData.build(rawManaingOfficeMetaData));
		MockRawBoundManagedObjectInstanceMetaDataBuilder<Indexed, Flows> instance = rawBoundManagedObjectMetaData
				.addRawBoundManagedObjectInstanceMetaData();
		rawBoundManagedObjectMetaData.build();
		instance.build().loadManagedObjectMetaData(AssetType.MANAGED_OBJECT, managedObjectSourceName,
				MockConstruct.mockAssetManagerFactory(), this.issues);
		return rawBoundManagedObjectMetaData.build();
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaData} and runs the manage by
	 * office.
	 *
	 * @param rawManagingOffice
	 *            {@link RawManagingOfficeMetaData}.
	 * @param isCreateExecuteContext
	 *            <code>true</code> if {@link ManagedObjectExecuteContext}
	 *            should be available.
	 * @param recycleFunctionName
	 *            Recycle {@link ManagedFunction} name.
	 * @param processBoundMetaData
	 *            {@link ProcessState} bound
	 *            {@link RawBoundManagedObjectMetaData} for the {@link Office}.
	 */
	private void run_manageByOffice(RawManagingOfficeMetaData<Flows> rawManagingOffice, boolean isCreateExecuteContext,
			RawBoundManagedObjectMetaData... processBoundMetaData) {

		// Manage by office
		rawManagingOffice.manageByOffice(this.officeMetaData.build(), processBoundMetaData, this.officeTeams,
				this.issues);

		// Validate creation of execute context
		if (isCreateExecuteContext) {
			assertNotNull("Should have execute context available",
					rawManagingOffice.getManagedObjectExecuteContextFactory());
		} else {
			assertNull("Execute context should not be available",
					rawManagingOffice.getManagedObjectExecuteContextFactory());
		}
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaData} and runs the manage by
	 * office.
	 * 
	 * @param isCreateExecuteContext
	 *            <code>true</code> if {@link ManagedObjectExecuteContext}
	 *            should be available.
	 * @param recycleFunctionName
	 *            Recycle {@link ManagedFunction} name.
	 * @param processBoundMetaDataNames
	 *            Names fo the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @return New {@link RawManagingOfficeMetaData} with manage by office run.
	 */
	private RawManagingOfficeMetaData<?> run_manageByOffice(boolean isCreateExecuteContext,
			String... processBoundMetaDataNames) {

		// Create and manage by office
		RawManagingOfficeMetaData<Flows> rawManagingOfficeMetaData = this.createRawManagingOffice();

		// Create the process bound managed object instances
		RawBoundManagedObjectMetaData[] managedObjects = new RawBoundManagedObjectMetaData[processBoundMetaDataNames.length];
		for (int i = 0; i < processBoundMetaDataNames.length; i++) {
			managedObjects[i] = this.createRawBoundManagedObjectMetaData(processBoundMetaDataNames[i],
					rawManagingOfficeMetaData);
		}

		// Manage by the office
		this.run_manageByOffice(rawManagingOfficeMetaData, isCreateExecuteContext, managedObjects);

		// Return the raw managing office meta-data
		return rawManagingOfficeMetaData;
	}

}