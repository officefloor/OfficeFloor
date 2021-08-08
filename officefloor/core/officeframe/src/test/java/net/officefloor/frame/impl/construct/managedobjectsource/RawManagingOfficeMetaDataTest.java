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

package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectInstanceMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionInvocationImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.ManagedObjectAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectCleanupImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectStartupFunction;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.MockClockFactory;
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
	private final RawManagedObjectMetaDataMockBuilder<Indexed, Flows> rawMoMetaData = MockConstruct
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
	 * Start up {@link ManagedFunctionInvocation} instances.
	 */
	private final List<ManagedFunctionInvocation> startupFunctions = new LinkedList<>();

	/**
	 * {@link InputManagedObjectConfiguration}.
	 */
	private InputManagedObjectConfiguration<Flows> inputConfiguration = new DependencyMappingBuilderImpl<>(
			INPUT_MANAGED_OBJECT_NAME);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaDataMockBuilder officeMetaData = MockConstruct.mockOfficeMetaData(MANAGING_OFFICE_NAME);

	/**
	 * Default {@link ExecutionStrategy}.
	 */
	private ThreadFactory[] defaultExecutionStrategy = null;

	/**
	 * {@link ExecutionStrategy} instances by name.
	 */
	private final Map<String, ThreadFactory[]> executionStrategies = new HashMap<>();

	/**
	 * {@link AssetManagerRegistry}.
	 */
	private final AssetManagerRegistry assetManagerFactory = new AssetManagerRegistry(null, null);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Root {@link SourceContext}.
	 */
	private final SourceContext rootContext = new SourceContextImpl("ROOT", false, null,
			Thread.currentThread().getContextClassLoader(), new MockClockFactory());

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
		final ManagedObjectCleanup cleanup = new ManagedObjectCleanupImpl(null, this.officeMetaData.build());
		final ManagedObject managedObject = this.createMock(ManagedObject.class);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> rawOffice = this.createRawManagingOffice();
		final RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> instance = this
				.createRawBoundManagedObjectInstanceMetaData("MOS", rawOffice);

		// Have managed before managed by office.
		// This would be the possible case that used by same office.
		rawOffice.manageManagedObject(instance.build(), this.assetManagerFactory, 1); // undertake first
		this.run_manageByOffice(rawOffice, true);
		FunctionState function = instance.build().getManagedObjectMetaData().recycle(managedObject, cleanup);
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
		final ManagedObjectCleanup cleanup = new ManagedObjectCleanupImpl(null, this.officeMetaData.build());
		final ManagedObject managedObject = this.createMock(ManagedObject.class);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> rawOffice = this.createRawManagingOffice();
		final RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> instance = this
				.createRawBoundManagedObjectInstanceMetaData("MOS", rawOffice);

		// Undertake afterwards
		this.run_manageByOffice(rawOffice, true);
		rawOffice.manageManagedObject(instance.build(), new AssetManagerRegistry(null, null), 1);
		FunctionState function = instance.build().getManagedObjectMetaData().recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure have recycle function
		assertNotNull("Should have recycle function", function);
	}

	/**
	 * Ensure able to not have a recycle {@link ManagedFunction}.
	 */
	public void testNoRecycleFunction() {

		final ManagedObjectCleanup cleanup = new ManagedObjectCleanupImpl(null, this.officeMetaData.build());
		final ManagedObject managedObject = this.createMock(ManagedObject.class);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> rawOffice = this.createRawManagingOffice();
		final RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> instance = this
				.createRawBoundManagedObjectInstanceMetaData("MOS", rawOffice);

		// Ensure not obtain recycle function
		rawOffice.manageManagedObject(instance.build(), this.assetManagerFactory, 1);
		FunctionState function = instance.build().getManagedObjectMetaData().recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure no recycle function
		assertNull("Should be no recycle function", function);
	}

	/**
	 * Ensure issue if can not find start up {@link ManagedFunction}.
	 */
	public void testNullStartupFunctionName() {

		this.startupFunctions.add(new ManagedFunctionInvocationImpl(null, null));
		this.record_issue("Must provide name for start up function 0");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if can not find start up {@link ManagedFunction}.
	 */
	public void testNoStartupFunction() {

		this.startupFunctions.add(new ManagedFunctionInvocationImpl("STARTUP", null));
		this.record_issue("Start up function 'STARTUP' not found");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incompatible parameter type for start up
	 * {@link ManagedFunction}.
	 */
	public void testIncompatibleStartupFunction() {

		String startupFunctionName = "STARTUP";
		this.startupFunctions.add(new ManagedFunctionInvocationImpl(startupFunctionName, "Not Integer"));
		this.officeMetaData.addManagedFunction(startupFunctionName, Integer.class);
		this.record_issue("Incompatible parameter type for startup function (parameter=" + String.class.getName()
				+ ", required type=" + Integer.class.getName() + ", function=" + startupFunctionName + ")");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if attempting to link to unknown start up
	 * {@link ManagedObjectSource} before managing.
	 */
	public void testLinkUnknownBeforeManagedObjectSource() {

		// Record
		String startupFunctionName = "STARTUP";
		this.startupFunctions.add(new ManagedFunctionInvocationImpl(startupFunctionName, null));
		this.officeMetaData.addManagedFunction(startupFunctionName, null);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> rawOffice = this.createRawManagingOffice();
		final RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> instance = this
				.createRawBoundManagedObjectInstanceMetaData("MOS", rawOffice);

		// Have managed before managed by office.
		// This would be the possible case that used by same office.
		rawOffice.manageManagedObject(instance.build(), this.assetManagerFactory, 1); // undertake first
		this.run_manageByOffice(rawOffice, true);
		ManagedObjectStartupFunction[] startupFunctions = instance.build().getManagedObjectMetaData()
				.getStartupFunctions();
		this.verifyMockObjects();

		// Ensure have start up function
		assertEquals("Incorrect number of start up functions", 1, startupFunctions.length);
		assertEquals("Incorrect start up function", startupFunctionName,
				startupFunctions[0].getFlowMetaData().getInitialFunctionMetaData().getFunctionName());
		assertNull("Should be no start up argument", startupFunctions[0].getParameter());
	}

	/**
	 * Ensure able to link the startup {@link ManagedFunction} after managing.
	 */
	public void testLinkStartupFunctionAfterManaging() {

		// Record
		String startupFunctionName = "STARTUP";
		String startupArgument = "ARGUMENT";
		this.startupFunctions.add(new ManagedFunctionInvocationImpl(startupFunctionName, startupArgument));
		this.officeMetaData.addManagedFunction(startupFunctionName, String.class);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> rawOffice = this.createRawManagingOffice();
		final RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> instance = this
				.createRawBoundManagedObjectInstanceMetaData("MOS", rawOffice);

		// Undertake afterwards
		this.run_manageByOffice(rawOffice, true);
		rawOffice.manageManagedObject(instance.build(), new AssetManagerRegistry(null, null), 1);
		ManagedObjectStartupFunction[] startupFunctions = instance.build().getManagedObjectMetaData()
				.getStartupFunctions();
		this.verifyMockObjects();

		// Ensure have start up function
		assertEquals("Incorrect number of start up functions", 1, startupFunctions.length);
		assertEquals("Incorrect start up function", startupFunctionName,
				startupFunctions[0].getFlowMetaData().getInitialFunctionMetaData().getFunctionName());
		assertEquals("Incorrect start up argument", startupArgument, startupFunctions[0].getParameter());
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
	 * Ensures issues if {@link ManagedObject} is not managed by the {@link Office}.
	 */
	public void testNotManagedByOffice() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, null);
		this.record_issue("ManagedObjectSource by input name '" + INPUT_MANAGED_OBJECT_NAME + "' not managed by Office "
				+ MANAGING_OFFICE_NAME);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<Flows> office = this.createRawManagingOffice();
		RawBoundManagedObjectMetaDataMockBuilder<?, ?> rawBoundManagedObjectMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData("BOUND", this.rawMoMetaData.getBuilt());
		this.run_manageByOffice(office, false, rawBoundManagedObjectMetaData.build());
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if no {@link RawBoundManagedObjectInstanceMetaData} for the
	 * {@link ManagedObjectSource}.
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
		ManagedObjectSourceContextImpl<Flows> context = new ManagedObjectSourceContextImpl<Flows>(
				this.getClass().getName(), false, INPUT_MANAGED_OBJECT_NAME, this.configuration, null, null,
				this.rootContext, this.configuration, this.officeMetaData.getBuilder(), new Object());
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
	 * Ensures able to construct {@link Flow} for a {@link ManagedObject} that is
	 * not the first bound or first instance.
	 */
	public void testConstructFlowOfNotFirstBoundOrInstance() throws Exception {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addFlow(null, Flows.KEY);

		// Configure function through managed object source context
		ManagedObjectSourceContextImpl<Flows> context = new ManagedObjectSourceContextImpl<Flows>(
				this.getClass().getName(), false, INPUT_MANAGED_OBJECT_NAME, this.configuration, null, null,
				this.rootContext, this.configuration, this.officeMetaData.getBuilder(), new Object());
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
	 * Ensures issues if {@link ExecutionStrategy} instances configured but no
	 * {@link ExecutionStrategy} instances required.
	 */
	public void testNoExecutionStrategiesButExecutionStrategiesConfigured() {

		// Record flows configured but none required
		this.configuration.linkExecutionStrategy(0, "test");
		this.record_issue(
				"ManagedObjectSourceMetaData specifies no execution strategies but execution strategies configured for it");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ExecutionStrategy} is configured.
	 */
	public void testNoExecutionStrategyConfigured() {

		// Record no execution strategy configured
		this.rawMoMetaData.getMetaDataBuilder().addExecutionStrategy();
		this.record_issue("No execution strategy configured for execution strategy 0 (label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ExecutionStrategy} for the
	 * {@link ExecutionStrategy}.
	 */
	public void testNoExecutionStrategy() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addExecutionStrategy();
		this.configuration.getBuilder().linkExecutionStrategy(0, "UNKNOWN");
		this.record_issue(
				"No execution strategy available by name 'UNKNOWN' for execution strategy 0 (label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if extra {@link ExecutionStrategy} configured.
	 */
	public void testExtraExecutionStrategyConfigured() {

		// Record
		this.rawMoMetaData.getMetaDataBuilder().addExecutionStrategy();
		this.configuration.getBuilder().linkExecutionStrategy(0, "STRATEGY");
		this.configuration.getBuilder().linkExecutionStrategy(1, "STRATEGY");
		this.executionStrategies.put("STRATEGY", new ThreadFactory[0]);
		this.record_issue("Extra execution strategies configured than specified by ManagedObjectSourceMetaData");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link ExecutionStrategy} for a
	 * {@link ManagedObject}.
	 */
	public void testConstructExecutionStrategy() throws Exception {

		// Record
		final ThreadFactory[] threadFactories = new ThreadFactory[0];
		this.rawMoMetaData.getMetaDataBuilder().addExecutionStrategy();
		this.configuration.getBuilder().linkExecutionStrategy(0, "STRATEGY");
		this.executionStrategies.put("STRATEGY", threadFactories);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<?> metaData = this.run_manageByOffice(true, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();

		// Ensure strategy available from execution context
		ManagedObjectExecuteContext<?> context = metaData.getManagedObjectExecuteManagerFactory()
				.createManagedObjectExecuteManager().getManagedObjectExecuteContext();
		assertSame("Incorrect thread factories for execution strategy", threadFactories,
				context.getExecutionStrategy(0));
	}

	/**
	 * Ensures able to construct {@link DefaultExecutive} {@link ExecutionStrategy}
	 * for a {@link ManagedObject}.
	 */
	public void testConstructDefaultExecutionStrategy() throws Exception {

		// Record
		final ThreadFactory[] threadFactories = new ThreadFactory[0];
		this.rawMoMetaData.getMetaDataBuilder().addExecutionStrategy();
		this.defaultExecutionStrategy = threadFactories;

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<?> metaData = this.run_manageByOffice(true, INPUT_MANAGED_OBJECT_NAME);
		this.verifyMockObjects();

		// Ensure strategy available from execution context
		ManagedObjectExecuteContext<?> context = metaData.getManagedObjectExecuteManagerFactory()
				.createManagedObjectExecuteManager().getManagedObjectExecuteContext();
		assertSame("Incorrect thread factories for default execution strategy", threadFactories,
				context.getExecutionStrategy(0));
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription Description of the issue.
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
				this.rawMoMetaData.getManagedObjectSourceMetaData().getFlowMetaData(),
				this.rawMoMetaData.getManagedObjectSourceMetaData().getExecutionMetaData(), this.configuration,
				this.startupFunctions.toArray(new ManagedFunctionInvocation[this.startupFunctions.size()]));
		rawManagingOffice.setRawManagedObjectMetaData(this.rawMoMetaData.build(rawManagingOffice));
		return rawManagingOffice;
	}

	/**
	 * Creates the {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param rawManaingOfficeMetaData {@link RawManagingOfficeMetaData}.
	 * @return {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	private RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> createRawBoundManagedObjectInstanceMetaData(
			String managedObjectSourceName, RawManagingOfficeMetaData<Flows> rawManaingOfficeMetaData) {
		RawBoundManagedObjectMetaDataMockBuilder<Indexed, Flows> rawBoundManagedObjectMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData(managedObjectSourceName, this.rawMoMetaData.getBuilt());
		RawBoundManagedObjectInstanceMetaDataMockBuilder<Indexed, Flows> instance = rawBoundManagedObjectMetaData
				.addRawBoundManagedObjectInstanceMetaData();
		rawBoundManagedObjectMetaData.build();
		instance.build().loadManagedObjectMetaData(AssetType.MANAGED_OBJECT, managedObjectSourceName,
				MockConstruct.mockAssetManagerRegistry(), 1, this.issues);
		return instance;
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaData} and runs the manage by office.
	 *
	 * @param rawManagingOffice      {@link RawManagingOfficeMetaData}.
	 * @param isCreateExecuteContext <code>true</code> if
	 *                               {@link ManagedObjectExecuteContext} should be
	 *                               available.
	 * @param recycleFunctionName    Recycle {@link ManagedFunction} name.
	 * @param processBoundMetaData   {@link ProcessState} bound
	 *                               {@link RawBoundManagedObjectMetaData} for the
	 *                               {@link Office}.
	 */
	private void run_manageByOffice(RawManagingOfficeMetaData<Flows> rawManagingOffice, boolean isCreateExecuteContext,
			RawBoundManagedObjectMetaData... processBoundMetaData) {

		// Manage by office
		ManagedObjectAdministrationMetaDataFactory moAdminFactory = new ManagedObjectAdministrationMetaDataFactory(
				new RawAdministrationMetaDataFactory(this.officeMetaData.build(), null, null, null), null, null);
		rawManagingOffice.manageByOffice(this.officeMetaData.build(), processBoundMetaData, moAdminFactory,
				this.defaultExecutionStrategy, this.executionStrategies, new AssetManagerRegistry(null, null), 1,
				this.issues);

		// Validate creation of execute context
		if (isCreateExecuteContext) {
			assertNotNull("Should have execute context available",
					rawManagingOffice.getManagedObjectExecuteManagerFactory());
		} else {
			assertNull("Execute context should not be available",
					rawManagingOffice.getManagedObjectExecuteManagerFactory());
		}
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaData} and runs the manage by office.
	 * 
	 * @param isCreateExecuteContext    <code>true</code> if
	 *                                  {@link ManagedObjectExecuteContext} should
	 *                                  be available.
	 * @param recycleFunctionName       Recycle {@link ManagedFunction} name.
	 * @param processBoundMetaDataNames Names fo the
	 *                                  {@link RawBoundManagedObjectInstanceMetaData}.
	 * @return New {@link RawManagingOfficeMetaData} with manage by office run.
	 */
	private RawManagingOfficeMetaData<?> run_manageByOffice(boolean isCreateExecuteContext,
			String... processBoundMetaDataNames) {

		// Create and manage by office
		RawManagingOfficeMetaData<Flows> rawManagingOfficeMetaData = this.createRawManagingOffice();

		// Create the process bound managed object instances
		RawBoundManagedObjectMetaData[] managedObjects = new RawBoundManagedObjectMetaData[processBoundMetaDataNames.length];
		for (int i = 0; i < processBoundMetaDataNames.length; i++) {
			managedObjects[i] = this.createRawBoundManagedObjectInstanceMetaData(processBoundMetaDataNames[i],
					rawManagingOfficeMetaData).getRawBoundManagedObjectMetaData().build();
		}

		// Manage by the office
		this.run_manageByOffice(rawManagingOfficeMetaData, isCreateExecuteContext, managedObjects);

		// Return the raw managing office meta-data
		return rawManagingOfficeMetaData;
	}

}
