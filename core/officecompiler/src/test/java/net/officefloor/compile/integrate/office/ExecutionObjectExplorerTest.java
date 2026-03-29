/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.integrate.office;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.ExecutionManagedObject;
import net.officefloor.compile.spi.office.ExecutionObjectExplorer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Tests the {@link ExecutionObjectExplorer} exploring the execution tree.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionObjectExplorerTest extends OfficeFrameTestCase {

	/**
	 * Ensure explore simple {@link ManagedObject}.
	 */
	public void testSimpleObject() throws Exception {
		this.doExplore((context) -> {
			return Singleton.load(context.getOfficeArchitect(), "OBJECT");
		}, (context) -> {
			ExecutionManagedObject mo = context.getInitialManagedObject();
			assertEquals("Incorrect managed object", "OFFICE.String", mo.getManagedObjectName());
		});
	}

	/**
	 * Ensure explore dependency.
	 */
	public void testDependency() throws Exception {
		this.doExplore((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			Singleton.load(office, "DEPENDENCY");
			OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("OBJECT",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, DependencyObject.class.getName());
			return mos.addOfficeManagedObject("OBJECT", ManagedObjectScope.THREAD);
		}, (context) -> {
			ExecutionManagedObject mo = context.getInitialManagedObject();
			assertEquals("Incorrect managed object", "OFFICE.OBJECT", mo.getManagedObjectName());
			ManagedObjectDependencyType<?>[] dependencyTypes = mo.getManagedObjectType().getDependencyTypes();
			assertEquals("Incorrect number of dependencies", 1, dependencyTypes.length);
			ExecutionManagedObject dependency = mo.getManagedObject(dependencyTypes[0]);
			assertEquals("Incorrect dependency", "OFFICE.String", dependency.getManagedObjectName());
		});
	}

	public static class DependencyObject {
		@Dependency
		private String dependency;
	}

	/**
	 * Ensure explore flow.
	 */
	public void testFlow() throws Exception {
		this.doExplore((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("OBJECT",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, FlowObject.class.getName());
			OfficeSection section = context.addSection("SECTION", FlowSection.class);
			office.link(mos.getOfficeManagedObjectFlow("function"), section.getOfficeSectionInput("service"));
			return mos.addOfficeManagedObject("OBJECT", ManagedObjectScope.THREAD);
		}, (context) -> {
			ExecutionManagedObject mo = context.getInitialManagedObject();
			assertEquals("Incorrect managed object", "OFFICE.OBJECT", mo.getManagedObjectName());
			ManagedObjectFlowType<?>[] flowTypes = mo.getManagedObjectType().getFlowTypes();
			assertEquals("Incorrect number of flows", 1, flowTypes.length);
			ExecutionManagedFunction function = mo.getManagedFunction(flowTypes[0]);
			assertEquals("Incorrect function", "SECTION.service", function.getManagedFunctionName());
		});
	}

	@FlowInterface
	public static interface Flows {
		void function();
	}

	public static class FlowObject {
		Flows flows;
	}

	public static class FlowSection {
		public void service() {
			// no operation
		}
	}

	/**
	 * Undertakes the exploration.
	 * 
	 * @param extension {@link CompileExecutionExtension} to configured the
	 *                  {@link Office}.
	 * @param explorer  {@link ExecutionObjectExplorer}.
	 */
	private void doExplore(CompileExecutionExtension extension, ExecutionObjectExplorer explorer) throws Exception {
		CompileOfficeFloor compile = new CompileOfficeFloor();
		Closure<Boolean> isExplored = new Closure<>(false);
		compile.office((compileContext) -> {

			// Run the extension, with exploration
			OfficeManagedObject managedObject = extension.extend(compileContext);

			// Register the execution explorer
			managedObject.addExecutionExplorer((executionContext) -> {

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
		OfficeManagedObject extend(CompileOfficeContext context) throws Exception;
	}

}
