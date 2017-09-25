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
package net.officefloor.frame.impl.construct.managedfunction;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.EscalationFlowFactory;
import net.officefloor.frame.internal.construct.FlowMetaDataFactory;
import net.officefloor.frame.internal.construct.RawAdministrationMetaData;
import net.officefloor.frame.internal.construct.RawAdministrationMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawManagedFunctionMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawManagedFunctionMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedFunctionMetaDataTest<O extends Enum<O>, F extends Enum<F>> extends OfficeFrameTestCase {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private static final String FUNCTION_NAME = "FUNCTION";

	/**
	 * {@link ManagedFunctionConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionConfiguration<O, F> configuration = this
			.createMock(ManagedFunctionConfiguration.class);

	/**
	 * {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	private final RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory = this
			.createMock(RawBoundManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawAdministrationMetaDataFactory}.
	 */
	private final RawAdministrationMetaDataFactory administrationMetaDataFactory = this
			.createMock(RawAdministrationMetaDataFactory.class);

	/**
	 * {@link FlowMetaDataFactory}.
	 */
	private final FlowMetaDataFactory flowMetaDataFactory = this.createMock(FlowMetaDataFactory.class);

	/**
	 * {@link EscalationFlowFactory}.
	 */
	private final EscalationFlowFactory escalationFlowFactory = this.createMock(EscalationFlowFactory.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionFactory<O, F> functionFactory = this.createMock(ManagedFunctionFactory.class);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaData rawOfficeMetaData = this.createMock(RawOfficeMetaData.class);

	/**
	 * Responsible {@link TeamManagement}.
	 */
	private final TeamManagement responsibleTeam = this.createMock(TeamManagement.class);

	/**
	 * {@link ManagedFunctionLocator} to find the
	 * {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionLocator functionLocator = this.createMock(ManagedFunctionLocator.class);

	/**
	 * {@link Office} scope {@link RawBoundManagedObjectMetaData}.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> officeScopeManagedObjects = new HashMap<>();

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this.createMock(AssetManagerFactory.class);

	/**
	 * {@link TeamManagement} instances by their name within the {@link Office}.
	 */
	private final Map<String, TeamManagement> officeTeams = new HashMap<>();

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this.createMock(OfficeMetaData.class);

	/**
	 * Ensure issue if not {@link ManagedFunction} name.
	 */
	public void testNoFunctionName() {

		// Record no function name
		this.recordReturn(this.configuration, this.configuration.getFunctionName(), null);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getOfficeName(), "OFFICE");
		this.issues.addIssue(AssetType.OFFICE, "OFFICE", "ManagedFunction added without name");

		// Attempt to construct function meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionFactory}.
	 */
	public void testNoFunctionFactory() {

		// Record no task factory
		this.recordReturn(this.configuration, this.configuration.getFunctionName(), FUNCTION_NAME);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionFactory(), null);
		this.record_functionIssue("No ManagedFunctionFactory provided");

		// Attempt to construct function meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to have an annotation.
	 */
	public void testAnnotation() {

		final Object[] ANNOTATIONS = new Object[] { "Annotation" };

		// Record with differentiator
		this.recordReturn(this.configuration, this.configuration.getFunctionName(), FUNCTION_NAME);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionFactory(), this.functionFactory);
		this.recordReturn(this.configuration, this.configuration.getAnnotations(), ANNOTATIONS);
		this.recordReturn(this.configuration, this.configuration.getResponsibleTeamName(), "TEAM");
		Map<String, TeamManagement> teams = new HashMap<String, TeamManagement>();
		teams.put("TEAM", this.responsibleTeam);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getTeams(), teams);
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify annotation
		assertEquals("Incorrect annotation", ANNOTATIONS[0], metaData.getManagedFunctionMetaData().getAnnotations()[0]);
	}

	/**
	 * Ensure able to have no annotation.
	 */
	public void testNoAnnotation() {

		// Record with differentiator
		this.record_nameFactoryTeam(); // no annotation loaded
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify no annotation
		assertEquals("Should have no annotation", 0, metaData.getManagedFunctionMetaData().getAnnotations().length);
	}

	/**
	 * Ensure able to not specify a {@link Team}.
	 */
	public void testNoTeamName() {

		// Record no team name
		this.recordReturn(this.configuration, this.configuration.getFunctionName(), FUNCTION_NAME);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionFactory(), this.functionFactory);
		this.recordReturn(this.configuration, this.configuration.getAnnotations(), new Object[0]);
		this.recordReturn(this.configuration, this.configuration.getResponsibleTeamName(), null);
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();

		// Attempt to construct function meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		assertNull("Should allow any team", metaData.getManagedFunctionMetaData().getResponsibleTeam());
	}

	/**
	 * Ensure issue if unknown {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record unknown team
		this.recordReturn(this.configuration, this.configuration.getFunctionName(), FUNCTION_NAME);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionFactory(), this.functionFactory);
		this.recordReturn(this.configuration, this.configuration.getAnnotations(), new Object[0]);
		this.recordReturn(this.configuration, this.configuration.getResponsibleTeamName(), "TEAM");
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getTeams(), new HashMap<String, Team>());
		this.record_functionIssue("Unknown Team 'TEAM' responsible for ManagedFunction");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure {@link ManagedFunction} name, {@link ManagedFunctionFactory} and
	 * {@link Team} are available.
	 */
	public void testFunctionInitialDetails() {

		// Record initial task details
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify initial raw details
		assertEquals("Incorrect function name", FUNCTION_NAME, metaData.getFunctionName());

		// Verify initial details
		ManagedFunctionMetaData<?, ?> taskMetaData = metaData.getManagedFunctionMetaData();
		assertEquals("Incorrect function name", FUNCTION_NAME, taskMetaData.getFunctionName());
		assertEquals("Incorect function factory", this.functionFactory, taskMetaData.getManagedFunctionFactory());
		assertEquals("No differentiator", 0, taskMetaData.getAnnotations().length);
		assertEquals("Incorrect team", this.responsibleTeam, taskMetaData.getResponsibleTeam());
	}

	/**
	 * Ensure can construct {@link ManagedFunction} with a single
	 * {@link ManagedFunction} bound {@link ManagedObject}.
	 */
	public void testSingleFunctionBoundManagedObject() {

		final RawBoundManagedObjectMetaData rawMoMetaData = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectMetaData<?> moMetaData = this.createMock(ManagedObjectMetaData.class);

		// Record single function bound managed object
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects("MO", rawMoMetaData);
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.recordReturn(rawMoMetaData, rawMoMetaData.getDefaultInstanceIndex(), 0);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData<?>[] { rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData, rawMoInstanceMetaData.getManagedObjectMetaData(), moMetaData);

		// Attempt to construct function meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify function meta-data
		ManagedObjectMetaData<?>[] functionMoMetaData = metaData.getManagedFunctionMetaData()
				.getManagedObjectMetaData();
		assertEquals("Should have a function bound managed object meta-data", 1, functionMoMetaData.length);
		assertEquals("Incorrect managed object meta-data", moMetaData, functionMoMetaData[0]);
	}

	/**
	 * Ensure can construct {@link ManagedFunction} with a {@link Office} scoped
	 * {@link ManagedObject}.
	 */
	public void testSingleOfficeScopedManagedObject() {

		final RawBoundManagedObjectMetaData rawBoundMoMetaData = this.createMock(RawBoundManagedObjectMetaData.class);
		this.officeScopeManagedObjects.put("MO", rawBoundMoMetaData);

		final ManagedFunctionObjectConfiguration<?> objectConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex moIndex = this.createMock(ManagedObjectIndex.class);

		// Record single function bound managed object
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { objectConfiguration });
		this.recordReturn(objectConfiguration, objectConfiguration.getObjectType(), String.class);
		this.recordReturn(objectConfiguration, objectConfiguration.isParameter(), false);
		this.recordReturn(objectConfiguration, objectConfiguration.getScopeManagedObjectName(), "MO");
		this.recordReturn(rawBoundMoMetaData, rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData<?>[] { rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData, rawMoInstanceMetaData.getRawManagedObjectMetaData(), rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getObjectType(), String.class);
		this.recordReturn(rawBoundMoMetaData, rawBoundMoMetaData.getManagedObjectIndex(), moIndex);
		this.recordReturn(rawBoundMoMetaData, rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData<?>[] { rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData, rawMoInstanceMetaData.getDependencies(),
				new RawBoundManagedObjectMetaData[0]);
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();
		this.recordReturn(rawBoundMoMetaData, rawBoundMoMetaData.getManagedObjectIndex(), moIndex);
		this.recordReturn(rawBoundMoMetaData, rawBoundMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData<?>[] { rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData, rawMoInstanceMetaData.getDependencies(),
				new RawBoundManagedObjectMetaData[0]);

		// Attempt to construct function meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify function meta-data
		ManagedObjectMetaData<?>[] functionMoMetaData = metaData.getManagedFunctionMetaData()
				.getManagedObjectMetaData();
		assertEquals("Should not have managed object meta-data as office scoped", 0, functionMoMetaData.length);
		ManagedObjectIndex[] indexes = metaData.getManagedFunctionMetaData().getRequiredManagedObjects();
		assertEquals("Incorrect number of require managed objects", 1, indexes.length);
		assertSame("Incorrect required managed object index", moIndex, indexes[0]);
	}

	/**
	 * Ensure issue if not able to obtain {@link ManagedObjectMetaData}.
	 */
	public void testNoManagedObjectMetaData() {

		final RawBoundManagedObjectMetaData rawMoMetaData = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);

		// Record single function bound managed object
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects("MO", rawMoMetaData);
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.recordReturn(rawMoMetaData, rawMoMetaData.getDefaultInstanceIndex(), 2);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { null, null, rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData, rawMoInstanceMetaData.getManagedObjectMetaData(), null);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getBoundManagedObjectName(), "MO");
		this.record_functionIssue("No managed object meta-data for function managed object MO");

		// Attempt to construct function meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Object} type.
	 */
	public void testNoObjectType() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record no managed object name
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), null);
		this.record_functionIssue("No type for object at index 0");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link in a parameter.
	 */
	public void testParameter() {

		final Class<?> parameterType = Connection.class;
		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record parameter
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), parameterType);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), true);
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		ManagedFunctionMetaData<O, F> metaData = rawMetaData.getManagedFunctionMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0, metaData.getRequiredManagedObjects().length);
		assertEquals("Incorrect parameter type", parameterType, metaData.getParameterType());

		// Ensure can translate to parameter
		ManagedObjectIndex parameterIndex = metaData.getManagedObject(0);
		assertEquals("Incorrect translation to parameter", ManagedFunctionLogicImpl.PARAMETER_INDEX,
				parameterIndex.getIndexOfManagedObjectWithinScope());
		assertNull("Should not have scope for parameter index", parameterIndex.getManagedObjectScope());
	}

	/**
	 * Ensure able to link in the parameter being used twice.
	 */
	public void testParameterUsedTwice() {

		final Class<?> parameterType = Connection.class;

		// Parameter will be Connection but can be passed as Object
		final Class<?> paramTypeOne = Connection.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigOne = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final Class<?> paramTypeTwo = Object.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigTwo = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record parameter used twice
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { paramConfigOne, paramConfigTwo });
		this.recordReturn(paramConfigOne, paramConfigOne.getObjectType(), paramTypeOne);
		this.recordReturn(paramConfigOne, paramConfigOne.isParameter(), true);
		this.recordReturn(paramConfigTwo, paramConfigTwo.getObjectType(), paramTypeTwo);
		this.recordReturn(paramConfigTwo, paramConfigTwo.isParameter(), true);
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		ManagedFunctionMetaData<O, F> metaData = rawMetaData.getManagedFunctionMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0, metaData.getRequiredManagedObjects().length);
		assertEquals("Parameter type should be most specific", parameterType, metaData.getParameterType());

		// Ensure can translate to parameters
		assertEquals("Incorrect translation to parameter one", ManagedFunctionLogicImpl.PARAMETER_INDEX,
				metaData.getManagedObject(0).getIndexOfManagedObjectWithinScope());
		assertEquals("Incorrect translation to parameter two", ManagedFunctionLogicImpl.PARAMETER_INDEX,
				metaData.getManagedObject(1).getIndexOfManagedObjectWithinScope());
	}

	/**
	 * Ensure issue if parameter used twice with incompatible types.
	 */
	public void testIncompatibleParameters() {

		// Parameter types are incompatible
		final Class<?> paramTypeOne = Integer.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigOne = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final Class<?> paramTypeTwo = String.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigTwo = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record parameters incompatible
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { paramConfigOne, paramConfigTwo });
		this.recordReturn(paramConfigOne, paramConfigOne.getObjectType(), paramTypeOne);
		this.recordReturn(paramConfigOne, paramConfigOne.isParameter(), true);
		this.recordReturn(paramConfigTwo, paramConfigTwo.getObjectType(), paramTypeTwo);
		this.recordReturn(paramConfigTwo, paramConfigTwo.isParameter(), true);
		this.record_functionIssue(
				"Incompatible parameter types (" + paramTypeOne.getName() + ", " + paramTypeTwo.getName() + ")");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can handle {@link ManagedObject} not configured for dependency.
	 */
	public void testMissingManagedObject() {

		// Record null managed object configuration
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { null });
		this.record_functionIssue("No object configuration at index 0");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedObject} name.
	 */
	public void testNoScopeManagedObjectName() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record no managed object name
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration, moConfiguration.getScopeManagedObjectName(), null);
		this.record_functionIssue("No name for ManagedObject at index 0");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedObject}.
	 */
	public void testUnknownManagedObject() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record unknown managed object
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration, moConfiguration.getScopeManagedObjectName(), "MO");
		this.record_functionIssue("Can not find scope managed object 'MO'");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue {@link Object} required is incompatible with {@link Object}
	 * from the {@link ManagedObject}.
	 */
	public void testIncompatibleManagedObject() {

		final RawBoundManagedObjectMetaData rawBoundMo = this.createMock(RawBoundManagedObjectMetaData.class);
		this.officeScopeManagedObjects.put("MO", rawBoundMo);

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawWorkMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMo = this.createMock(RawManagedObjectMetaData.class);

		// Record incompatible managed object type
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), Connection.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration, moConfiguration.getScopeManagedObjectName(), "MO");
		this.record_singleInstance_getRawManagedObjectMetaData(rawBoundMo, rawWorkMoInstance, rawMo);
		this.recordReturn(rawMo, rawMo.getObjectType(), Integer.class);
		this.recordReturn(rawWorkMoInstance, rawWorkMoInstance.getRawManagedObjectMetaData(), rawMo);
		this.recordReturn(rawMo, rawMo.getManagedObjectName(), "MANAGED_OBJECT_SOURCE");
		this.record_functionIssue("ManagedObject MO is incompatible (require=" + Connection.class.getName()
				+ ", object of ManagedObject type=" + Integer.class.getName()
				+ ", ManagedObjectSource=MANAGED_OBJECT_SOURCE)");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct with a single {@link ManagedObject} that has no
	 * dependencies or administration.
	 */
	public void testManagedObject() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Work required Managed Object
		final RawBoundManagedObjectMetaData rawMo = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> mo = this.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex moIndex = new ManagedObjectIndexImpl(ManagedObjectScope.THREAD, 0);
		final ManagedObjectMetaData<?> moMetaData = this.createMock(ManagedObjectMetaData.class);

		// Register the managed object
		this.officeScopeManagedObjects.put("MO", rawMo);

		// Record
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects("MO", rawMo);
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration, moConfiguration.getScopeManagedObjectName(), "MO");
		this.record_singleInstance_getRawManagedObjectMetaData(rawMo, rawMoInstance, mo);
		this.recordReturn(mo, mo.getObjectType(), Connection.class);
		LoadDependencies moLinked = new LoadDependencies(rawMo, moIndex, rawMoInstance);
		this.record_loadDependencies(moLinked);
		this.record_NoGovernance();
		this.recordReturn(rawMo, rawMo.getDefaultInstanceIndex(), 0);
		this.recordReturn(rawMo, rawMo.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData<?>[] { rawMoInstance });
		this.recordReturn(rawMoInstance, rawMoInstance.getManagedObjectMetaData(), moMetaData);
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		this.record_dependencySortingForCoordination(moLinked);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure have the required managed object
		ManagedObjectIndex[] requiredManagedObjects = metaData.getManagedFunctionMetaData().getRequiredManagedObjects();
		assertEquals("Incorrect number of managed objects", 1, requiredManagedObjects.length);
		assertEquals("Incorrect required managed object", moIndex, requiredManagedObjects[0]);

		// Ensure can translate
		assertEquals("Incorrect function managed object", moIndex,
				metaData.getManagedFunctionMetaData().getManagedObject(0));
	}

	/**
	 * Ensure able to order {@link ManagedObject} dependencies so that
	 * dependencies come first (required for coordinating).
	 */
	public void testManagedObjectDependencyOrdering() {

		final RawBoundManagedObjectMetaData rawFunctionMo = this.createMock(RawBoundManagedObjectMetaData.class);
		this.officeScopeManagedObjects.put("MO", rawFunctionMo);

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Function required Managed Object
		final RawBoundManagedObjectInstanceMetaData<?> rawFunctionMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> functionMo = this.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex functionMoIndex = new ManagedObjectIndexImpl(ManagedObjectScope.THREAD, 0);

		// Dependency of the function required Managed Object
		final RawBoundManagedObjectMetaData dependencyMo = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> dependencyMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex dependencyMoIndex = new ManagedObjectIndexImpl(ManagedObjectScope.THREAD, 1);

		// Dependency of the dependency Managed Object
		final RawBoundManagedObjectMetaData dependencyDependency = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> dependencyDependencyInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex dependencyDependencyIndex = new ManagedObjectIndexImpl(ManagedObjectScope.PROCESS, 0);

		// Record
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration, moConfiguration.getScopeManagedObjectName(), "MO");
		this.record_singleInstance_getRawManagedObjectMetaData(rawFunctionMo, rawFunctionMoInstance, functionMo);
		this.recordReturn(functionMo, functionMo.getObjectType(), Connection.class);

		// Record loading dependencies
		LoadDependencies dependencyDependencyLinked = new LoadDependencies(dependencyDependency,
				dependencyDependencyIndex, dependencyDependencyInstance);
		LoadDependencies dependencyLinked = new LoadDependencies(dependencyMo, dependencyMoIndex, dependencyMoInstance,
				dependencyDependencyLinked);
		LoadDependencies workLinked = new LoadDependencies(rawFunctionMo, functionMoIndex, rawFunctionMoInstance,
				dependencyLinked);
		this.record_loadDependencies(workLinked);

		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Record sorting required dependencies
		this.record_dependencySortingForCoordination(workLinked, dependencyLinked, dependencyDependencyLinked);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure have managed objects with dependency first
		ManagedObjectIndex[] requiredManagedObjects = metaData.getManagedFunctionMetaData().getRequiredManagedObjects();
		assertEquals("Incorrect number of managed objects", 3, requiredManagedObjects.length);
		assertEquals("Dependency of dependency should be first", dependencyDependencyIndex, requiredManagedObjects[0]);
		assertEquals("Dependency should be after its dependency", dependencyMoIndex, requiredManagedObjects[1]);
		assertEquals("Coordinating should be after its dependency", functionMoIndex, requiredManagedObjects[2]);

		// Ensure can translate
		assertEquals("Incorrect function managed object", functionMoIndex,
				metaData.getManagedFunctionMetaData().getManagedObject(0));
	}

	/**
	 * Ensure issue if there is a cyclic dependency between the required
	 * {@link ManagedObject} instances.
	 */
	public void testManagedObjectCyclicDependency() {

		final RawBoundManagedObjectMetaData rawMoA = this.createMock(RawBoundManagedObjectMetaData.class);
		this.officeScopeManagedObjects.put("MO", rawMoA);

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoAInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> moA = this.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex aIndex = new ManagedObjectIndexImpl(ManagedObjectScope.THREAD, 0);
		final RawBoundManagedObjectMetaData rawMoB = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoBInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex bIndex = new ManagedObjectIndexImpl(ManagedObjectScope.THREAD, 1);

		// Record
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(), Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration, moConfiguration.getScopeManagedObjectName(), "MO");
		this.record_singleInstance_getRawManagedObjectMetaData(rawMoA, rawMoAInstance, moA);
		this.recordReturn(moA, moA.getObjectType(), Connection.class);

		// Record cyclic dependency
		LoadDependencies aLinked = new LoadDependencies(rawMoA, aIndex, rawMoAInstance, (LoadDependencies) null);
		LoadDependencies bLinked = new LoadDependencies(rawMoB, bIndex, rawMoBInstance, aLinked);
		aLinked.dependencies[0] = bLinked; // create cycle
		this.record_loadDependencies(aLinked);

		// Record office meta-data
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Record dependency sorting
		this.record_dependencySortingForCoordination(aLinked, bLinked);
		// Record reporting of cycle
		this.recordReturn(rawMoA, rawMoA.getBoundManagedObjectName(), "MO_A");
		this.recordReturn(rawMoB, rawMoB.getBoundManagedObjectName(), "MO_B");
		this.record_functionIssue("Can not have cyclic dependencies (MO_A, MO_B)");

		// Should not construct task meta-data as cyclic dependency
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle issue with {@link Flow}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testIssuesWithFlows() {

		final FlowConfiguration[] flowConfiguration = new FlowConfiguration[] {
				this.createMock(FlowConfiguration.class) };

		// Record no task node reference
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), flowConfiguration);
		this.recordReturn(this.flowMetaDataFactory, this.flowMetaDataFactory.createFlowMetaData(flowConfiguration,
				this.officeMetaData, AssetType.FUNCTION, FUNCTION_NAME, this.issues), null);

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link Flow}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testConstructFlow() {

		final FlowConfiguration[] flowConfiguration = new FlowConfiguration[] {
				this.createMock(FlowConfiguration.class) };
		final FlowMetaData[] flowMetaData = new FlowMetaData[] { this.createMock(FlowMetaData.class) };

		// Record construct flow
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), flowConfiguration);
		this.recordReturn(this.flowMetaDataFactory, this.flowMetaDataFactory.createFlowMetaData(flowConfiguration,
				this.officeMetaData, AssetType.FUNCTION, FUNCTION_NAME, this.issues), flowMetaData);
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify flow
		FlowMetaData flow = metaData.getManagedFunctionMetaData().getFlow(0);
		assertEquals("Incorrect flow meta-data", flowMetaData[0], flow);
	}

	/**
	 * Ensure issue if no next {@link ManagedFunction} name.
	 */
	public void testNoNextFunctionName() {

		final ManagedFunctionReference taskNodeReference = this.createMock(ManagedFunctionReference.class);

		// Record no next task name
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.record_NoFlows();
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getNextFunction(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getFunctionName(), null);
		this.record_functionIssue("No function name provided for next function");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if argument to next {@link ManagedFunction} is incompatible.
	 */
	public void testIncompatibleNextFunctionArgument() {

		final ManagedFunctionReference taskNodeReference = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> nextTaskMetaData = this.createMock(ManagedFunctionMetaData.class);

		// Record construct next task (which is on another work)
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.record_NoFlows();
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getNextFunction(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getFunctionName(), "NEXT_TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("NEXT_TASK"),
				nextTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference.getArgumentType(), Integer.class);
		this.recordReturn(nextTaskMetaData, nextTaskMetaData.getParameterType(), Connection.class);
		this.record_functionIssue(
				"Argument is not compatible with function parameter (argument=" + Integer.class.getName()
						+ ", parameter=" + Connection.class.getName() + ", function=NEXT_TASK) for next function");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct next {@link ManagedFunctionMetaData}.
	 */
	public void testConstructNextFunction() {

		final ManagedFunctionReference taskNodeReference = this.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?> nextTaskMetaData = this.createMock(ManagedFunctionMetaData.class);

		// Record construct next task (which is on another work)
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.record_NoFlows();
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getNextFunction(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getFunctionName(), "NEXT_TASK");
		this.recordReturn(this.functionLocator, this.functionLocator.getManagedFunctionMetaData("NEXT_TASK"),
				nextTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference.getArgumentType(), null);
		this.recordReturn(nextTaskMetaData, nextTaskMetaData.getParameterType(), null);
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify constructed next task
		ManagedFunctionMetaData<?, ?> nextTask = metaData.getManagedFunctionMetaData().getNextManagedFunctionMetaData();
		assertEquals("Incorrect next task meta-data", nextTaskMetaData, nextTask);
	}

	/**
	 * Ensure handle issue with {@link EscalationFlow}.
	 */
	public void testIssueWithEscalationFlows() {

		final EscalationConfiguration[] escalationConfiguration = new EscalationConfiguration[] {
				this.createMock(EscalationConfiguration.class) };

		// Record no task referenced
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.recordReturn(this.configuration, this.configuration.getEscalations(), escalationConfiguration);
		this.recordReturn(this.escalationFlowFactory, this.escalationFlowFactory.createEscalationFlows(
				escalationConfiguration, this.officeMetaData, AssetType.FUNCTION, FUNCTION_NAME, this.issues), null);

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link EscalationFlow}.
	 */
	public void testConstructEscalation() {

		final EscalationConfiguration[] escalationConfiguration = new EscalationConfiguration[] {
				this.createMock(EscalationConfiguration.class) };
		final EscalationFlow[] escalationFlows = new EscalationFlow[] { this.createMock(EscalationFlow.class) };

		// Record construct escalation
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.recordReturn(this.configuration, this.configuration.getEscalations(), escalationConfiguration);
		this.recordReturn(this.escalationFlowFactory,
				this.escalationFlowFactory.createEscalationFlows(escalationConfiguration, this.officeMetaData,
						AssetType.FUNCTION, FUNCTION_NAME, issues),
				escalationFlows);
		this.record_NoAdministration();

		// Record escalation type
		this.recordReturn(escalationFlows[0], escalationFlows[0].getTypeOfCause(), IOException.class);

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);

		// Verify constructed escalation
		EscalationProcedure escalationProcedure = metaData.getManagedFunctionMetaData().getEscalationProcedure();
		EscalationFlow escalation = escalationProcedure.getEscalation(new IOException("test"));
		assertSame("Incorrect escalation flow", escalationFlows[0], escalation);

		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Governance} name.
	 */
	public void testNoGovernanceName() {

		final ManagedFunctionGovernanceConfiguration gov = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final Map<String, RawGovernanceMetaData<?, ?>> governances = new HashMap<String, RawGovernanceMetaData<?, ?>>();

		// Record construct governance
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();

		// Record configuring governance
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { gov });
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(gov, gov.getGovernanceName(), null);
		this.record_functionIssue("No Governance name provided for Governance 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Governance}.
	 */
	public void testUnknownGovernance() {

		final ManagedFunctionGovernanceConfiguration gov = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final Map<String, RawGovernanceMetaData<?, ?>> governances = new HashMap<String, RawGovernanceMetaData<?, ?>>();

		// Record construct governance
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();

		// Record configuring governance
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { gov });
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(gov, gov.getGovernanceName(), "UNKNOWN");
		this.record_functionIssue("Unknown Governance 'UNKNOWN'");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct required {@link Governance}.
	 */
	public void testGovernance() {

		final ManagedFunctionGovernanceConfiguration govOne = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final RawGovernanceMetaData<?, ?> rawGovOne = this.createMock(RawGovernanceMetaData.class);
		final ManagedFunctionGovernanceConfiguration govTwo = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final RawGovernanceMetaData<?, ?> rawGovTwo = this.createMock(RawGovernanceMetaData.class);
		final Map<String, RawGovernanceMetaData<?, ?>> governances = new HashMap<String, RawGovernanceMetaData<?, ?>>();
		governances.put("GOV_ZERO", this.createMock(RawGovernanceMetaData.class));
		governances.put("GOV_ONE", rawGovOne);
		governances.put("GOV_TWO", this.createMock(RawGovernanceMetaData.class));
		governances.put("GOV_THREE", rawGovTwo);

		// Record construct governance
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();

		// Record configuring governance
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { govOne, govTwo });
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(govOne, govOne.getGovernanceName(), "GOV_ONE");
		this.recordReturn(rawGovOne, rawGovOne.getGovernanceIndex(), 1);
		this.recordReturn(govTwo, govTwo.getGovernanceName(), "GOV_THREE");
		this.recordReturn(rawGovTwo, rawGovTwo.getGovernanceIndex(), 3);

		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure correct required governance
		boolean[] requiredGovernance = metaData.getManagedFunctionMetaData().getRequiredGovernance();
		assertFalse("First governance should not be active", requiredGovernance[0]);
		assertTrue("Second governance should be active", requiredGovernance[1]);
		assertFalse("Third governance should not be active", requiredGovernance[2]);
		assertTrue("Fourth governance should be active", requiredGovernance[3]);
	}

	/**
	 * Flagged to be manual {@link Governance} however {@link Governance}
	 * configured for the {@link ManagedFunction}.
	 */
	public void testGovernanceButFlaggedManual() {

		final ManagedFunctionGovernanceConfiguration gov = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);

		// Record construct governance
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();

		// Record configuring governance
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { gov });
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.isManuallyManageGovernance(), true);
		this.record_functionIssue("Manually manage Governance but Governance configured for OfficeFloor management");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure no required {@link Governance} if flagged for manual
	 * {@link Governance} control.
	 */
	public void testManualGovernance() {

		// Record construct governance
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();

		// Record configuring governance
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[0]);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.isManuallyManageGovernance(), true);

		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.record_NoAdministration();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure manual governance
		boolean[] requiredGovernance = metaData.getManagedFunctionMetaData().getRequiredGovernance();
		assertNull("Should not have governance array when manual", requiredGovernance);
	}

	/**
	 * Ensure able to construct pre {@link Administration}.
	 */
	public void testConstructPreAdministration() {

		final AdministrationConfiguration<?, ?, ?>[] preAdministration = new AdministrationConfiguration[] {
				this.createMock(AdministrationConfiguration.class) };
		final AdministrationConfiguration<?, ?, ?>[] postAdministration = new AdministrationConfiguration[0];
		final RawAdministrationMetaData rawAdministrationMetaData = this.createMock(RawAdministrationMetaData.class);
		final AdministrationMetaData<?, ?, ?> administrationMetaData = this.createMock(AdministrationMetaData.class);
		final Map<String, RawBoundManagedObjectMetaData> scopeMo = new HashMap<>();

		// Record construct function with pre-administration
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_dependencySortingForCoordination();
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.recordReturn(this.configuration, this.configuration.getPreAdministration(), preAdministration);
		this.recordReturn(this.administrationMetaDataFactory,
				this.administrationMetaDataFactory.constructRawAdministrationMetaData(preAdministration,
						AssetType.FUNCTION, FUNCTION_NAME, this.officeMetaData, this.flowMetaDataFactory,
						this.escalationFlowFactory, this.officeTeams, scopeMo, issues),
				new RawAdministrationMetaData[] { rawAdministrationMetaData });
		this.recordReturn(rawAdministrationMetaData, rawAdministrationMetaData.getAdministrationMetaData(),
				administrationMetaData);
		this.recordReturn(rawAdministrationMetaData, rawAdministrationMetaData.getRawBoundManagedObjectMetaData(),
				new RawBoundManagedObjectMetaData[0]);
		this.recordReturn(this.configuration, this.configuration.getPostAdministration(), postAdministration);
		this.recordReturn(this.administrationMetaDataFactory,
				this.administrationMetaDataFactory.constructRawAdministrationMetaData(postAdministration,
						AssetType.FUNCTION, FUNCTION_NAME, this.officeMetaData, this.flowMetaDataFactory,
						this.escalationFlowFactory, this.officeTeams, scopeMo, issues),
				new RawAdministrationMetaData[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		ManagedFunctionMetaData<O, F> functionMetaData = rawMetaData.getManagedFunctionMetaData();
		this.verifyMockObjects();

		// Ensure have administration
		assertEquals("Should have pre administration", 1, functionMetaData.getPreAdministrationMetaData().length);
		AdministrationMetaData<?, ?, ?> adminMetaData = functionMetaData.getPreAdministrationMetaData()[0];
		assertSame("Incorrect administration meta-data", administrationMetaData, adminMetaData);
		assertEquals("Should have post administration", 0, functionMetaData.getPostAdministrationMetaData().length);
	}

	/**
	 * Ensures that dependency of administered {@link ManagedObject} is also
	 * included in required {@link ManagedObjectIndex} instances.
	 */
	public void testPostAdminstrationWithAdministeredManagedObjectDependency() {

		final AdministrationConfiguration<?, ?, ?>[] preAdministration = new AdministrationConfiguration[0];
		final AdministrationConfiguration<?, ?, ?>[] postAdministration = new AdministrationConfiguration[] {
				this.createMock(AdministrationConfiguration.class) };
		final RawAdministrationMetaData rawAdministrationMetaData = this.createMock(RawAdministrationMetaData.class);
		final AdministrationMetaData<?, ?, ?> administrationMetaData = this.createMock(AdministrationMetaData.class);
		final Map<String, RawBoundManagedObjectMetaData> scopeMo = new HashMap<>(this.officeScopeManagedObjects);
		final RawBoundManagedObjectMetaData rawBoundManagedObject = this
				.createMock(RawBoundManagedObjectMetaData.class);

		// Administration required Managed Object
		final RawBoundManagedObjectInstanceMetaData<?> rawAdministrationMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex administrationMoIndex = new ManagedObjectIndexImpl(ManagedObjectScope.THREAD, 0);

		// Dependency of the administration required Managed Object
		final RawBoundManagedObjectMetaData dependencyMo = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> dependencyMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex dependencyMoIndex = new ManagedObjectIndexImpl(ManagedObjectScope.THREAD, 1);

		// Dependency of the dependency Managed Object
		final RawBoundManagedObjectMetaData dependencyDependency = this.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> dependencyDependencyInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex dependencyDependencyIndex = new ManagedObjectIndexImpl(ManagedObjectScope.PROCESS, 0);

		// Record construct task duty association
		this.record_nameFactoryTeam();
		this.record_functionBoundManagedObjects();
		this.record_NoRequiredManagedObjects();
		this.record_NoGovernance();
		this.record_NoFlows();
		this.record_NoNextFunction();
		this.record_NoEscalations();
		this.recordReturn(this.configuration, this.configuration.getPreAdministration(), preAdministration);
		this.recordReturn(this.administrationMetaDataFactory,
				this.administrationMetaDataFactory.constructRawAdministrationMetaData(preAdministration,
						AssetType.FUNCTION, FUNCTION_NAME, this.officeMetaData, this.flowMetaDataFactory,
						this.escalationFlowFactory, this.officeTeams, scopeMo, issues),
				new RawAdministrationMetaData[0]);
		this.recordReturn(this.configuration, this.configuration.getPostAdministration(), postAdministration);
		this.recordReturn(this.administrationMetaDataFactory,
				this.administrationMetaDataFactory.constructRawAdministrationMetaData(postAdministration,
						AssetType.FUNCTION, FUNCTION_NAME, this.officeMetaData, this.flowMetaDataFactory,
						this.escalationFlowFactory, this.officeTeams, scopeMo, issues),
				new RawAdministrationMetaData[] { rawAdministrationMetaData });
		this.recordReturn(rawAdministrationMetaData, rawAdministrationMetaData.getAdministrationMetaData(),
				administrationMetaData);
		this.recordReturn(rawAdministrationMetaData, rawAdministrationMetaData.getRawBoundManagedObjectMetaData(),
				new RawBoundManagedObjectMetaData[] { rawBoundManagedObject });

		// Record loading dependencies
		LoadDependencies dependencyDependencyLinked = new LoadDependencies(dependencyDependency,
				dependencyDependencyIndex, dependencyDependencyInstance);
		LoadDependencies dependencyLinked = new LoadDependencies(dependencyMo, dependencyMoIndex, dependencyMoInstance,
				dependencyDependencyLinked);
		LoadDependencies administrationLinked = new LoadDependencies(rawBoundManagedObject, administrationMoIndex,
				rawAdministrationMoInstance, dependencyLinked);
		this.record_loadDependencies(administrationLinked);
		this.record_dependencySortingForCoordination(administrationLinked, dependencyLinked,
				dependencyDependencyLinked);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<O, F> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		ManagedFunctionMetaData<O, F> functionMetaData = rawMetaData.getManagedFunctionMetaData();
		this.verifyMockObjects();

		// Ensure have administered managed object and dependency as required
		ManagedObjectIndex[] requiredManagedObjects = functionMetaData.getRequiredManagedObjects();
		assertEquals("Administered managed objects should be required", 3, requiredManagedObjects.length);
		assertEquals("Dependency of dependency must be first", dependencyDependencyIndex, requiredManagedObjects[0]);
		assertEquals("Administered dependency must be first", dependencyMoIndex, requiredManagedObjects[1]);
		assertEquals("Administered managed object must be after its dependency", administrationMoIndex,
				requiredManagedObjects[2]);
	}

	/**
	 * Records obtaining {@link ManagedFunction} name,
	 * {@link ManagedFunctionFactory} and responsible {@link Team}.
	 */
	private void record_nameFactoryTeam() {
		this.recordReturn(this.configuration, this.configuration.getFunctionName(), FUNCTION_NAME);
		this.recordReturn(this.configuration, this.configuration.getManagedFunctionFactory(), this.functionFactory);
		this.recordReturn(this.configuration, this.configuration.getAnnotations(), new Object[0]);
		this.recordReturn(this.configuration, this.configuration.getResponsibleTeamName(), "TEAM");
		Map<String, TeamManagement> teams = new HashMap<String, TeamManagement>();
		teams.put("TEAM", this.responsibleTeam);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getTeams(), teams);
	}

	/**
	 * Records bounding {@link ManagedObject} instances to the
	 * {@link ManagedFunction}.
	 * 
	 * @param nameMoPairs
	 *            Name and {@link RawBoundManagedObjectMetaData} pairs.
	 */
	private void record_functionBoundManagedObjects(Object... nameMoPairs) {

		// Obtain the listing of work bound names and managed objects
		int moCount = nameMoPairs.length / 2;
		String[] boundMoNames = new String[moCount];
		RawBoundManagedObjectMetaData[] workBoundMo = new RawBoundManagedObjectMetaData[moCount];
		for (int i = 0; i < nameMoPairs.length; i += 2) {
			int loadIndex = i / 2;
			boundMoNames[loadIndex] = (String) nameMoPairs[i];
			workBoundMo[loadIndex] = (RawBoundManagedObjectMetaData) nameMoPairs[i + 1];
		}

		// Record bounding managed objects to work
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getOfficeScopeManagedObjects(),
				this.officeScopeManagedObjects);
		ManagedObjectConfiguration<?>[] moConfiguration = new ManagedObjectConfiguration[moCount];
		this.recordReturn(this.configuration, this.configuration.getManagedObjectConfiguration(), moConfiguration);
		if (moConfiguration.length > 0) {
			Map<String, RawManagedObjectMetaData<?, ?>> officeRegisteredManagedObjects = new HashMap<String, RawManagedObjectMetaData<?, ?>>(
					0);
			this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getManagedObjectMetaData(),
					officeRegisteredManagedObjects);
			Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData = new HashMap<String, RawGovernanceMetaData<?, ?>>();
			this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getGovernanceMetaData(),
					rawGovernanceMetaData);
			this.recordReturn(this.rawBoundManagedObjectFactory,
					this.rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(moConfiguration, this.issues,
							ManagedObjectScope.FUNCTION, AssetType.FUNCTION, FUNCTION_NAME, this.assetManagerFactory,
							officeRegisteredManagedObjects, this.officeScopeManagedObjects, null, null,
							rawGovernanceMetaData),
					workBoundMo);
			for (int i = 0; i < moCount; i++) {
				this.recordReturn(workBoundMo[i], workBoundMo[i].getBoundManagedObjectName(), boundMoNames[i]);
			}
		}
	}

	/**
	 * Records no {@link ManagedObject} instances required.
	 */
	private void record_NoRequiredManagedObjects() {
		this.recordReturn(this.configuration, this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[0]);
	}

	/**
	 * Records no {@link Administration} {@link AdministrationDuty} instances
	 * for {@link ManagedFunction}.
	 */
	private void record_NoAdministration() {
		final Map<String, RawBoundManagedObjectMetaData> scopeMo = new HashMap<>(this.officeScopeManagedObjects);

		// Record no pre administration
		final AdministrationConfiguration<?, ?, ?>[] preAdministration = new AdministrationConfiguration[0];
		this.recordReturn(this.configuration, this.configuration.getPreAdministration(), preAdministration);
		this.recordReturn(this.administrationMetaDataFactory,
				this.administrationMetaDataFactory.constructRawAdministrationMetaData(preAdministration,
						AssetType.FUNCTION, FUNCTION_NAME, this.officeMetaData, this.flowMetaDataFactory,
						this.escalationFlowFactory, this.officeTeams, scopeMo, issues),
				new RawAdministrationMetaData[0]);

		// Record no post administration
		final AdministrationConfiguration<?, ?, ?>[] postAdministration = new AdministrationConfiguration[0];
		this.recordReturn(this.configuration, this.configuration.getPostAdministration(), postAdministration);
		this.recordReturn(this.administrationMetaDataFactory,
				this.administrationMetaDataFactory.constructRawAdministrationMetaData(postAdministration,
						AssetType.FUNCTION, FUNCTION_NAME, this.officeMetaData, this.flowMetaDataFactory,
						this.escalationFlowFactory, this.officeTeams, scopeMo, issues),
				new RawAdministrationMetaData[0]);
	}

	/**
	 * Records no {@link Governance} instances for the {@link ManagedFunction}.
	 */
	private void record_NoGovernance() {
		final Map<?, ?> governances = this.createMock(Map.class);
		this.recordReturn(this.configuration, this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[0]);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(governances, governances.size(), 0);
	}

	/**
	 * Records obtaining the {@link RawManagedObjectMetaData} for a
	 * {@link RawBoundManagedObjectMetaData} with a single
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 * 
	 * @param rawMo
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param rawInstanceMo
	 *            Single {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param rawMo
	 *            {@link RawManagedObjectMetaData}.
	 */
	private void record_singleInstance_getRawManagedObjectMetaData(RawBoundManagedObjectMetaData rawBoundMo,
			RawBoundManagedObjectInstanceMetaData<?> rawBoundMoInstance, RawManagedObjectMetaData<?, ?> rawMo) {
		this.recordReturn(rawBoundMo, rawBoundMo.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { rawBoundMoInstance });
		this.recordReturn(rawBoundMoInstance, rawBoundMoInstance.getRawManagedObjectMetaData(), rawMo);
	}

	/**
	 * Records sorting the {@link RawBoundManagedObjectMetaData} for
	 * coordinating.
	 * 
	 * @param boundManagedObjects
	 *            Listing of {@link RawBoundManagedObjectMetaData} to be sorted.
	 */
	private void record_dependencySortingForCoordination(LoadDependencies... boundManagedObjects) {
		for (LoadDependencies boundManagedObject : boundManagedObjects) {
			this.record_loadDependencies(boundManagedObject);
		}
	}

	/**
	 * Records loading the dependencies.
	 * 
	 * @param mo
	 *            {@link LoadDependencies}.
	 */
	private void record_loadDependencies(LoadDependencies mo) {
		this.record_loadDependencies(mo, new HashSet<ManagedObjectIndex>());
	}

	/**
	 * Records loading the dependencies.
	 * 
	 * @param mo
	 *            {@link LoadDependencies}.
	 * @param loadedDependencies
	 *            Set of loaded dependencies.
	 */
	private void record_loadDependencies(LoadDependencies mo, Set<ManagedObjectIndex> loadedDependencies) {

		// Record checking whether loaded
		this.recordReturn(mo.boundMo, mo.boundMo.getManagedObjectIndex(), mo.index);
		if (loadedDependencies.contains(mo.index)) {
			// Dependency already loaded
			return;
		}

		// Create the listing of dependencies
		RawBoundManagedObjectMetaData[] dependencies = new RawBoundManagedObjectMetaData[mo.dependencies.length];
		for (int i = 0; i < dependencies.length; i++) {
			dependencies[i] = mo.dependencies[i].boundMo;
		}

		// Record loading the direct dependencies
		this.recordReturn(mo.boundMo, mo.boundMo.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { mo.boundMoInstance });
		this.recordReturn(mo.boundMoInstance, mo.boundMoInstance.getDependencies(), dependencies);

		// Flag the dependency as loaded
		loadedDependencies.add(mo.index);

		// Record loading the transient dependencies
		for (LoadDependencies dependency : mo.dependencies) {
			this.record_loadDependencies(dependency, loadedDependencies);
		}
	}

	/**
	 * Struct to contain details for recording loading dependencies.
	 */
	private static class LoadDependencies {

		public final RawBoundManagedObjectMetaData boundMo;

		public final ManagedObjectIndex index;

		public final RawBoundManagedObjectInstanceMetaData<?> boundMoInstance;

		public final LoadDependencies[] dependencies;

		public LoadDependencies(RawBoundManagedObjectMetaData boundMo, ManagedObjectIndex index,
				RawBoundManagedObjectInstanceMetaData<?> boundMoInstance, LoadDependencies... dependencies) {
			this.boundMo = boundMo;
			this.index = index;
			this.boundMoInstance = boundMoInstance;
			this.dependencies = dependencies;
		}
	}

	/**
	 * Records no {@link Flow}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void record_NoFlows() {
		final FlowConfiguration[] flowConfigurations = new FlowConfiguration[0];
		this.recordReturn(this.configuration, this.configuration.getFlowConfiguration(), flowConfigurations);
		this.recordReturn(this.flowMetaDataFactory, this.flowMetaDataFactory.createFlowMetaData(flowConfigurations,
				this.officeMetaData, AssetType.FUNCTION, FUNCTION_NAME, issues), new FlowMetaData[0]);
	}

	/**
	 * Records no next {@link ManagedFunction}.
	 */
	private void record_NoNextFunction() {
		this.recordReturn(this.officeMetaData, this.officeMetaData.getManagedFunctionLocator(), this.functionLocator);
		this.recordReturn(this.configuration, this.configuration.getNextFunction(), null);
	}

	/**
	 * Records no {@link EscalationFlow}.
	 */
	private void record_NoEscalations() {
		final EscalationConfiguration[] escalationConfigurations = new EscalationConfiguration[0];
		this.recordReturn(this.configuration, this.configuration.getEscalations(), escalationConfigurations);
		this.recordReturn(this.escalationFlowFactory,
				this.escalationFlowFactory.createEscalationFlows(escalationConfigurations, this.officeMetaData,
						AssetType.FUNCTION, FUNCTION_NAME, issues),
				new EscalationFlow[0]);
	}

	/**
	 * Records an issue on the {@link OfficeFloorIssues} about the
	 * {@link ManagedFunction}.
	 * 
	 * @param issueDescription
	 *            Issue description expected.
	 */
	private void record_functionIssue(String issueDescription) {
		this.issues.addIssue(AssetType.FUNCTION, FUNCTION_NAME, issueDescription);
	}

	/**
	 * Constructs the {@link RawManagedFunctionMetaData}.
	 * 
	 * @param isExpectConstruct
	 *            If expected to be constructed.
	 * @return {@link RawManagedFunctionMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private RawManagedFunctionMetaData<O, F> constructRawFunctionMetaData(boolean isExpectConstruct) {

		// Construct the raw function meta-data
		RawManagedFunctionMetaData<?, ?> metaData = RawManagedFunctionMetaDataImpl.getFactory()
				.constructRawManagedFunctionMetaData(this.configuration, this.rawOfficeMetaData,
						this.assetManagerFactory, this.rawBoundManagedObjectFactory, this.issues);
		if (isExpectConstruct) {
			assertNotNull("Expected to construct meta-data", metaData);
		} else {
			assertNull("Not expected to construct meta-data", metaData);
		}

		// Return the meta-data
		return (RawManagedFunctionMetaData<O, F>) metaData;
	}

	/**
	 * Fully constructs the {@link RawManagedFunctionMetaData} by ensuring
	 * remaining state is loaded. Will always expect to construct the
	 * {@link RawManagedFunctionMetaData}.
	 * 
	 * @param isLoad
	 *            Indicates if loaded.
	 * @return {@link RawManagedFunctionMetaData}.
	 */
	private RawManagedFunctionMetaData<O, F> fullyConstructRawFunctionMetaData(boolean isLoad) {

		// Construct the raw function meta-data
		RawManagedFunctionMetaData<O, F> metaData = this.constructRawFunctionMetaData(true);

		// Other functions expected to be constructed between these steps

		// Link the functions and load remaining state to function meta-data
		boolean isLoaded = metaData.loadOfficeMetaData(this.officeMetaData, this.flowMetaDataFactory,
				this.escalationFlowFactory, this.administrationMetaDataFactory, this.officeTeams, this.issues);
		assertEquals("Incorrect load", isLoad, isLoaded);

		// Return the fully constructed meta-data
		return metaData;
	}

}