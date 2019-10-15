/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.procedure.build;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.java.ClassProcedureServiceFactory;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileSectionContext;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.Spawn;

/**
 * Tests the {@link ProcedureDesigner}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractProcedureTestCase extends OfficeFrameTestCase {

	/**
	 * Indicates {@link ProcedureArchitect}. Otherwise {@link ProcedureDesigner}.
	 */
	private final boolean isOfficeNotSection;

	/**
	 * {@link MockCompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link Office} additional setup.
	 */
	private CompileOfficeExtension officeExtraSetup = null;

	/**
	 * Instantiate.
	 * 
	 * @param isOfficeNotSection Indicates {@link ProcedureArchitect}. Otherwise
	 *                           {@link ProcedureDesigner}.
	 */
	public AbstractProcedureTestCase(Boolean isOfficeNotSection) {
		this.isOfficeNotSection = isOfficeNotSection;
	}

	/**
	 * Ensure can run the {@link Procedure}.
	 */
	public void testRunProcedure() throws Throwable {
		RunProcedure.isRun = false;
		this.recordPossibleSection();
		this.doTest((setup) -> {
			setup.addProcedure(RunProcedure.class.getName(), ClassProcedureServiceFactory.SERVICE_NAME, "procedure",
					false);
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("procedure"), null);
		});
		assertTrue("Should run procedure", RunProcedure.isRun);
	}

	public static class RunProcedure {

		private static boolean isRun = false;

		public void procedure() {
			isRun = true;
		}
	}

	/**
	 * Ensure can trigger next {@link Procedure}.
	 */
	public void testNextProcedure() {
		NextProcedure.isInitiaited = false;
		NextProcedure.isNextRun = false;
		this.recordPossibleSection();
		this.recordPossibleSection();
		this.doTest((setup) -> {
			setup.linkNext(
					setup.addProcedure(NextProcedure.class.getName(), ClassProcedureServiceFactory.SERVICE_NAME,
							"initial", true),
					setup.addProcedure(NextProcedure.class.getName(), ClassProcedureServiceFactory.SERVICE_NAME, "next",
							false));
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("initial"), null);
		});
		assertTrue("Should run initial", NextProcedure.isInitiaited);
		assertTrue("Should run next", NextProcedure.isNextRun);
	}

	public static class NextProcedure {

		private static boolean isInitiaited = false;

		private static boolean isNextRun = false;

		public void initial() {
			assertFalse("Next should not be run", isNextRun);
			isInitiaited = true;
		}

		public void next() {
			assertTrue("Should be initiated", isInitiaited);
			isNextRun = true;
		}
	}

	/**
	 * Ensure can trigger with object.
	 */
	public void testObjectProcedure() {
		ObjectProcedure.procedureObject = null;
		ProcedureObject object = new ProcedureObject();
		this.recordPossibleSection();
		this.doTest((setup) -> {
			setup.linkObject(setup.addProcedure(ObjectProcedure.class.getName(),
					ClassProcedureServiceFactory.SERVICE_NAME, "procedure", false), ProcedureObject.class.getName(),
					object);
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("procedure"), null);
		});
		assertSame("Incorrect object", object, ObjectProcedure.procedureObject);
	}

	public static class ProcedureObject {
	}

	public static class ObjectProcedure {

		private static ProcedureObject procedureObject = null;

		public void procedure(ProcedureObject object) {
			procedureObject = object;
		}
	}

	/**
	 * Ensure can pass parameter.
	 */
	public void testParameter() {
		ParameterProcedure.parameter = null;
		final String PARAM = "TEST";
		this.recordPossibleSection();
		this.doTest((setup) -> {
			setup.flagParameter(setup.addProcedure(ParameterProcedure.class.getName(),
					ClassProcedureServiceFactory.SERVICE_NAME, "procedure", false), String.class.getName());
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("procedure"), PARAM);
		});
		assertSame("Incorrect parameter", PARAM, ParameterProcedure.parameter);
	}

	public static class ParameterProcedure {

		private static String parameter = null;

		public void procedure(@Parameter String param) {
			parameter = param;
		}
	}

	/**
	 * Ensure can trigger {@link Flow}.
	 */
	public void testFlow() {
		final String PARAM = "TEST";
		FlowProcedure.parameter = null;
		this.recordPossibleSection();
		this.recordPossibleSection();
		this.doTest((setup) -> {
			setup.linkFlow(
					setup.flagParameter(setup.addProcedure(FlowProcedure.class.getName(),
							ClassProcedureServiceFactory.SERVICE_NAME, "initial", false), String.class.getName()),
					"doFlow", setup.flagParameter(setup.addProcedure(FlowProcedure.class.getName(),
							ClassProcedureServiceFactory.SERVICE_NAME, "flow", false), String.class.getName()),
					false);
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("initial"), PARAM);
		});
		assertEquals("Incorrect flow parameter", PARAM, FlowProcedure.parameter);
	}

	@FlowInterface
	public static interface Flows {
		void doFlow(String parameter);
	}

	public static class FlowProcedure {

		private static String parameter = null;

		public void initial(Flows flows, @Parameter String parameter) {
			flows.doFlow(parameter);
		}

		public void flow(@Parameter String param) {
			parameter = param;
		}
	}

	/**
	 * Ensure can spawn {@link Flow}.
	 */
	public void testSpawnFlow() {
		SpawnFlowProcedure.initialObject = null;
		SpawnFlowProcedure.flowObject = null;
		this.recordPossibleSection();
		this.recordPossibleSection();
		this.doTest((setup) -> {
			Object dependency = setup.addDependency(SpawnFlowObject.class);
			setup.linkFlow(
					setup.linkDependency(
							setup.addProcedure(SpawnFlowProcedure.class.getName(),
									ClassProcedureServiceFactory.SERVICE_NAME, "initial", false),
							SpawnFlowObject.class.getName(), dependency),
					"doFlow",
					setup.linkDependency(
							setup.addProcedure(SpawnFlowProcedure.class.getName(),
									ClassProcedureServiceFactory.SERVICE_NAME, "flow", false),
							SpawnFlowObject.class.getName(), dependency),
					true);
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("initial"), null);
		});
		assertNotNull("Should inject dependency for initial", SpawnFlowProcedure.initialObject);
		assertNotNull("Should inject dependency for flow", SpawnFlowProcedure.flowObject);
		assertNotSame("Should be different dependencies due to spawn", SpawnFlowProcedure.initialObject,
				SpawnFlowProcedure.flowObject);
	}

	public static class SpawnFlowObject {
	}

	@FlowInterface
	public static interface SpawnFlows {

		@Spawn
		void doFlow();
	}

	public static class SpawnFlowProcedure {

		private static SpawnFlowObject initialObject;

		private static SpawnFlowObject flowObject;

		public void initial(SpawnFlowObject object, SpawnFlows flows) {
			initialObject = object;
			flows.doFlow();
		}

		public void flow(SpawnFlowObject object) {
			flowObject = object;
		}
	}

	/**
	 * Ensure validate the parameter type.
	 */
	public void testInvalidParameterType() {
		this.recordPossibleSection();
		this.recordPossibleSection();
		this.issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class,
				"Argument is not compatible with function parameter (argument=java.lang.String, parameter=java.lang.Integer, function="
						+ this.getInvokeName("next") + ") for next function");
		this.doTest((setup) -> {
			setup.linkNext(
					setup.addProcedure(InvalidNextParameterProcedure.class.getName(),
							ClassProcedureServiceFactory.SERVICE_NAME, "initial", true),
					setup.flagParameter(
							setup.addProcedure(InvalidNextParameterProcedure.class.getName(),
									ClassProcedureServiceFactory.SERVICE_NAME, "next", false),
							Integer.class.getName()));
		}, null);
	}

	public static class InvalidNextParameterProcedure {

		public String initial() {
			return "INVALID";
		}

		public void next(@Parameter Integer invalid) {
			// not matching parameter type
		}
	}

	/**
	 * Ensure can handle {@link Escalation}.
	 */
	public void testHandleEscalation() {
		this.issues.recordCaptureIssues(false);
		this.recordPossibleSection();
		HandleEscalationProcedure.escalation = new Exception("TEST");
		this.officeExtraSetup = (office) -> {
			// Configure handling of escalation
			OfficeArchitect architect = office.getOfficeArchitect();
			ProcedureArchitect procedureArchitect = ProcedureEmployer.employProcedureArchitect(architect,
					office.getOfficeSourceContext());
			OfficeSection handler = procedureArchitect.addProcedure(HandleEscalationProcedure.class.getName(),
					ClassProcedureServiceFactory.SERVICE_NAME, "handleEscalation", false);
			OfficeEscalation escalation = architect.addOfficeEscalation(Exception.class.getName());
			architect.link(escalation, handler.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
			procedureArchitect.informOfficeArchitect();
		};
		this.doTest((setup) -> {
			setup.linkEscalation(setup.addProcedure(HandleEscalationProcedure.class.getName(),
					ClassProcedureServiceFactory.SERVICE_NAME, "throwEscalation", false), Exception.class);
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("throwEscalation"), null);
		});
		assertSame("Incorrect handled escalation", HandleEscalationProcedure.escalation,
				HandleEscalationProcedure.handled);
	}

	public static class HandleEscalationProcedure {

		private static Exception escalation;

		private static Exception handled = null;

		public void throwEscalation() throws Exception {
			throw escalation;
		}

		public void handleEscalation(@Parameter Exception escalation) {
			handled = escalation;
		}
	}

	/**
	 * Records setup for possible {@link SectionSource}.
	 */
	private void recordPossibleSection() {
		if (this.isOfficeNotSection) {
			this.issues.recordCaptureIssues(false);
		}
	}

	/**
	 * Setup logic.
	 */
	@FunctionalInterface
	protected static interface SetupLogic<P, D> {
		void setup(SetupContext<P, D> context);
	}

	/**
	 * Context for {@link SetupLogic}.
	 */
	protected static interface SetupContext<P, D> {

		P addProcedure(String className, String serviceName, String procedureName, boolean isNext);

		void linkNext(P source, P target);

		void linkObject(P source, String objectName, Object target);

		D addDependency(Class<?> dependencyClass);

		P linkDependency(P source, String objectName, D dependency);

		P flagParameter(P source, String objectName);

		void linkFlow(P source, String flowName, P target, boolean isSpawn);

		void linkEscalation(P source, Class<? extends Throwable> escalationType);
	}

	protected static class SectionDesignerSetupContext implements SetupContext<SectionFunction, SectionManagedObject> {

		public final CompileSectionContext context;

		private final ProcedureDesigner procedureDesigner;

		private SectionDesignerSetupContext(CompileSectionContext context, ProcedureDesigner procedureDesigner) {
			this.context = context;
			this.procedureDesigner = procedureDesigner;
		}

		@Override
		public SectionFunction addProcedure(String className, String serviceName, String procedureName,
				boolean isNext) {
			return this.procedureDesigner.addProcedure(className, serviceName, procedureName);
		}

		@Override
		public void linkNext(SectionFunction source, SectionFunction target) {
			this.context.getSectionDesigner().link(source, target);
		}

		@Override
		public void linkObject(SectionFunction source, String objectName, Object target) {
			SectionManagedObject mo = Singleton.load(this.context.getSectionDesigner(), target);
			this.context.getSectionDesigner().link(source.getFunctionObject(objectName), mo);
		}

		@Override
		public SectionManagedObject addDependency(Class<?> dependencyClass) {
			SectionManagedObjectSource mos = context.getSectionDesigner()
					.addSectionManagedObjectSource(dependencyClass.getName(), ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, dependencyClass.getName());
			return mos.addSectionManagedObject(dependencyClass.getName(), ManagedObjectScope.THREAD);
		}

		@Override
		public SectionFunction linkDependency(SectionFunction source, String objectName,
				SectionManagedObject dependency) {
			this.context.getSectionDesigner().link(source.getFunctionObject(objectName), dependency);
			return source;
		}

		@Override
		public SectionFunction flagParameter(SectionFunction source, String objectName) {
			source.getFunctionObject(objectName).flagAsParameter();
			return source;
		}

		@Override
		public void linkFlow(SectionFunction source, String flowName, SectionFunction target, boolean isSpawn) {
			FunctionFlow flow = source.getFunctionFlow(flowName);
			this.context.getSectionDesigner().link(flow, target, isSpawn);
		}

		@Override
		public void linkEscalation(SectionFunction source, Class<? extends Throwable> escalationType) {
			FunctionFlow escalation = source.getFunctionEscalation(escalationType.getName());
			SectionOutput output = this.context.getSectionDesigner().addSectionOutput(escalationType.getName(),
					escalationType.getName(), true);
			this.context.getSectionDesigner().link(escalation, output, false);
		}
	}

	protected static class OfficeArchitectSetupContext implements SetupContext<OfficeSection, OfficeManagedObject> {

		public final CompileOfficeContext context;

		public final ProcedureArchitect procedureArchitect;

		private OfficeArchitectSetupContext(CompileOfficeContext context, ProcedureArchitect procedureArchitect) {
			this.context = context;
			this.procedureArchitect = procedureArchitect;
		}

		@Override
		public OfficeSection addProcedure(String className, String serviceName, String procedureName, boolean isNext) {
			return this.procedureArchitect.addProcedure(className, serviceName, procedureName, isNext);
		}

		@Override
		public void linkNext(OfficeSection source, OfficeSection target) {
			this.context.getOfficeArchitect().link(source.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
					target.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		}

		@Override
		public void linkObject(OfficeSection source, String objectName, Object target) {
			OfficeManagedObject mo = Singleton.load(this.context.getOfficeArchitect(), target);
			this.context.getOfficeArchitect().link(source.getOfficeSectionObject(objectName), mo);
		}

		@Override
		public OfficeManagedObject addDependency(Class<?> dependencyClass) {
			OfficeManagedObjectSource mos = context.getOfficeArchitect()
					.addOfficeManagedObjectSource(dependencyClass.getName(), ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, dependencyClass.getName());
			return mos.addOfficeManagedObject(dependencyClass.getName(), ManagedObjectScope.THREAD);
		}

		@Override
		public OfficeSection linkDependency(OfficeSection source, String objectName, OfficeManagedObject dependency) {
			this.context.getOfficeArchitect().link(source.getOfficeSectionObject(objectName), dependency);
			return source;
		}

		@Override
		public OfficeSection flagParameter(OfficeSection source, String objectName) {
			// Should be managed by section
			return source;
		}

		@Override
		public void linkFlow(OfficeSection source, String flowName, OfficeSection target, boolean isSpawn) {
			OfficeSectionOutput output = source.getOfficeSectionOutput(flowName);
			this.context.getOfficeArchitect().link(output, target.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		}

		@Override
		public void linkEscalation(OfficeSection source, Class<? extends Throwable> escalationType) {
			// Should be managed by section
		}
	}

	/**
	 * Test logic.
	 */
	@FunctionalInterface
	protected static interface TestLogic {
		void test(OfficeFloor officeFloor) throws Throwable;
	}

	/**
	 * Undertakes test.
	 * 
	 * @param setup {@link SetupLogic}.
	 * @param test  {@link TestLogic}.
	 */
	@SuppressWarnings("unchecked")
	protected <P, D> void doTest(SetupLogic<P, D> setup, TestLogic test) {

		// Configure to run procedure
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler().setCompilerIssues(this.issues);
		compile.office((office) -> {
			if (this.isOfficeNotSection) {
				ProcedureArchitect procedureArchitect = ProcedureEmployer
						.employProcedureArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());
				setup.setup((SetupContext<P, D>) new OfficeArchitectSetupContext(office, procedureArchitect));
				procedureArchitect.informOfficeArchitect();
			}
		});
		if (this.officeExtraSetup != null) {
			compile.office(this.officeExtraSetup);
		}
		compile.section((section) -> {
			if (!this.isOfficeNotSection) {
				ProcedureDesigner procedureDesigner = ProcedureEmployer
						.employProcedureDesigner(section.getSectionDesigner(), section.getSectionSourceContext());
				setup.setup((SetupContext<P, D>) new SectionDesignerSetupContext(section, procedureDesigner));
				procedureDesigner.informSectionDesigner();
			}
		});

		// Run the test
		this.replayMockObjects();
		try {
			try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
				if (test == null) {
					assertNull("Should not compile OfficeFloor", officeFloor);
				} else {
					test.test(officeFloor);
				}
			}
		} catch (Throwable ex) {
			throw fail(ex);
		}
		this.verifyMockObjects();
	}

	/**
	 * Obtains the {@link Procedure} {@link ManagedFunction} name.
	 * 
	 * @param procedureName {@link Procedure} name.
	 * @return {@link Procedure} {@link ManagedFunction} name.
	 */
	protected String getInvokeName(String procedureName) {
		return this.isOfficeNotSection ? procedureName + ".procedure" : "SECTION." + procedureName;
	}

}