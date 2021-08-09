/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure.build;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.MockManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorExtension;
import net.officefloor.compile.test.officefloor.CompileSectionContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Property;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.Spawn;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

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
	 * {@link OfficeFloor} additional setup.
	 */
	private CompileOfficeFloorExtension officeFloorExtraSetup = null;

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
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.addProcedure("procedure", RunProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME, "procedure",
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
	 * Ensure can configure same {@link Procedure} twice (under different names).
	 */
	public void testReuseSameProcedure() throws Throwable {
		ReusedProcedure.callCount = 0;
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.linkNext(
					setup.addProcedure("procedureOne", ReusedProcedure.class.getName(),
							ClassProcedureSource.SOURCE_NAME, "procedure", true),
					setup.addProcedure("procedureTwo", ReusedProcedure.class.getName(),
							ClassProcedureSource.SOURCE_NAME, "procedure", false));
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("procedureOne"), null);
		});
		assertEquals("Should run procedure twice", 2, ReusedProcedure.callCount);
	}

	public static class ReusedProcedure {

		private static int callCount = 0;

		public void procedure() {
			callCount++;
		}
	}

	/**
	 * Ensure can trigger next {@link Procedure}.
	 */
	public void testNextProcedure() {
		NextProcedure.isInitiaited = false;
		NextProcedure.isNextRun = false;
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.linkNext(
					setup.addProcedure("initial", NextProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME,
							"initial", true),
					setup.addProcedure("next", NextProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME, "next",
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
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.linkObject(setup.addProcedure("procedure", ObjectProcedure.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "procedure", false), ProcedureObject.class.getName(), object);
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
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.addProcedure("procedure", ParameterProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME,
					"procedure", false);
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
	 * Ensure can inject {@link Property}.
	 */
	public void testProperty() {
		PropertyProcedure.propertyValue = null;
		final String VALUE = "TEST";
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.addProcedure("procedure", PropertyProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME,
					"procedure", false, "name", VALUE);
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("procedure"), null);
		});
		assertSame("Incorrect property", VALUE, PropertyProcedure.propertyValue);
	}

	public static class PropertyProcedure {

		private static String propertyValue = null;

		public void procedure(@Property("name") String value) {
			propertyValue = value;
		}
	}

	/**
	 * Ensure can inject {@link Property} via override.
	 */
	public void testOverrideProperty() {
		PropertyProcedure.propertyValue = null;
		final String VALUE = "TEST";
		this.issues.recordCaptureIssues(false);
		this.officeFloorExtraSetup = (context) -> {
			DeployedOffice office = context.getDeployedOffice();
			office.addOverrideProperty(this.getInvokeName("procedure") + ".name", VALUE);
		};
		this.doTest((setup) -> {
			setup.addProcedure("procedure", PropertyProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME,
					"procedure", false);
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("procedure"), null);
		});
		assertSame("Incorrect property", VALUE, PropertyProcedure.propertyValue);
	}

	/**
	 * Ensure can use {@link VariableManagedObjectSource}.
	 */
	public void testVariable() {
		VariableProcedure.textValue = null;
		VariableProcedure.numberValue = null;
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.linkNext(
					setup.addProcedure("outProcedure", VariableProcedure.class.getName(),
							ClassProcedureSource.SOURCE_NAME, "outProcedure", true),
					setup.addProcedure("inProcedure", VariableProcedure.class.getName(),
							ClassProcedureSource.SOURCE_NAME, "inProcedure", false));
		}, (officeFloor) -> {
			CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName("outProcedure"), null);
		});
		assertEquals("Incorrect text", "TEXT", VariableProcedure.textValue);
		assertEquals("Incorrect number", Integer.valueOf(1), VariableProcedure.numberValue);
	}

	public static class VariableProcedure {

		private static String textValue = null;

		private static Integer numberValue = null;

		public void outProcedure(Out<String> text, Var<Integer> number) {
			text.set("TEXT");
			number.set(1);
		}

		public void inProcedure(In<String> text, @Val Integer number) {
			textValue = text.get();
			numberValue = number;
		}
	}

	/**
	 * Ensure can trigger {@link Flow}.
	 */
	public void testFlow() {
		final String PARAM = "TEST";
		FlowProcedure.parameter = null;
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			setup.linkFlow(
					setup.addProcedure("initial", FlowProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME,
							"initial", false),
					"doFlow", setup.addProcedure("doFlow", FlowProcedure.class.getName(),
							ClassProcedureSource.SOURCE_NAME, "flow", false));
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
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.doTest((setup) -> {
			Object dependency = setup.addDependency(SpawnFlowObject.class);
			setup.linkFlow(
					setup.linkDependency(
							setup.addProcedure("initial", SpawnFlowProcedure.class.getName(),
									ClassProcedureSource.SOURCE_NAME, "initial", false),
							SpawnFlowObject.class.getName(), dependency),
					"doFlow",
					setup.linkDependency(
							setup.addProcedure("flow", SpawnFlowProcedure.class.getName(),
									ClassProcedureSource.SOURCE_NAME, "flow", false),
							SpawnFlowObject.class.getName(), dependency));
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
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class,
				"Argument is not compatible with function parameter (argument=java.lang.String, parameter=java.lang.Integer, function="
						+ this.getInvokeName("next") + ") for next function");
		this.doTest((setup) -> {
			setup.linkNext(
					setup.addProcedure("initial", InvalidNextParameterProcedure.class.getName(),
							ClassProcedureSource.SOURCE_NAME, "initial", true),
					setup.addProcedure("next", InvalidNextParameterProcedure.class.getName(),
							ClassProcedureSource.SOURCE_NAME, "next", false));
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
		this.issues.recordCaptureIssues(false);
		HandleEscalationProcedure.escalation = new Exception("TEST");
		this.officeExtraSetup = (office) -> {
			// Configure handling of escalation
			OfficeArchitect architect = office.getOfficeArchitect();
			ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer.employProcedureArchitect(architect,
					office.getOfficeSourceContext());
			OfficeSection handler = procedureArchitect.addProcedure("handleEscalation",
					HandleEscalationProcedure.class.getName(), ClassProcedureSource.SOURCE_NAME, "handleEscalation",
					false, new PropertyListImpl());
			OfficeEscalation escalation = architect.addOfficeEscalation(Exception.class.getName());
			architect.link(escalation, handler.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		};
		this.doTest((setup) -> {
			setup.linkEscalation(setup.addProcedure("throwEscalation", HandleEscalationProcedure.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "throwEscalation", false), Exception.class);
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
	 * Ensure can load via {@link ManagedFunction}.
	 */
	public void testLoadManagedFunction() {
		this.issues.recordCaptureIssues(false);
		String resource = "RESOURCE";
		String procedureName = "managedFunction";
		String propertyName = "name";
		String propertyValue = "value";
		Closure<Boolean> isRun = new Closure<Boolean>(false);
		MockManagedFunctionProcedureSource.run(null, (context) -> {
			assertEquals("Incorrect resource name", resource, context.getResource());
			assertEquals("Incorrect procedure name", procedureName, context.getProcedureName());
			assertEquals("Incorrect property", propertyValue, context.getSourceContext().getProperty(propertyName));
			context.setManagedFunction(() -> (managedFunctionContext) -> {
				isRun.value = true; // flag run
			}, None.class, None.class);
		}, () -> {
			this.doTest((setup) -> {
				setup.addProcedure(procedureName, resource, MockManagedFunctionProcedureSource.SERVICE_NAME,
						procedureName, false, propertyName, propertyValue);
			}, (officeFloor) -> {
				CompileOfficeFloor.invokeProcess(officeFloor, this.getInvokeName(procedureName), null);
			});
			return null;
		});
		assertTrue("Should run managed function", isRun.value);
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

		P addProcedure(String sectionName, String resource, String serviceName, String procedureName, boolean isNext,
				String... propertyNameValuePairs);

		void linkNext(P source, P target);

		void linkObject(P source, String objectName, Object target);

		D addDependency(Class<?> dependencyClass);

		P linkDependency(P source, String objectName, D dependency);

		void linkFlow(P source, String flowName, P target);

		void linkEscalation(P source, Class<? extends Throwable> escalationType);
	}

	protected static class SectionDesignerSetupContext implements SetupContext<SubSection, SectionManagedObject> {

		public final CompileSectionContext context;

		private final ProcedureArchitect<SubSection> procedureArchitect;

		private SectionDesignerSetupContext(CompileSectionContext context,
				ProcedureArchitect<SubSection> procedureDesigner) {
			this.context = context;
			this.procedureArchitect = procedureDesigner;
		}

		@Override
		public SubSection addProcedure(String sectionName, String resource, String serviceName, String procedureName,
				boolean isNext, String... propertyNameValuePairs) {
			return this.procedureArchitect.addProcedure(sectionName, resource, serviceName, procedureName, isNext,
					new PropertyListImpl(propertyNameValuePairs));
		}

		@Override
		public void linkNext(SubSection source, SubSection target) {
			this.context.getSectionDesigner().link(source.getSubSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
					target.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
		}

		@Override
		public void linkObject(SubSection source, String objectName, Object target) {
			SectionManagedObject mo = Singleton.load(this.context.getSectionDesigner(), target);
			this.context.getSectionDesigner().link(source.getSubSectionObject(objectName), mo);
		}

		@Override
		public SectionManagedObject addDependency(Class<?> dependencyClass) {
			SectionManagedObjectSource mos = context.getSectionDesigner()
					.addSectionManagedObjectSource(dependencyClass.getName(), ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, dependencyClass.getName());
			return mos.addSectionManagedObject(dependencyClass.getName(), ManagedObjectScope.THREAD);
		}

		@Override
		public SubSection linkDependency(SubSection source, String objectName, SectionManagedObject dependency) {
			this.context.getSectionDesigner().link(source.getSubSectionObject(objectName), dependency);
			return source;
		}

		@Override
		public void linkFlow(SubSection source, String flowName, SubSection target) {
			SubSectionOutput flow = source.getSubSectionOutput(flowName);
			this.context.getSectionDesigner().link(flow, target.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
		}

		@Override
		public void linkEscalation(SubSection source, Class<? extends Throwable> escalationType) {
			SubSectionOutput escalation = source.getSubSectionOutput(escalationType.getName());
			SectionOutput output = this.context.getSectionDesigner().addSectionOutput(escalationType.getName(),
					escalationType.getName(), true);
			this.context.getSectionDesigner().link(escalation, output);
		}
	}

	protected static class OfficeArchitectSetupContext implements SetupContext<OfficeSection, OfficeManagedObject> {

		public final CompileOfficeContext context;

		public final ProcedureArchitect<OfficeSection> procedureArchitect;

		private OfficeArchitectSetupContext(CompileOfficeContext context,
				ProcedureArchitect<OfficeSection> procedureArchitect) {
			this.context = context;
			this.procedureArchitect = procedureArchitect;
		}

		@Override
		public OfficeSection addProcedure(String sectionName, String resource, String serviceName, String procedureName,
				boolean isNext, String... propertyNameValuePairs) {
			return this.procedureArchitect.addProcedure(sectionName, resource, serviceName, procedureName, isNext,
					new PropertyListImpl(propertyNameValuePairs));
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
		public void linkFlow(OfficeSection source, String flowName, OfficeSection target) {
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
		if (this.officeFloorExtraSetup != null) {
			compile.officeFloor(this.officeFloorExtraSetup);
		}
		compile.office((office) -> {
			if (this.isOfficeNotSection) {
				ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer
						.employProcedureArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());
				setup.setup((SetupContext<P, D>) new OfficeArchitectSetupContext(office, procedureArchitect));
			}
		});
		if (this.officeExtraSetup != null) {
			compile.office(this.officeExtraSetup);
		}
		compile.section((section) -> {
			if (!this.isOfficeNotSection) {
				ProcedureArchitect<SubSection> procedureDesigner = ProcedureEmployer
						.employProcedureDesigner(section.getSectionDesigner(), section.getSectionSourceContext());
				setup.setup((SetupContext<P, D>) new SectionDesignerSetupContext(section, procedureDesigner));
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
		return this.isOfficeNotSection ? procedureName + ".procedure" : "SECTION." + procedureName + ".procedure";
	}

}
