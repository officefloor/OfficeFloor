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

package net.officefloor.compile.integrate.managedobject;

import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling a {@link SectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileSectionManagedObjectTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {

		// Record managed object type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} bound to {@link ProcessState}.
	 */
	public void testProcessBoundManagedObject() {

		// Record managed object type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT", "OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.recordReturn(office,
				office.addProcessManagedObject("OFFICE.SECTION.MANAGED_OBJECT", "OFFICE.SECTION.MANAGED_OBJECT"), null);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not linked.
	 */
	public void testManagedObjectWithDependencyNotLinked() {

		// Record managed object type
		this.issues.recordCaptureIssues(false);
		this.issues.recordIssue("OFFICE.SECTION.DEPENDENT." + Node.escape(SimpleManagedObject.class.getName()),
				ManagedObjectDependencyNodeImpl.class, "Managed Object Dependency "
						+ SimpleManagedObject.class.getName() + " is not linked to a DependentObjectNode");
		this.issues.recordIssue("OFFICE", OfficeNodeImpl.class,
				"Failure loading " + OfficeSectionType.class.getSimpleName() + " from source SECTION");

		// Compile the OfficeFloor
		this.compile(false);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency in
	 * {@link SectionModel}.
	 */
	public void testManagedObjectWithDependencyInSection() {

		// Record managed object type
		this.issues.recordCaptureIssues(false);

		// Record building the office
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.DEPENDENT", "OFFICE.SECTION.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this.record_officeBuilder_addProcessManagedObject("OFFICE.SECTION.DEPENDENT",
				"OFFICE.SECTION.DEPENDENT");
		mapper.mapDependency(0, "OFFICE.SECTION.SIMPLE");
		office.registerManagedObjectSource("OFFICE.SECTION.SIMPLE", "OFFICE.SECTION.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SECTION.SIMPLE", "OFFICE.SECTION.SIMPLE");

		// Add managed objects to office
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.SIMPLE_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency outside
	 * {@link SectionModel}.
	 */
	public void testManagedObjectWithDependencyOutSideSection() {

		// Record managed object type
		this.issues.recordCaptureIssues(false);

		// Record building the office
		this.record_init();

		// Register the section linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SIMPLE", "OFFICE.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE", "OFFICE.SIMPLE");
		office.registerManagedObjectSource("OFFICE.SECTION.DEPENDENT", "OFFICE.SECTION.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this.record_officeBuilder_addProcessManagedObject("OFFICE.SECTION.DEPENDENT",
				"OFFICE.SECTION.DEPENDENT");
		mapper.mapDependency(0, "OFFICE.SIMPLE");

		// Add managed objects to office
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectFlow} of
	 * {@link SectionManagedObjectSource} is not linked.
	 */
	public void testManagedObjectSourceFlowNotLinked() {

		// Record managed object type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.issues.recordIssue("OFFICE.SECTION.MANAGED_OBJECT_SOURCE.doProcess", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow doProcess is not linked to a ManagedFunctionNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked {@link ProcessState}
	 * with a {@link SubSectionInput}.
	 */
	public void testManagedObjectSourceFlowLinkedToSubSectionInput() {

		// Record managed object type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		managingOffice.linkFlow(0, "SECTION.SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked {@link ProcessState}
	 * with an {@link ExternalFlowModel}.
	 */
	public void testManagedObjectSourceFlowLinkedToExternalFlow() {

		// Record section and managed object type
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION_ONE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.SECTION_ONE.MANAGED_OBJECT_SOURCE");
		managingOffice.linkFlow(0, "SECTION_TWO.INPUT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_TWO", "INPUT");
		function.linkParameter(0, Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests to ensure can link {@link ManagedObject} with
	 * {@link ManagedObjectPool}.
	 */
	public void testManagedObjectPooling() {

		// Record section and managed object type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		// Register the managed object pool for managed object
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT", "OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a {@link Dependency}.
	 */
	public static class DependencyManagedObject {

		@Dependency
		SimpleManagedObject dependency;
	}

	/**
	 * Class for {@link ClassManagedFunctionSource}.
	 */
	public static class ProcessClass {

		public void process(Integer parameter) {
		}
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a
	 * {@link FlowInterface}.
	 */
	public static class ProcessManagedObject {

		@FlowInterface
		public static interface Processes {
			void doProcess(Integer parameter);
		}

		Processes processes;
	}

}
