/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.integrate.officefloor;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.structure.OfficeObjectNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.extension.AutoWireOfficeFloorExtensionService;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Tests the {@link AutoWire} of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorTest extends AbstractCompileTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Reset the Office and Supplier
		CompileOfficeSource.reset();
		CompileSupplierSource.reset();
	}

	/**
	 * Ensure can auto-wire an {@link OfficeFloorManagedObject}.
	 */
	public void testAutoWireOfficeFloorManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Auto-wire the object
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the office
		CompileOfficeSource.registerOffice(null, CompileManagedObject.class, "MANAGED_OBJECT", null, this);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectDependency} for an
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testAutoWireOfficeFloorManagedObjectDependency() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENCY_OBJECT", "DEPENDENCY_SOURCE");
		DependencyMappingBuilder dependencyMo = this.record_officeBuilder_addThreadManagedObject("DEPENDENCY_OBJECT",
				"DEPENDENCY_OBJECT");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE_OBJECT", "SIMPLE_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("SIMPLE_OBJECT", "SIMPLE_OBJECT");

		// Auto-wire the dependency
		dependencyMo.mapDependency(0, "SIMPLE_OBJECT");

		// Build the office
		CompileOfficeSource.registerOffice(null, DependencyManagedObject.class, "DEPENDENCY_OBJECT", null, this);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire an {@link OfficeFloorInputManagedObject}.
	 */
	public void testAutoWireOfficeFloorInputManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("INPUT_SOURCE", ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_OBJECT");

		// Build the office
		CompileOfficeSource.registerOffice(null, CompileManagedObject.class, "INPUT_OBJECT", null, this);

		// Link the input
		managingOffice.linkFlow(0, "SECTION.FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire a qualified {@link OfficeFloorInputManagedObject}.
	 */
	public void testAutoWireOfficeFloorQualifiedInputManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("INPUT_SOURCE", ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_OBJECT");

		// Build the office
		CompileOfficeSource.registerOffice("QUALIFIED", CompileManagedObject.class, "INPUT_OBJECT", null, this);

		// Link the input
		managingOffice.linkFlow(0, "SECTION.FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectDependency} for an
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testAutoWireOfficeFloorInputManagedObjectDependency() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputMo = this
				.record_managingOfficeBuilder_setInputManagedObjectName("DEPENDENCY_OBJECT");

		// Build the office
		CompileOfficeSource.registerOffice(null, DependencyManagedObject.class, "DEPENDENCY_OBJECT", null, this);

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE_OBJECT", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE_OBJECT", "SIMPLE_OBJECT");

		// Map the auto-wired dependency
		inputMo.mapDependency(0, "SIMPLE_OBJECT");

		// Link the input
		managingOffice.linkFlow(0, "SECTION.FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if cyclic dependencies for {@link ManagedObjectDependency}
	 * chain.
	 */
	public void testAutoWireOfficeFloorManagedObjectWithCyclicDependencies() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("DEPENDENCY_ONE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENCY_ONE_OBJECT", "DEPENDENCY_ONE_SOURCE");
		DependencyMappingBuilder one = this.record_officeBuilder_addThreadManagedObject("DEPENDENCY_ONE_OBJECT",
				"DEPENDENCY_ONE_OBJECT");
		one.mapDependency(0, "DEPENDENCY_TWO_OBJECT");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("DEPENDENCY_TWO_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CycleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENCY_TWO_OBJECT", "DEPENDENCY_TWO_SOURCE");
		DependencyMappingBuilder two = this.record_officeBuilder_addThreadManagedObject("DEPENDENCY_TWO_OBJECT",
				"DEPENDENCY_TWO_OBJECT");
		two.mapDependency(0, "DEPENDENCY_ONE_OBJECT");

		// Build the office
		CompileOfficeSource.registerOffice(null, CycleManagedObject.class, "DEPENDENCY_ONE_OBJECT", null, this);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link SuppliedManagedObjectSource}.
	 */
	public void testAutoWireSuppliedManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register the supplied managed object source
		Singleton mos = new Singleton(new CompileManagedObject());
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, mos);

		// Should supply and auto-wire the dependency
		final String mosName = CompileManagedObject.class.getName();
		final String moName = CompileManagedObject.class.getName();
		this.record_officeFloorBuilder_addManagedObject(mosName, mos, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource(mosName, mosName);
		this.record_officeBuilder_addThreadManagedObject(moName, mosName);

		// Build the office
		CompileOfficeSource.registerOffice(null, CompileManagedObject.class, moName, null, this);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectDependency} for a
	 * {@link SuppliedManagedObjectSource}.
	 */
	public void testAutoWireSuppliedManagedObjectDependency() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register the supplied managed object source
		Singleton mos = new Singleton(new CompileManagedObject());
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, mos);

		// Build the Managed Object with dependency
		office.registerManagedObjectSource("DEPENDENCY", "DEPENDENCY_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder dependency = this.record_officeBuilder_addProcessManagedObject("DEPENDENCY",
				"DEPENDENCY");

		// Should supply and auto-wire the dependency
		final String mosName = CompileManagedObject.class.getName();
		final String moName = CompileManagedObject.class.getName();
		this.record_officeFloorBuilder_addManagedObject(mosName, mos, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource(mosName, mosName);
		this.record_officeBuilder_addThreadManagedObject(moName, mosName);
		dependency.mapDependency(0, moName);

		// Build the office
		CompileOfficeSource.registerOffice(null, DependencyManagedObject.class, "DEPENDENCY", null, this);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure {@link SuppliedManagedObjectSource} requiring a {@link Flow} is
	 * not available for auto-wiring. Must be manually added with {@link Flow}
	 * configured.
	 * <p>
	 * Purpose of {@link SupplierSource} is integration with object dependency
	 * injection libraries. These are not expected to support
	 * continuation/thread injection.
	 */
	public void testSuppliedManagedObjectWithFlowNotAvailable() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register supplied managed object with flow
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, new FlowManagedObjectSource());

		// Should not supply managed object as requires flow configuration
		this.issues.recordIssue("OBJECT", OfficeObjectNodeImpl.class, "No target found by auto-wiring");
		this.issues.recordIssue("OBJECT", OfficeObjectNodeImpl.class,
				"Office Object OBJECT is not linked to a BoundManagedObjectNode");
		this.issues.recordIssue("OBJECT", OfficeObjectNodeImpl.class,
				"Office Object OBJECT is not linked to a BoundManagedObjectNode");

		// Build the office
		CompileOfficeSource.isBuild = false;
		CompileOfficeSource.registerOffice(null, CompileManagedObject.class, "OBJECT", null, this);
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure {@link SuppliedManagedObjectSource} requiring a {@link Team} is
	 * not available for auto-wiring. Must be manually added with {@link Team}
	 * configured.
	 * <p>
	 * Purpose of {@link SupplierSource} is integration with object dependency
	 * injection libraries. These are not expected to support
	 * continuation/thread injection.
	 */
	public void testSuppliedManagedObjectWithTeamNotAvailable() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register supplied managed object with team
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, new TeamManagedObjectSource());

		// Should not supply managed object as requires flow configuration
		this.issues.recordIssue("OBJECT", OfficeObjectNodeImpl.class, "No target found by auto-wiring");
		this.issues.recordIssue("OBJECT", OfficeObjectNodeImpl.class,
				"Office Object OBJECT is not linked to a BoundManagedObjectNode");
		this.issues.recordIssue("OBJECT", OfficeObjectNodeImpl.class,
				"Office Object OBJECT is not linked to a BoundManagedObjectNode");

		// Build the office
		CompileOfficeSource.isBuild = false;
		CompileOfficeSource.registerOffice(null, CompileManagedObject.class, "OBJECT", null, this);
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link OfficeFloorTeam}.
	 */
	public void testAutoWireOfficeFloorTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeFloorExtensionService.enableAutoWireTeams();

		// Record building the OfficeFloor (with auto-wire of team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Auto-wire the team
		office.registerTeam("OFFICE_TEAM", "OFFICEFLOOR_TEAM");

		// Build the office
		CompileOfficeSource.registerOffice(null, CompileManagedObject.class, null, "OFFICE_TEAM", this);

		// Compile the OfficeFloor
		this.compile(true);
	}

	public static class CompileManagedObject {
	}

	public static class DependencyManagedObject {
		@Dependency
		private CompileManagedObject dependency;
	}

	public static class CycleManagedObject extends CompileManagedObject {
		@Dependency
		private DependencyManagedObject dependency;
	}

	public static class ProcessManagedObject extends CompileManagedObject {

		@FlowInterface
		public static interface Flows {
			void doFlow();
		}

		Flows flows;
	}

	public static class DependencyProcessManagedObject extends ProcessManagedObject {
		@Dependency
		private CompileManagedObject dependency;
	}

	@TestSource
	public static class CompileSupplierSource extends AbstractSupplierSource {

		private static class SuppliedInstance {
			private final String qualifier;
			private final Class<?> type;
			private final ManagedObjectSource<?, ?> managedObjectSource;
			private final String[] propertyNameValuePairs;

			public SuppliedInstance(String qualifier, Class<?> type, ManagedObjectSource<?, ?> managedObjectSource,
					String[] propertyNameValuePairs) {
				this.qualifier = qualifier;
				this.type = type;
				this.managedObjectSource = managedObjectSource;
				this.propertyNameValuePairs = propertyNameValuePairs;
			}
		}

		private static final List<SuppliedInstance> supplied = new LinkedList<>();

		public static void reset() {
			supplied.clear();
		}

		public static void addSuppliedManagedObjectSource(String qualifier, Class<?> type,
				ManagedObjectSource<?, ?> managedObjectSource, String... propertyNameValuePairs) {
			supplied.add(new SuppliedInstance(qualifier, type, managedObjectSource, propertyNameValuePairs));
		}

		public static void addSuppliedManagedObjectSource(Class<?> type, ManagedObjectSource<?, ?> managedObjectSource,
				String... propertyNameValuePairs) {
			addSuppliedManagedObjectSource(null, type, managedObjectSource, propertyNameValuePairs);
		}

		/*
		 * ================= SupplierSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			for (SuppliedInstance instance : supplied) {
				SuppliedManagedObjectSource mos = context.addManagedObjectSource(instance.qualifier, instance.type,
						instance.managedObjectSource);
				for (int i = 0; i < instance.propertyNameValuePairs.length; i += 2) {
					String name = instance.propertyNameValuePairs[i];
					String value = instance.propertyNameValuePairs[i + 1];
					mos.addProperty(name, value);
				}
			}
		}
	}

	@TestSource
	public static class CompileOfficeSource extends AbstractOfficeSource {

		private static CompileSectionSource sectionSource = null;

		private static String objectQualifier = null;

		private static String officeTeamName = null;

		private static boolean isBuild = true;

		public static void reset() {
			sectionSource = null;
			objectQualifier = null;
			officeTeamName = null;
			isBuild = true;
		}

		public static void registerOffice(String dependencyQualifier, Class<?> dependencyType, String managedObjectName,
				String teamName, AutoWireOfficeFloorTest testCase) {

			// Register the section source for use
			sectionSource = new CompileSectionSource(dependencyType, managedObjectName);
			objectQualifier = dependencyQualifier;
			officeTeamName = teamName;

			// Determine if build (helps debugging)
			if (!isBuild) {
				return;
			}

			// Record adding the managed function
			ManagedFunctionBuilder<?, ?> function = testCase.record_officeBuilder_addFunction("SECTION", "FUNCTION");
			if (managedObjectName != null) {
				function.linkManagedObject(0, managedObjectName, sectionSource.namespace.dependencyType);
			}
			if (teamName != null) {
				function.setResponsibleTeam(teamName);
			}
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {

			// Add section
			OfficeSection section = officeArchitect.addOfficeSection("SECTION", sectionSource, null);

			// Register the object (if required)
			if (sectionSource.namespace.managedObjectName != null) {
				OfficeObject object = officeArchitect.addOfficeObject("OBJECT",
						sectionSource.namespace.dependencyType.getName());
				if (objectQualifier != null) {
					object.setTypeQualifier(objectQualifier);
				}
				OfficeSectionObject sectionObject = section.getOfficeSectionObject("OBJECT");
				officeArchitect.link(sectionObject, object);
			}

			// Register the office team (if required)
			if (officeTeamName != null) {
				OfficeTeam team = officeArchitect.addOfficeTeam(officeTeamName);
				team.addTypeQualification(null, sectionSource.namespace.dependencyType.getName());
				officeArchitect.link(section.getOfficeSectionFunction("FUNCTION").getResponsibleTeam(), team);
			}
		}
	}

	@TestSource
	public static class CompileSectionSource extends AbstractSectionSource {

		private final CompileManagedFunctionSource namespace;

		public CompileSectionSource(Class<?> dependencyType, String managedObjectName) {
			this.namespace = new CompileManagedFunctionSource(dependencyType, managedObjectName);
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

			// Add the managed function (with input)
			SectionFunction function = designer.addSectionFunctionNamespace("NAMESPACE", namespace)
					.addSectionFunction("FUNCTION", "FUNCTION");
			designer.link(designer.addSectionInput("INPUT", null), function);

			// Add the dependency (if required)
			if (this.namespace.managedObjectName != null) {
				designer.link(function.getFunctionObject("OBJECT"),
						designer.addSectionObject("OBJECT", this.namespace.dependencyType.getName()));
			}
		}
	}

	@TestSource
	public static class CompileManagedFunctionSource extends AbstractManagedFunctionSource
			implements ManagedFunction<Indexed, None> {

		private final Class<?> dependencyType;

		private final String managedObjectName;

		public CompileManagedFunctionSource(Class<?> dependencyType, String managedObjectName) {
			this.dependencyType = dependencyType;
			this.managedObjectName = managedObjectName;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			ManagedFunctionTypeBuilder<Indexed, None> function = functionNamespaceTypeBuilder
					.addManagedFunctionType("FUNCTION", () -> this, Indexed.class, None.class);
			if (this.managedObjectName != null) {
				ManagedFunctionObjectTypeBuilder<?> dependency = function.addObject(this.dependencyType);
				dependency.setLabel("OBJECT");
			}
		}

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {
			return null;
		}
	}

	@TestSource
	public static class FlowManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedObject {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(this.getClass());

			// Require flow
			context.addFlow(String.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	@TestSource
	public static class TeamManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedObject, ManagedFunction<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(this.getClass());

			// Require team
			context.getManagedObjectSourceContext().getRecycleFunction(() -> this).setResponsibleTeam("MO_TEAM");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		@Override
		public Object execute(ManagedFunctionContext<None, None> context) throws Throwable {
			return null;
		}
	}

}