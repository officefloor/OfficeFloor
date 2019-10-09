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
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileSectionContext;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;

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
		fail("TODO implement");
	}

	/**
	 * Ensure can trigger {@link Flow}.
	 */
	public void testFlow() {
		fail("TODO implement");
	}

	/**
	 * Setup logic.
	 */
	@FunctionalInterface
	protected static interface SetupLogic<P> {
		void setup(SetupContext<P> context);
	}

	/**
	 * Context for {@link SetupLogic}.
	 */
	protected static interface SetupContext<P> {

		P addProcedure(String className, String serviceName, String procedureName, boolean isNext);

		void linkNext(P source, P target);

		void linkObject(P source, String objectName, Object target);
	}

	protected static class SectionDesignerSetupContext implements SetupContext<SectionFunction> {

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
	}

	protected static class OfficeArchitectSetupContext implements SetupContext<OfficeSection> {

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
	protected <P> void doTest(SetupLogic<P> setup, TestLogic test) {

		// Configure to run procedure
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((office) -> {
			if (this.isOfficeNotSection) {
				ProcedureArchitect procedureArchitect = ProcedureEmployer
						.employProcedureArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());
				setup.setup((SetupContext<P>) new OfficeArchitectSetupContext(office, procedureArchitect));
				procedureArchitect.informOfficeArchitect();
			}
		});
		compile.section((section) -> {
			if (!this.isOfficeNotSection) {
				ProcedureDesigner procedureDesigner = ProcedureEmployer
						.employProcedureDesigner(section.getSectionDesigner(), section.getSectionSourceContext());
				setup.setup((SetupContext<P>) new SectionDesignerSetupContext(section, procedureDesigner));
				procedureDesigner.informSectionDesigner();
			}
		});

		// Run the test
		try {
			try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
				test.test(officeFloor);
			}
		} catch (Throwable ex) {
			throw fail(ex);
		}
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