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

import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.construct.ManagedFunctionLocator;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
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
	@SuppressWarnings("rawtypes")
	private final RawManagedObjectMetaData rawMoMetaData = this.createMock(RawManagedObjectMetaData.class);

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	final ManagedObjectMetaData<?> moMetaData = this.createMock(ManagedObjectMetaData.class);

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeConfiguration<?> configuration = this.createMock(ManagingOfficeConfiguration.class);

	/**
	 * {@link InputManagedObjectConfiguration}.
	 */
	private final InputManagedObjectConfiguration<?> inputConfiguration = this
			.createMock(InputManagedObjectConfiguration.class);

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator functionLocator = this.createMock(ManagedFunctionLocator.class);

	/**
	 * {@link Office} {@link TeamManagement} instances.
	 */
	@SuppressWarnings("unchecked")
	private final Map<String, TeamManagement> officeTeams = this.createMock(Map.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this.createMock(OfficeMetaData.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensure issue if no {@link ManagedFunctionMetaData} for recycle
	 * {@link ManagedFunction}.
	 */
	public void testNoFunctionForRecycleFunction() {

		final String RECYCLE_FUNCTION_NAME = "RECYCLE_FUNCTION";

		// Record no work for recycle function
		this.record_managedObjectSourceName();
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(RECYCLE_FUNCTION_NAME),
				null);
		this.record_issue("Recycle function 'RECYCLE_FUNCTION' not found");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, RECYCLE_FUNCTION_NAME, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incompatible parameter type for recycle
	 * {@link ManagedFunction}.
	 */
	public void testIncompatibleRecycleFunction() {

		final String RECYCLE_FUNCTION_NAME = "RECYCLE_FUNCTION";

		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);

		// Record recycle task has incompatible parameter
		this.record_managedObjectSourceName();
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(RECYCLE_FUNCTION_NAME),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), Integer.class);
		this.record_issue("Incompatible parameter type for recycle function (parameter=" + Integer.class.getName()
				+ ", required type=" + RecycleManagedObjectParameter.class.getName() + ", function="
				+ RECYCLE_FUNCTION_NAME + ")");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, RECYCLE_FUNCTION_NAME, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link the recycle {@link ManagedFunction} before managing.
	 */
	public void testLinkRecycleFunctionBeforeManaging() {

		final String RECYCLE_FUNCTION_NAME = "RECYCLE_FUNCTION";

		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);
		final ManagedObjectMetaDataImpl<?> moMetaData = this.createMoMetaData();
		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final ManagedObjectCleanup cleanup = this.createMock(ManagedObjectCleanup.class);

		// Record manage office
		this.record_managedObjectSourceName();
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData(RECYCLE_FUNCTION_NAME),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), RecycleManagedObjectParameter.class);
		this.record_getFlowConfigurations();

		// Record instigating the recycle flow
		FunctionState recycleFunction = this.record_cleanup(cleanup, taskMetaData, String.class, managedObject, null);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaDataImpl<?> rawOffice = this.createRawManagingOffice(RECYCLE_FUNCTION_NAME);

		// Have managed before managed by office.
		// This would be the possible case that used by same office.
		rawOffice.manageManagedObject(moMetaData);

		rawOffice.manageByOffice(null, this.officeMetaData, this.functionLocator, this.officeTeams, this.issues);
		FunctionState function = moMetaData.recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure correct recycle function
		assertEquals("Incorrect recycle function", recycleFunction, function);
	}

	/**
	 * Ensure able to link the recycle {@link ManagedFunction} after managing.
	 */
	public void testLinkRecycleFunctionAfterManaging() {

		final String RECYCLE_FUNCTION_NAME = "RECYCLE_FUNCTION";

		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);
		final ManagedObjectMetaDataImpl<?> moMetaData = this.createMoMetaData();
		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final ManagedObjectCleanup cleanup = this.createMock(ManagedObjectCleanup.class);

		// Record managed office
		this.record_managedObjectSourceName();
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("RECYCLE_FUNCTION"),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), null);
		this.record_getFlowConfigurations();

		// Record instigating the recycle flow
		final FunctionState recycleFunction = this.record_cleanup(cleanup, taskMetaData, String.class, managedObject,
				null);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaDataImpl<?> rawOffice = this.createRawManagingOffice(RECYCLE_FUNCTION_NAME);
		rawOffice.manageByOffice(null, this.officeMetaData, this.functionLocator, this.officeTeams, this.issues);

		// Have managed after managed by office.
		// This would the be case when used by another office.
		rawOffice.manageManagedObject(moMetaData);
		FunctionState function = moMetaData.recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure correct recycle function
		assertEquals("Incorrect recycle function", recycleFunction, function);
	}

	/**
	 * Ensure able to not have a recycle {@link ManagedFunction}.
	 */
	public void testNoRecycleFunction() {

		final ManagedObjectMetaDataImpl<?> moMetaData = this.createMoMetaData();
		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final ManagedObjectCleanup cleanup = this.createMock(ManagedObjectCleanup.class);

		// Record no recycle task
		this.record_managedObjectSourceName();
		this.record_getFlowConfigurations();
		FunctionState recycleFunction = this.record_cleanup(cleanup, null, String.class, managedObject, null);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaDataImpl<?> rawOffice = this.run_manageByOffice(true, null, null);

		// Ensure not obtain recycle function
		rawOffice.manageManagedObject(moMetaData);
		FunctionState function = moMetaData.recycle(managedObject, cleanup);
		this.verifyMockObjects();

		// Ensure correct recycle function
		assertEquals("Incorrect recycle function", recycleFunction, function);
	}

	/**
	 * Ensures issues if no {@link ProcessState} bound name for
	 * {@link ManagedObject}.
	 */
	public void testNoBoundInputManagedObjectName() {

		final ManagedObjectFlowMetaData<?> moFlowMetaData = this.createMock(ManagedObjectFlowMetaData.class);

		// Record not managed by office
		this.record_managedObjectSourceName();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[0]);
		this.recordReturn(this.inputConfiguration, this.inputConfiguration.getBoundManagedObjectName(), null);
		this.record_issue("ManagedObjectSource invokes flows but does not provide input Managed Object binding name");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, null, moFlowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if {@link ManagedObject} is not managed by the
	 * {@link Office}.
	 */
	public void testNotManagedByOffice() {

		final ManagedObjectFlowMetaData<?> moFlowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final RawBoundManagedObjectMetaData boundMetaData = this.createMock(RawBoundManagedObjectMetaData.class);

		// Record not managed by office
		this.record_managedObjectSourceName();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[0]);
		this.recordReturn(this.inputConfiguration, this.inputConfiguration.getBoundManagedObjectName(),
				INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(boundMetaData, boundMetaData.getBoundManagedObjectName(), "NOT_MATCH");
		this.recordReturn(this.officeMetaData, this.officeMetaData.getOfficeName(), MANAGING_OFFICE_NAME);
		this.record_issue("ManagedObjectSource by input name '" + INPUT_MANAGED_OBJECT_NAME + "' not managed by Office "
				+ MANAGING_OFFICE_NAME);

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, new RawBoundManagedObjectMetaData[] { boundMetaData }, moFlowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if no {@link RawBoundManagedObjectInstanceMetaData} for
	 * the {@link ManagedObjectSource}.
	 */
	public void testNoInstanceForManagedObjectSource() {

		final ManagedObjectFlowMetaData<?> moFlowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final RawBoundManagedObjectMetaData boundMetaData = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this.createMock(RawManagedObjectMetaData.class);

		// Record not managed by office
		this.record_managedObjectSourceName();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[0]);
		this.recordReturn(this.inputConfiguration, this.inputConfiguration.getBoundManagedObjectName(),
				INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(boundMetaData, boundMetaData.getBoundManagedObjectName(), INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(boundMetaData, boundMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { instanceMetaData });
		this.recordReturn(instanceMetaData, instanceMetaData.getRawManagedObjectMetaData(), rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(), "NOT_MATCH");
		this.recordReturn(this.officeMetaData, this.officeMetaData.getOfficeName(), MANAGING_OFFICE_NAME);
		this.record_issue("ManagedObjectSource by input name '" + INPUT_MANAGED_OBJECT_NAME + "' not managed by Office "
				+ MANAGING_OFFICE_NAME);

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, new RawBoundManagedObjectMetaData[] { boundMetaData }, moFlowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if {@link Flow} instances configured but no {@link Flow}
	 * instances required.
	 */
	public void testNoFlowsButFlowsConfigured() {

		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);

		// Record flows configured but none required
		this.record_managedObjectSourceName();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[] { flowConfiguration });
		this.record_issue("ManagedObjectSourceMetaData specifies no flows but flows configured for it");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link Flow} is configured.
	 */
	public void testNoFlowConfigured() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);

		// Record no flow configured
		this.record_managedObjectSourceName();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(), Flows.WRONG_KEY);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.record_issue("No flow configured for flow 0 (key=" + Flows.KEY + ", label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedFunctionMetaData} for the {@link Flow}.
	 */
	public void testNoFlowFunction() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final ManagedFunctionReference taskReference = this.createMock(ManagedFunctionReference.class);

		// Record no flow task
		this.record_managedObjectSourceName();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(), Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), "FLOW");
		this.recordReturn(flowConfiguration, flowConfiguration.getManagedFunctionReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), null);
		this.recordReturn(taskReference, taskReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), null);
		this.record_issue("Can not find function meta-data TASK for flow 0 (key=" + Flows.KEY + ", label=FLOW)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if incompatible argument to {@link flow}
	 * {@link ManagedFunction}.
	 */
	public void testIncompatibleFlowArgument() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final ManagedFunctionReference taskReference = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);

		// Record incompatible argument
		this.record_managedObjectSourceName();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), "");
		this.recordReturn(flowConfiguration, flowConfiguration.getManagedFunctionReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), Integer.class);
		this.recordReturn(taskReference, taskReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), String.class);
		this.record_issue("Argument is not compatible with function parameter (argument=" + Integer.class.getName()
				+ ", parameter=" + String.class.getName()
				+ ", function=TASK) for flow 0 (key=<indexed>, label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if extra {@link Flow} configured.
	 */
	public void testExtraFlowConfigured() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfigurationOne = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final ManagedFunctionReference taskReference = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfigurationTwo = this
				.createMock(ManagedObjectFlowConfiguration.class);

		// Record extra flow configured
		this.record_managedObjectSourceName();
		this.record_getFlowConfigurations(flowConfigurationOne, flowConfigurationTwo);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfigurationOne, flowConfigurationOne.getFlowKey(), Flows.KEY);
		this.recordReturn(flowConfigurationTwo, flowConfigurationTwo.getFlowKey(), Flows.WRONG_KEY);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), "LABEL");
		this.recordReturn(flowConfigurationOne, flowConfigurationOne.getManagedFunctionReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), null);
		this.recordReturn(taskReference, taskReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), null);
		this.record_issue("Extra flows configured than specified by ManagedObjectSourceMetaData");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link Flow} for a {@link ManagedObject} .
	 */
	public void testConstructFlow() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final ManagedFunctionReference taskReference = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> taskMetaData = this.createMock(ManagedFunctionMetaData.class);
		final String parameter = "PARAMETER";
		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final ManagedFunctionContainer function = this.createMock(ManagedFunctionContainer.class);

		// Record construct flow
		this.record_managedObjectSourceName();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.recordReturn(flowConfiguration, flowConfiguration.getManagedFunctionReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), String.class);
		this.recordReturn(taskReference, taskReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"), taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), Object.class);

		// Record invoking the flow
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.createProcess(null, parameter, null, null, managedObject, this.moMetaData, 0),
				function, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						FlowMetaData flowMetaData = (FlowMetaData) actual[0];
						assertEquals("Incorrect parameter", parameter, actual[1]);
						assertEquals("Incorrect managed object", managedObject, actual[4]);
						assertEquals("Incorrect managed object meta-data",
								RawManagingOfficeMetaDataTest.this.moMetaData, actual[5]);
						assertEquals("Incorrect process index", 0, actual[6]);

						// Validate flow meta-data
						assertEquals("Incorrect task meta-data", taskMetaData,
								flowMetaData.getInitialFunctionMetaData());
						assertFalse("Within new process so no need to spawn thread state",
								flowMetaData.isSpawnThreadState());

						// Matches if at this point
						return true;
					}
				});

		// Record activating function (and subsequently process)
		FunctionLoop functionLoop = this.createMock(FunctionLoop.class);
		this.recordReturn(this.officeMetaData, this.officeMetaData.getFunctionLoop(), functionLoop);
		functionLoop.executeFunction(function);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<?> rawOffice = this.run_manageByOffice(true, null, processBoundMetaData,
				flowMetaData);
		rawOffice.getManagedObjectExecuteContextFactory().createManagedObjectExecuteContext().invokeProcess(0,
				parameter, managedObject, 0, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link Flow} for a {@link ManagedObject} that
	 * is not the first bound or first instance.
	 */
	public void testConstructFlowOfNotFirstBoundOrInstance() {

		final int processMoIndex = 2;
		final int instanceIndex = 3;

		final ManagedObjectFlowMetaData<?> flowMetaData = this.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final ManagedFunctionReference functionReference = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> functionMetaData = this.createMock(ManagedFunctionMetaData.class);
		final String parameter = "PARAMETER";
		final ManagedObject managedObject = this.createMock(ManagedObject.class);
		final ManagedFunctionContainer function = this.createMock(ManagedFunctionContainer.class);

		// Record construct flow
		this.record_managedObjectSourceName();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this.record_bindToProcess(instanceIndex,
				"NOT_MATCH_INDEX_0", "NOT_MATCH_INDEX_1", INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.recordReturn(flowConfiguration, flowConfiguration.getManagedFunctionReference(), functionReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), String.class);
		this.recordReturn(functionReference, functionReference.getFunctionName(), "TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("TASK"),
				functionMetaData);
		this.recordReturn(functionMetaData, functionMetaData.getParameterType(), Object.class);

		// Record invoking the flow
		this.recordReturn(this.officeMetaData, this.officeMetaData.createProcess(null, parameter, null, null,
				managedObject, this.moMetaData, processMoIndex), function, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						FlowMetaData flowMetaData = (FlowMetaData) actual[0];
						assertEquals("Incorrect parameter", parameter, actual[1]);
						assertNull("Should not have escalation handler", actual[2]);
						assertEquals("Incorrect managed object", managedObject, actual[4]);
						assertEquals("Incorrect managed object meta-data",
								RawManagingOfficeMetaDataTest.this.moMetaData, actual[5]);
						assertEquals("Incorrect process index", processMoIndex, actual[6]);

						// Validate flow meta-data
						assertEquals("Incorrect function meta-data", functionMetaData,
								flowMetaData.getInitialFunctionMetaData());
						assertFalse("In process, so no need to spawn thread state", flowMetaData.isSpawnThreadState());

						// Matches if at this point
						return true;
					}
				});

		// Record activating function (and subsequently process)
		FunctionLoop functionLoop = this.createMock(FunctionLoop.class);
		this.recordReturn(this.officeMetaData, this.officeMetaData.getFunctionLoop(), functionLoop);
		functionLoop.executeFunction(function);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<?> rawOffice = this.run_manageByOffice(true, null, processBoundMetaData,
				flowMetaData);
		rawOffice.getManagedObjectExecuteContextFactory().createManagedObjectExecuteContext().invokeProcess(0,
				parameter, managedObject, 0, null);
		this.verifyMockObjects();
	}

	/**
	 * {@link Flow} key.
	 */
	private enum Flows {
		KEY, WRONG_KEY
	}

	/**
	 * Records obtaining the {@link ManagedObjectSource} name.
	 */
	private void record_managedObjectSourceName() {
		this.recordReturn(this.rawMoMetaData, this.rawMoMetaData.getManagedObjectName(), MANAGED_OBJECT_SOURCE_NAME);
	}

	/**
	 * Records clean up of the {@link ManagedObject}.
	 * 
	 * @param cleanup
	 *            {@link ManagedObjectCleanup}.
	 * @param recycleFunctionMetaData
	 *            {@link ManagedFunctionMetaData} for the recycle
	 *            {@link FunctionState}.
	 * @param managedObject
	 *            {@link ManagedObject} to be recycled.
	 * @param pool
	 *            {@link ManagedObjectPool}.
	 * @return Recycle {@link FunctionState} created.
	 */
	private FunctionState record_cleanup(ManagedObjectCleanup cleanup,
			final ManagedFunctionMetaData<?, ?> recycleFunctionMetaData, final Class<?> objectType,
			final ManagedObject managedObject, final ManagedObjectPool pool) {

		final FunctionState recycleFunction = this.createMock(FunctionState.class);

		this.recordReturn(cleanup, cleanup.cleanup(null, objectType, managedObject, pool), recycleFunction,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {

						// Ensure correct flow meta-data
						FlowMetaData recycleMetaData = (FlowMetaData) actual[0];
						if (recycleFunctionMetaData == null) {
							assertNull("Should not have recycle function", recycleMetaData);
						} else {
							assertSame("Incorrect recycle function meta-data", recycleFunctionMetaData,
									recycleMetaData.getInitialFunctionMetaData());
						}

						// Validate remaining arguments
						assertEquals("Incorrect object type", expected[1], actual[1]);
						assertEquals("Incorrect managed object", expected[2], actual[2]);
						assertEquals("Incorrect managed object pool", expected[3], actual[3]);
						return true;
					}
				});

		// Return the recycle function
		return recycleFunction;
	}

	/**
	 * Records obtaining the {@link ManagedObjectFlowConfiguration} instances.
	 * 
	 * @param flowConfigurations
	 *            {@link ManagedObjectFlowConfiguration} instances.
	 */
	private void record_getFlowConfigurations(ManagedObjectFlowConfiguration<?>... flowConfigurations) {
		// Record obtaining the flow configuration
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), flowConfigurations);
	}

	/**
	 * Records obtaining the {@link ProcessState} bound index for the
	 * {@link ManagedObject}.
	 * 
	 * @param flowConfigurations
	 *            {@link ManagedObjectFlowConfiguration} instances.
	 */
	private RawBoundManagedObjectMetaData[] record_bindToProcess(int instanceIndex, String... processBoundNames) {

		RawBoundManagedObjectMetaData[] processBoundMetaDatas = new RawBoundManagedObjectMetaData[processBoundNames.length];
		for (int i = 0; i < processBoundMetaDatas.length; i++) {
			processBoundMetaDatas[i] = this.createMock(RawBoundManagedObjectMetaData.class);
		}

		RawBoundManagedObjectInstanceMetaData<?>[] instanceMetaDatas = new RawBoundManagedObjectInstanceMetaData[instanceIndex
				+ 1];
		RawManagedObjectMetaData<?, ?>[] rawMoMetaDatas = new RawManagedObjectMetaData[instanceMetaDatas.length];
		for (int i = 0; i < instanceMetaDatas.length; i++) {
			instanceMetaDatas[i] = this.createMock(RawBoundManagedObjectInstanceMetaData.class);
			rawMoMetaDatas[i] = this.createMock(RawManagedObjectMetaData.class);
		}

		// Record obtaining the process index and meta-data
		this.recordReturn(this.inputConfiguration, this.inputConfiguration.getBoundManagedObjectName(),
				INPUT_MANAGED_OBJECT_NAME);
		for (int b = 0; b < processBoundNames.length; b++) {
			String processBoundName = processBoundNames[b];
			RawBoundManagedObjectMetaData processBoundMetaData = processBoundMetaDatas[b];
			this.recordReturn(processBoundMetaData, processBoundMetaData.getBoundManagedObjectName(), processBoundName);
			if (INPUT_MANAGED_OBJECT_NAME.equals(processBoundName)) {
				this.recordReturn(processBoundMetaData, processBoundMetaData.getRawBoundManagedObjectInstanceMetaData(),
						instanceMetaDatas);
				for (int i = 0; i < instanceMetaDatas.length; i++) {
					RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = instanceMetaDatas[i];
					RawManagedObjectMetaData<?, ?> rawMoMetaData = rawMoMetaDatas[i];
					this.recordReturn(instanceMetaData, instanceMetaData.getRawManagedObjectMetaData(), rawMoMetaData);
					if (i != instanceIndex) {
						this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(), "NOT_MATCH");
					} else {
						this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(),
								MANAGED_OBJECT_SOURCE_NAME);
						this.recordReturn(instanceMetaData, instanceMetaData.getManagedObjectMetaData(),
								this.moMetaData);
					}
				}
			}
		}

		// Return the process bound meta-data
		return processBoundMetaDatas;
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
	 * Creates the {@link RawManagingOfficeMetaDataImpl} for testing.
	 * 
	 * @param recycleFunctionName
	 *            Recycle {@link ManagedFunction} name.
	 * @param flowMetaData
	 *            {@link ManagedObjectFlowMetaData} listing.
	 * @return New {@link RawManagingOfficeMetaDataImpl}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RawManagingOfficeMetaDataImpl<?> createRawManagingOffice(String recycleFunctionName,
			ManagedObjectFlowMetaData<?>... flowMetaData) {
		// Create and return the raw managing office meta-data
		RawManagingOfficeMetaDataImpl<?> rawManagingOffice = new RawManagingOfficeMetaDataImpl(MANAGING_OFFICE_NAME,
				recycleFunctionName, this.inputConfiguration, flowMetaData, this.configuration);
		rawManagingOffice.setRawManagedObjectMetaData(this.rawMoMetaData);
		return rawManagingOffice;
	}

	/**
	 * Creates a {@link ManagedObjectMetaDataImpl} for use in testing.
	 * 
	 * @return {@link ManagedObjectMetaDataImpl}.
	 */
	private ManagedObjectMetaDataImpl<?> createMoMetaData() {
		return new ManagedObjectMetaDataImpl<None>("BOUND", String.class, -1, null, null, false, null, false, null,
				false, null, 0, null);
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaDataImpl} and runs the manage by
	 * office.
	 * 
	 * @param isCreateExecuteContext
	 *            <code>true</code> if {@link ManagedObjectExecuteContext}
	 *            should be available.
	 * @param recycleFunctionName
	 *            Recycle {@link ManagedFunction} name.
	 * @param processBoundMetaData
	 *            {@link ProcessState} bound
	 *            {@link RawBoundManagedObjectMetaData} for the {@link Office}.
	 * @param flowMetaData
	 *            {@link ManagedObjectFlowMetaData} listing.
	 * @return New {@link RawManagingOfficeMetaDataImpl} with manage by office
	 *         run.
	 */
	private RawManagingOfficeMetaDataImpl<?> run_manageByOffice(boolean isCreateExecuteContext,
			String recycleFunctionName, RawBoundManagedObjectMetaData[] processBoundMetaData,
			ManagedObjectFlowMetaData<?>... flowMetaData) {

		// Create and manage by office
		RawManagingOfficeMetaDataImpl<?> rawOffice = this.createRawManagingOffice(recycleFunctionName, flowMetaData);
		rawOffice.manageByOffice(processBoundMetaData, this.officeMetaData, this.functionLocator, this.officeTeams,
				this.issues);

		// Validate creation of execute context
		if (isCreateExecuteContext) {
			assertNotNull("Should have execute context available", rawOffice.getManagedObjectExecuteContextFactory());
		} else {
			assertNull("Execute context should not be available", rawOffice.getManagedObjectExecuteContextFactory());
		}

		// Return the raw managing office meta-data
		return rawOffice;
	}

}