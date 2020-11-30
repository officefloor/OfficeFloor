/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.managedfunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectInstanceMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawOfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawManagedFunctionMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedFunctionMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private static final String FUNCTION_NAME = "FUNCTION";

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	private static final ManagedFunctionFactory<Indexed, Indexed> FUNCTION_FACTORY = () -> null;

	/**
	 * {@link ManagedFunctionConfiguration}.
	 */
	private ManagedFunctionBuilderImpl<Indexed, Indexed> configuration = new ManagedFunctionBuilderImpl<>(FUNCTION_NAME,
			FUNCTION_FACTORY);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaDataMockBuilder rawOfficeMetaData = MockConstruct.mockRawOfficeMetaData("OFFICE");

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaDataMockBuilder officeMetaData = MockConstruct.mockOfficeMetaData("OFFICE");

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Initiate.
	 */
	public RawManagedFunctionMetaDataTest() {
		// Provide OfficeFloor meta-data to obtain Executor
		this.rawOfficeMetaData.setRawOfficeFloorMetaData(MockConstruct.mockRawOfficeFloorMetaData().build());
	}

	/**
	 * Ensure issue if not {@link ManagedFunction} name.
	 */
	public void testNoFunctionName() {

		// Record no function name
		this.configuration = new ManagedFunctionBuilderImpl<>(null, null);
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

		// Record no function factory
		this.configuration = new ManagedFunctionBuilderImpl<>(FUNCTION_NAME, null);
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

		final Object ANNOTATION = "Annotation";
		this.configuration.addAnnotation(ANNOTATION);

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify annotation
		assertEquals("Incorrect annotation", ANNOTATION, metaData.getManagedFunctionMetaData().getAnnotations()[0]);
	}

	/**
	 * Ensure able to have no annotation.
	 */
	public void testNoAnnotation() {

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify no annotation
		assertEquals("Should have no annotation", 0, metaData.getManagedFunctionMetaData().getAnnotations().length);
	}

	/**
	 * Ensure able to not specify a {@link Team}.
	 */
	public void testNoTeamName() {

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		assertNull("Should allow any team", metaData.getManagedFunctionMetaData().getResponsibleTeam());
	}

	/**
	 * Ensure issue if unknown {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record unknown team
		this.configuration.setResponsibleTeam("TEAM");
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

		// Record
		this.configuration.setResponsibleTeam("TEAM");
		TeamManagement responsibleTeam = this.rawOfficeMetaData.addTeam("TEAM");

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify initial raw details
		assertEquals("Incorrect function name", FUNCTION_NAME, metaData.getFunctionName());

		// Verify initial details
		ManagedFunctionMetaData<?, ?> functionMetaData = metaData.getManagedFunctionMetaData();
		assertEquals("Incorrect function name", FUNCTION_NAME, functionMetaData.getFunctionName());
		assertEquals("Incorect function factory", FUNCTION_FACTORY, functionMetaData.getManagedFunctionFactory());
		assertEquals("No differentiator", 0, functionMetaData.getAnnotations().length);
		assertEquals("Incorrect team", responsibleTeam, functionMetaData.getResponsibleTeam());
	}

	/**
	 * Ensure can construct {@link ManagedFunction} with a single
	 * {@link ManagedFunction} bound {@link ManagedObject}.
	 */
	public void testSingleFunctionBoundManagedObject() {

		// Record
		this.configuration.addManagedObject("OBJECT", "MO");
		RawManagedObjectMetaDataMockBuilder<?, ?> rawManagedObject = this.rawOfficeMetaData
				.addRegisteredManagedObject("MO");

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.constructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify function meta-data
		ManagedObjectMetaData<?>[] functionMoMetaData = metaData.getManagedFunctionMetaData()
				.getManagedObjectMetaData();
		assertEquals("Should have a function bound managed object meta-data", 1, functionMoMetaData.length);
		assertEquals("Incorrect managed object meta-data", "OBJECT", functionMoMetaData[0].getBoundManagedObjectName());
		assertSame("Incorrect managed object source", rawManagedObject.getManagedObjectSource(),
				functionMoMetaData[0].getManagedObjectSource());
	}

	/**
	 * Ensure can construct {@link ManagedFunction} with a {@link Office} scoped
	 * {@link ManagedObject}.
	 */
	public void testSingleOfficeScopedManagedObject() {

		// Record
		this.configuration.linkManagedObject(0, "MO", Object.class);
		RawBoundManagedObjectMetaDataMockBuilder<?, ?> bound = this.rawOfficeMetaData.addScopeBoundManagedObject("MO")
				.getRawBoundManagedObjectMetaData();

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify function meta-data
		ManagedObjectMetaData<?>[] functionMoMetaData = metaData.getManagedFunctionMetaData()
				.getManagedObjectMetaData();
		assertEquals("Should not have managed object meta-data as office scoped", 0, functionMoMetaData.length);
		ManagedObjectIndex[] indexes = metaData.getManagedFunctionMetaData().getRequiredManagedObjects();
		assertEquals("Incorrect number of require managed objects", 1, indexes.length);
		assertSame("Incorrect required managed object index", bound.build().getManagedObjectIndex(), indexes[0]);
	}

	/**
	 * Ensure issue if no {@link Object} type.
	 */
	public void testNoObjectType() {

		// Record
		this.configuration.linkManagedObject(0, "MO", null);
		this.record_functionIssue("No type for object at index 0");

		// Construct
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link in a parameter.
	 */
	public void testParameter() {

		// Record
		this.configuration.linkParameter(0, String.class);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		ManagedFunctionMetaData<Indexed, Indexed> metaData = rawMetaData.getManagedFunctionMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0, metaData.getRequiredManagedObjects().length);
		assertEquals("Incorrect parameter type", String.class, metaData.getParameterType());

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

		// Record
		this.configuration.linkParameter(0, String.class);
		this.configuration.linkParameter(1, String.class);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		ManagedFunctionMetaData<Indexed, Indexed> metaData = rawMetaData.getManagedFunctionMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0, metaData.getRequiredManagedObjects().length);
		assertEquals("Parameter type should be most specific", String.class, metaData.getParameterType());

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

		// Record
		this.configuration.linkParameter(0, Integer.class);
		this.configuration.linkParameter(1, String.class);
		this.record_functionIssue(
				"Incompatible parameter types (" + Integer.class.getName() + ", " + String.class.getName() + ")");

		// Construct
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedObject} name.
	 */
	public void testNoScopeManagedObjectName() {

		// Record
		this.configuration.linkManagedObject(0, null, Object.class);
		this.record_functionIssue("No name for ManagedObject at index 0");

		// Construct
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedObject}.
	 */
	public void testUnknownManagedObject() {

		// Record
		this.configuration.linkManagedObject(0, "MO", Object.class);
		this.record_functionIssue("Can not find scope managed object 'MO'");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue {@link Object} required is incompatible with {@link Object} from
	 * the {@link ManagedObject}.
	 */
	public void testIncompatibleManagedObject() {

		// Record
		this.configuration.linkManagedObject(0, "MO", String.class);
		this.rawOfficeMetaData.addScopeBoundManagedObject("MO").getRawBoundManagedObjectMetaData()
				.getRawManagedObjectBuilder().getMetaDataBuilder().setObjectClass(Integer.class);
		this.record_functionIssue("ManagedObject MO is incompatible (require=" + String.class.getName()
				+ ", object of ManagedObject type=" + Integer.class.getName() + ", ManagedObjectSource=MO)");

		// Construct
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to order {@link ManagedObject} dependencies so that dependencies
	 * come first (required for coordinating).
	 */
	public void testManagedObjectDependencyOrdering() {

		this.configuration.linkManagedObject(0, "THREE", Object.class);

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> one = this.rawOfficeMetaData
				.addScopeBoundManagedObject("ONE");

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> two = this.rawOfficeMetaData
				.addScopeBoundManagedObject("TWO");
		two.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addDependency(Object.class, null);
		two.getBuilder().mapDependency(0, "ONE");

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> three = this.rawOfficeMetaData
				.addScopeBoundManagedObject("THREE");
		three.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addDependency(Object.class, null);
		three.getBuilder().mapDependency(0, "TWO");

		// Construct
		this.replayMockObjects();

		// Load the dependencies
		one.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());
		two.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());
		three.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());

		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure have managed objects with dependency first
		ManagedObjectIndex[] requiredManagedObjects = metaData.getManagedFunctionMetaData().getRequiredManagedObjects();
		assertEquals("Incorrect number of managed objects", 3, requiredManagedObjects.length);
		assertSame("Dependency of dependency should be first",
				one.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(), requiredManagedObjects[0]);
		assertSame("Dependency should be after its dependency",
				two.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(), requiredManagedObjects[1]);
		assertSame("Coordinating should be after its dependency",
				three.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(), requiredManagedObjects[2]);

		// Ensure can translate
		assertSame("Incorrect function managed object",
				three.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(),
				metaData.getManagedFunctionMetaData().getManagedObject(0));
	}

	/**
	 * Ensure issue if there is a cyclic dependency between the required
	 * {@link ManagedObject} instances.
	 */
	public void testManagedObjectCyclicDependency() {

		this.configuration.linkManagedObject(0, "ONE", Object.class);

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> one = this.rawOfficeMetaData
				.addScopeBoundManagedObject("ONE");
		one.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addDependency(Object.class, null);
		one.getBuilder().mapDependency(0, "TWO");

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> two = this.rawOfficeMetaData
				.addScopeBoundManagedObject("TWO");
		two.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addDependency(Object.class, null);
		two.getBuilder().mapDependency(0, "ONE");

		this.record_functionIssue("Can not have cyclic dependencies (ONE, TWO)");

		// Construct
		this.replayMockObjects();

		one.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());
		two.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());

		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle issue with {@link Flow}.
	 */
	public void testIssuesWithFlows() {

		// Record
		this.configuration.linkFlow(0, "UNKNOWN", null, false);
		this.record_functionIssue("Can not find function meta-data UNKNOWN for flow index 0");

		// Construct
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link Flow}.
	 */
	public void testConstructFlow() {

		// Record
		this.configuration.linkFlow(0, "LINKED", null, false);
		ManagedFunctionMetaData<?, ?> flowMetaData = this.officeMetaData.addManagedFunction("LINKED", null);

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify flow
		FlowMetaData flow = metaData.getManagedFunctionMetaData().getFlow(0);
		assertSame("Incorrect flow meta-data", flowMetaData, flow.getInitialFunctionMetaData());
	}

	/**
	 * Ensure issue if no next {@link ManagedFunction} name.
	 */
	public void testNoNextFunctionName() {

		// Record
		this.configuration.setNextFunction(null, Object.class);
		this.record_functionIssue("No function name provided for next function");

		// Construct
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if argument to next {@link ManagedFunction} is incompatible.
	 */
	public void testIncompatibleNextFunctionArgument() {

		// Record
		this.configuration.setNextFunction("NEXT", Integer.class);
		this.officeMetaData.addManagedFunction("NEXT", String.class);
		this.record_functionIssue(
				"Argument is not compatible with function parameter (argument=" + Integer.class.getName()
						+ ", parameter=" + String.class.getName() + ", function=NEXT) for next function");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct next {@link ManagedFunctionMetaData}.
	 */
	public void testConstructNextFunction() {

		// Record
		this.configuration.setNextFunction("NEXT", null);
		ManagedFunctionMetaData<?, ?> next = this.officeMetaData.addManagedFunction("NEXT", null);

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Verify constructed next function
		ManagedFunctionMetaData<?, ?> nextFunction = metaData.getManagedFunctionMetaData()
				.getNextManagedFunctionMetaData();
		assertEquals("Incorrect next function meta-data", next, nextFunction);
	}

	/**
	 * Ensure handle issue with {@link EscalationFlow}.
	 */
	public void testIssueWithEscalationFlows() {

		// Record
		this.configuration.addEscalation(Throwable.class, "HANDLER");
		this.record_functionIssue("Can not find function meta-data HANDLER for escalation index 0");

		// Construct
		this.replayMockObjects();
		this.fullyConstructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link EscalationFlow}.
	 */
	public void testConstructEscalation() {

		// Record
		this.configuration.addEscalation(Throwable.class, "HANDLER");
		ManagedFunctionMetaData<?, ?> handler = this.officeMetaData.addManagedFunction("HANDLER", Throwable.class);

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.fullyConstructRawFunctionMetaData(true);

		// Verify constructed escalation
		EscalationProcedure escalationProcedure = metaData.getManagedFunctionMetaData().getEscalationProcedure();
		EscalationFlow escalation = escalationProcedure.getEscalation(new IOException("test"));
		assertSame("Incorrect escalation flow", handler, escalation.getManagedFunctionMetaData());

		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Governance} name.
	 */
	public void testNoGovernanceName() {

		// Record
		this.configuration.addGovernance(null);
		this.record_functionIssue("No Governance name provided for Governance 0");

		// Construct
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Governance}.
	 */
	public void testUnknownGovernance() {

		// Record
		this.configuration.addGovernance("UNKNOWN");
		this.record_functionIssue("Unknown Governance 'UNKNOWN'");

		// Construct
		this.replayMockObjects();
		this.constructRawFunctionMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct required {@link Governance}.
	 */
	public void testGovernance() {

		// Record
		this.configuration.addGovernance("TWO");
		this.configuration.addGovernance("FOUR");
		this.rawOfficeMetaData.addGovernance("ONE", Object.class);
		this.rawOfficeMetaData.addGovernance("TWO", Object.class);
		this.rawOfficeMetaData.addGovernance("THREE", Object.class);
		this.rawOfficeMetaData.addGovernance("FOUR", Object.class);

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure correct required governance
		boolean[] requiredGovernance = metaData.getManagedFunctionMetaData().getRequiredGovernance();
		assertEquals("Incorrect number of governances", 4, requiredGovernance.length);
		assertFalse("First governance should not be active", requiredGovernance[0]);
		assertTrue("Second governance should be active", requiredGovernance[1]);
		assertFalse("Third governance should not be active", requiredGovernance[2]);
		assertTrue("Fourth governance should be active", requiredGovernance[3]);
	}

	/**
	 * Flagged to be manual {@link Governance} however {@link Governance} configured
	 * for the {@link ManagedFunction}.
	 */
	public void testGovernanceButFlaggedManual() {

		// Record
		this.rawOfficeMetaData.manualGovernance();
		this.configuration.addGovernance("GOVERNED");
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

		// Record
		this.rawOfficeMetaData.manualGovernance();

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure manual governance
		boolean[] requiredGovernance = metaData.getManagedFunctionMetaData().getRequiredGovernance();
		assertNull("Should not have governance array when manual", requiredGovernance);
	}

	/**
	 * Ensure able to construct pre {@link Administration}.
	 */
	public void testConstructPreAdministration() {

		// Record
		this.configuration.preAdminister("ADMIN", Object.class, () -> null);

		// Construct
		this.replayMockObjects();
		RawManagedFunctionMetaData<Indexed, Indexed> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		ManagedFunctionMetaData<Indexed, Indexed> functionMetaData = rawMetaData.getManagedFunctionMetaData();
		this.verifyMockObjects();

		// Ensure have administration
		assertEquals("Should have pre administration", 1, functionMetaData.getPreAdministrationMetaData().length);
		ManagedFunctionAdministrationMetaData<?, ?, ?> adminMetaData = functionMetaData
				.getPreAdministrationMetaData()[0];
		assertEquals("Incorrect administration meta-data", "ADMIN",
				adminMetaData.getAdministrationMetaData().getAdministrationName());
		assertEquals("Incorrect logger name", "FUNCTION.pre.ADMIN", adminMetaData.getLogger().getName());
		assertEquals("Should not have post administration", 0, functionMetaData.getPostAdministrationMetaData().length);
	}

	/**
	 * Ensures that dependency of administered {@link ManagedObject} is also
	 * included in required {@link ManagedObjectIndex} instances.
	 */
	public void testPostAdminstrationWithAdministeredManagedObjectDependency() {

		// Record
		this.configuration.linkManagedObject(0, "ONE", Object.class);

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> one = this.rawOfficeMetaData
				.addScopeBoundManagedObject("ONE");
		one.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addDependency(Object.class, null);
		one.getBuilder().mapDependency(0, "FOUR");

		this.configuration.postAdminister("ADMIN", Object.class, () -> null).administerManagedObject("TWO");

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> two = this.rawOfficeMetaData
				.addScopeBoundManagedObject("TWO");
		two.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addDependency(Object.class, null);
		two.getBuilder().mapDependency(0, "THREE");
		two.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addExtension(Object.class, (mo) -> mo);

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> three = this.rawOfficeMetaData
				.addScopeBoundManagedObject("THREE");
		three.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addDependency(Object.class, null);
		three.getBuilder().mapDependency(0, "FOUR");

		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> four = this.rawOfficeMetaData
				.addScopeBoundManagedObject("FOUR");

		// Construct
		this.replayMockObjects();

		one.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());
		two.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());
		three.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());
		four.build().loadDependencies(this.issues, this.rawOfficeMetaData.build().getOfficeScopeManagedObjects());

		RawManagedFunctionMetaData<Indexed, Indexed> rawMetaData = this.fullyConstructRawFunctionMetaData(true);
		this.verifyMockObjects();

		// Ensure have administered managed object and dependency as required
		ManagedFunctionMetaData<Indexed, Indexed> functionMetaData = rawMetaData.getManagedFunctionMetaData();
		ManagedObjectIndex[] requiredManagedObjects = functionMetaData.getRequiredManagedObjects();
		assertEquals("Administered managed objects should be required", 4, requiredManagedObjects.length);
		assertSame("Dependency of dependency must be first",
				four.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(), requiredManagedObjects[0]);
		Set<ManagedObjectIndex> remaining = new HashSet<>(
				Arrays.asList(one.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(),
						two.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(),
						three.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex()));
		for (int i = 1; i < requiredManagedObjects.length; i++) {
			assertTrue("Should have index", remaining.remove(requiredManagedObjects[i]));
		}
		assertEquals("Should be no remaining", 0, remaining.size());
	}

	/**
	 * Records an issue on the {@link OfficeFloorIssues} about the
	 * {@link ManagedFunction}.
	 * 
	 * @param issueDescription Issue description expected.
	 */
	private void record_functionIssue(String issueDescription) {
		this.issues.addIssue(AssetType.FUNCTION, FUNCTION_NAME, issueDescription);
	}

	/**
	 * Constructs the {@link RawManagedFunctionMetaData}.
	 * 
	 * @param isExpectConstruct If expected to be constructed.
	 * @return {@link RawManagedFunctionMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private RawManagedFunctionMetaData<Indexed, Indexed> constructRawFunctionMetaData(boolean isExpectConstruct) {

		// Construct the raw function meta-data
		RawBoundManagedObjectMetaDataFactory rawBoundMoFactory = new RawBoundManagedObjectMetaDataFactory(
				MockConstruct.mockAssetManagerRegistry(), this.rawOfficeMetaData.build().getManagedObjectMetaData(),
				this.rawOfficeMetaData.build().getGovernanceMetaData());
		RawManagedFunctionMetaData<?, ?> metaData = new RawManagedFunctionMetaDataFactory(
				this.rawOfficeMetaData.build(), rawBoundMoFactory).constructRawManagedFunctionMetaData(
						this.configuration, new AssetManagerRegistry(null, null), 1, this.issues);
		if (isExpectConstruct) {
			assertNotNull("Expected to construct meta-data", metaData);
		} else {
			assertNull("Not expected to construct meta-data", metaData);
		}

		// Return the meta-data
		return (RawManagedFunctionMetaData<Indexed, Indexed>) metaData;
	}

	/**
	 * Fully constructs the {@link RawManagedFunctionMetaData} by ensuring remaining
	 * state is loaded. Will always expect to construct the
	 * {@link RawManagedFunctionMetaData}.
	 * 
	 * @param isLoad Indicates if loaded.
	 * @return {@link RawManagedFunctionMetaData}.
	 */
	private RawManagedFunctionMetaData<Indexed, Indexed> fullyConstructRawFunctionMetaData(boolean isLoad) {

		// Construct the raw function meta-data
		RawManagedFunctionMetaData<Indexed, Indexed> metaData = this.constructRawFunctionMetaData(true);

		// Other functions expected to be constructed between these steps

		// Construct the factories
		OfficeMetaData officeMetaData = this.officeMetaData.build();
		FlowMetaDataFactory flowMetaDataFactory = new FlowMetaDataFactory(officeMetaData);
		EscalationFlowFactory escalationFlowFactory = new EscalationFlowFactory(officeMetaData);
		RawAdministrationMetaDataFactory rawAdminFactory = new RawAdministrationMetaDataFactory(officeMetaData,
				flowMetaDataFactory, escalationFlowFactory, this.rawOfficeMetaData.getOfficeTeams());

		// Link the functions and load remaining state to function meta-data
		boolean isLoaded = metaData.loadOfficeMetaData(this.officeMetaData.build(), flowMetaDataFactory,
				escalationFlowFactory, rawAdminFactory, new AssetManagerRegistry(null, null), 1, this.issues);
		assertEquals("Incorrect load", isLoad, isLoaded);

		// Return the fully constructed meta-data
		return metaData;
	}

}
