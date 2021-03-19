/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.integrate.office;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.spi.office.ExecutionExplorer;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.ExecutionManagedObject;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.SectionInterface;

/**
 * Tests the {@link ExecutionExplorer} exploring the execution tree.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionExplorerTest extends OfficeFrameTestCase {

	/**
	 * Ensure can explore a single {@link ManagedFunction}.
	 */
	public void testSingleFunction() throws Exception {
		this.doExplore((context) -> {
			return context.addSection("SECTION", SingleFunction.class).getOfficeSectionInput("function");
		}, (context) -> {
			ExecutionManagedFunction function = context.getInitialManagedFunction();
			assertEquals("Incorrect function name", "SECTION.function", function.getManagedFunctionName());
			ManagedFunctionType<?, ?> functionType = function.getManagedFunctionType();
			assertNotNull("Should have managed function type", functionType);
			assertEquals("Should be no flows", 0, functionType.getFlowTypes().length);
			assertEquals("Shoud be no escalations", 0, functionType.getEscalationTypes().length);
			assertEquals("Should only have section object dependency", 1, functionType.getObjectTypes().length);
		});
	}

	public static class SingleFunction {
		public void function() {
		}
	}

	/**
	 * Ensure can explore next {@link ManagedFunction}.
	 */
	public void testNextFunction() throws Exception {
		this.doExplore((context) -> {
			return context.addSection("SECTION", NextFunction.class).getOfficeSectionInput("initiate");
		}, (context) -> {
			ExecutionManagedFunction initiate = context.getInitialManagedFunction();
			assertEquals("Incorrect initial function", "SECTION.initiate", initiate.getManagedFunctionName());
			ExecutionManagedFunction next = initiate.getNextManagedFunction();
			assertEquals("Incorrect next function", "SECTION.next", next.getManagedFunctionName());
		});
	}

	public static class NextFunction {
		@Next("next")
		public void initiate() {
			// no operation
		}

		public void next() {
			// no operation
		}
	}

	/**
	 * Ensure can explore a {@link Flow} to another {@link ManagedFunction}.
	 */
	public void testFlowToFunction() throws Exception {
		this.doExplore((context) -> {
			return context.addSection("SECTION", FlowToFunction.class).getOfficeSectionInput("initiate");
		}, (context) -> {
			ExecutionManagedFunction initiate = context.getInitialManagedFunction();
			assertEquals("Incorrect function", "SECTION.initiate", initiate.getManagedFunctionName());
			ManagedFunctionFlowType<?>[] flows = initiate.getManagedFunctionType().getFlowTypes();
			assertEquals("Incorrect number of flows", 1, flows.length);
			ExecutionManagedFunction function = initiate.getManagedFunction(flows[0]);
			assertEquals("Incorrect function", "SECTION.function", function.getManagedFunctionName());
		});
	}

	@FlowInterface
	public static interface FunctionFlows {
		void function();
	}

	public static class FlowToFunction {
		public void initiate(FunctionFlows flows) {
		}

		public void function() {
		}
	}

	/**
	 * Ensure can explore an {@link Escalation} to its handling
	 * {@link ManagedFunction}.
	 */
	public void testEscalationToFunction() throws Exception {
		this.doExplore((context) -> {
			return context.addSection("SECTION", EscalationToFunction.class).getOfficeSectionInput("trigger");
		}, (context) -> {
			ExecutionManagedFunction trigger = context.getInitialManagedFunction();
			assertEquals("Incorrect function", "SECTION.trigger", trigger.getManagedFunctionName());
			ManagedFunctionEscalationType[] escalations = trigger.getManagedFunctionType().getEscalationTypes();
			assertEquals("Incorrect number of escalations", 1, escalations.length);
			ExecutionManagedFunction handle = trigger.getManagedFunction(escalations[0]);
			assertEquals("Incorrect function", "SECTION.handle", handle.getManagedFunctionName());
		});
	}

	public static class EscalationToFunction {
		public void trigger() throws SQLException {
		}

		public void handle(@Parameter SQLException ex) {
		}
	}

	/**
	 * Ensure can explore {@link Escalation} from application.
	 */
	public void testEscalationFromApplication() throws Exception {
		this.doExplore((context) -> {
			return context.addSection("SECTION", EscalationFromApplication.class).getOfficeSectionInput("trigger");
		}, (context) -> {
			ExecutionManagedFunction trigger = context.getInitialManagedFunction();
			assertEquals("Incorrect function", "SECTION.trigger", trigger.getManagedFunctionName());
			ManagedFunctionEscalationType[] escalations = trigger.getManagedFunctionType().getEscalationTypes();
			assertEquals("Incorrect number of escalations", 1, escalations.length);
			ExecutionManagedFunction handle = trigger.getManagedFunction(escalations[0]);
			assertNull("No function if not handled by application", handle);
		});
	}

	public static class EscalationFromApplication {
		public void trigger() throws IOException {
		}
	}

	/**
	 * Ensure can explore {@link ManagedObject} for the {@link ManagedFunction}.
	 */
	public void testObjectForFunction() throws Exception {
		this.doExplore((context) -> {
			context.addManagedObject("MO", FunctionObject.class, ManagedObjectScope.THREAD);
			return context.addSection("SECTION", ObjectForFunction.class).getOfficeSectionInput("function");
		}, (context) -> {
			ExecutionManagedFunction function = context.getInitialManagedFunction();
			assertEquals("Incorrect function", "SECTION.function", function.getManagedFunctionName());
			ManagedFunctionObjectType<?>[] objects = function.getManagedFunctionType().getObjectTypes();
			assertEquals("Incorrect number of objects (includes section object)", 2, objects.length);
			ExecutionManagedObject object = function.getManagedObject(objects[1]);
			assertEquals("Incorrect managed boject", "OFFICE.MO", object.getManagedObjectName());
		});
	}

	public static class FunctionObject {
	}

	public static class ObjectForFunction {
		public void function(FunctionObject object) {
		}
	}

	/**
	 * Ensure can explore dependency of a {@link ManagedObject}.
	 */
	public void testObjectDependency() throws Exception {
		this.doExplore((context) -> {
			context.addManagedObject("MO", DependentObject.class, ManagedObjectScope.THREAD);
			context.addManagedObject("DEPENDENCY", DependencyObject.class, ManagedObjectScope.THREAD);
			return context.addSection("SECTION", ObjectDependency.class).getOfficeSectionInput("function");
		}, (context) -> {
			ExecutionManagedFunction function = context.getInitialManagedFunction();
			assertEquals("Incorrect function", "SECTION.function", function.getManagedFunctionName());
			ManagedFunctionObjectType<?>[] objects = function.getManagedFunctionType().getObjectTypes();
			assertEquals("Incorrect number of objects (includes seciton object)", 2, objects.length);
			ExecutionManagedObject object = function.getManagedObject(objects[1]);
			assertEquals("Incorrect managed boject", "OFFICE.MO", object.getManagedObjectName());
			ManagedObjectDependencyType<?>[] dependencies = object.getManagedObjectType().getDependencyTypes();
			assertEquals("Incorrect number of dependencies", 1, dependencies.length);
			ExecutionManagedObject dependency = object.getManagedObject(dependencies[0]);
			assertEquals("Incorrect dependency", "OFFICE.DEPENDENCY", dependency.getManagedObjectName());
		});
	}

	public static class DependencyObject {
	}

	public static class DependentObject {
		@Dependency
		private DependencyObject dependency;
	}

	public static class ObjectDependency {
		public void function(DependentObject object) {
		}
	}

	/**
	 * Ensure can explore {@link Flow} from {@link ManagedObject}.
	 */
	public void testObjectFlow() throws Exception {
		this.doExplore((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, FlowObject.class.getName());
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
			OfficeSection section = context.addSection("SECTION", ObjectFlow.class);
			office.link(mos.getOfficeManagedObjectFlow("doFlow"), section.getOfficeSectionInput("input"));
			return section.getOfficeSectionInput("function");
		}, (context) -> {
			ExecutionManagedFunction function = context.getInitialManagedFunction();
			assertEquals("Incorrect function", "SECTION.function", function.getManagedFunctionName());
			ManagedFunctionObjectType<?>[] objects = function.getManagedFunctionType().getObjectTypes();
			assertEquals("Incorrect number of objects (includes seciton object)", 2, objects.length);
			ExecutionManagedObject object = function.getManagedObject(objects[1]);
			assertEquals("Incorrect managed boject", "OFFICE.MO", object.getManagedObjectName());
			ManagedObjectFlowType<?>[] flows = object.getManagedObjectType().getFlowTypes();
			assertEquals("Incorrect number of flows", 1, flows.length);
			ExecutionManagedFunction flow = object.getManagedFunction(flows[0]);
			assertEquals("Incorrect flow function", "SECTION.input", flow.getManagedFunctionName());
		});
	}

	@FlowInterface
	public static interface ObjectFlows {
		void doFlow();
	}

	public static class FlowObject {
		protected ObjectFlows flows;
	}

	public static class ObjectFlow {
		public void function(FlowObject object) {
		}

		public void input() {
		}
	}

	/**
	 * Ensure can dynamically obtain the {@link ExecutionManagedFunction}.
	 */
	public void testDynamicFunction() throws Exception {
		this.doExplore((context) -> {
			return context.addSection("SECTION", DynamicFunction.class).getOfficeSectionInput("function");
		}, (context) -> {
			// Ensure can dynamically obtain function
			ExecutionManagedFunction dynamic = context.getManagedFunction("SECTION.dynamic");
			assertNotNull("Should find dynamic", dynamic);
			assertEquals("Incorrect dynamic", "SECTION.dynamic", dynamic.getManagedFunctionName());
		});
	}

	public static class DynamicFunction {
		public void function() {
		}

		public void dynamic() {
		}
	}

	/**
	 * Ensure can dynamically obtain the {@link ExecutionManagedFunction} from a
	 * {@link SubSection}.
	 */
	public void testDynamicSubSectionFunction() throws Exception {
		this.doExplore((context) -> {
			return context.addSection("SECTION", SectionFunction.class).getOfficeSectionInput("trigger");
		}, (context) -> {
			// Ensure can dynamically obtain function
			ExecutionManagedFunction dynamic = context.getManagedFunction("SECTION.SubSectionFunction.function");
			assertNotNull("Should find sub section function", dynamic);
			assertEquals("Incorrect sub section function", "SECTION.SubSectionFunction.function",
					dynamic.getManagedFunctionName());
		});
	}

	@SectionInterface(source = ClassSectionSource.class, locationClass = SubSectionFunctionImpl.class)
	public static interface SubSectionFunction {
		public void function();
	}

	public static class SubSectionFunctionImpl {
		public void function() {
		}
	}

	public static class SectionFunction {
		public void trigger(SubSectionFunction subSection) {
		}
	}

	/**
	 * Undertakes the exploration.
	 * 
	 * @param extension {@link CompileExecutionExtension} to configured the
	 *                  {@link Office}.
	 * @param explorer  {@link ExecutionExplorer}.
	 */
	private void doExplore(CompileExecutionExtension extension, ExecutionExplorer explorer) throws Exception {
		CompileOfficeFloor compile = new CompileOfficeFloor();
		Closure<Boolean> isExplored = new Closure<>(false);
		compile.office((compileContext) -> {

			// Run the extension, with exploration
			OfficeSectionInput input = extension.extend(compileContext);

			// Register the execution explorer
			input.addExecutionExplorer((executionContext) -> {

				// Undertake exploration
				explorer.explore(executionContext);

				// Flag that explored
				isExplored.value = true;
			});
		});
		compile.compileAndOpenOfficeFloor();
		assertTrue("Should have explored execution tree", isExplored.value);
	}

	private static interface CompileExecutionExtension {
		OfficeSectionInput extend(CompileOfficeContext context) throws Exception;
	}

}
