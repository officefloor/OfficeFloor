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

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Ensure able to override the {@link PropertyList} for various aspects of the
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeOverridePropertiesTest extends AbstractCompileTestCase {

	/**
	 * Ensure can override {@link Property} for the {@link OfficeSection} via
	 * directory files.
	 */
	public void testOverrideSectionPropertiesViaDirectory() {
		this.enableOverrideProperties();
		this.doOverrideSectionPropertiesTest();
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeSection} via
	 * override {@link Property}.
	 */
	public void testOverrideSectionPropertiesViaOverrides() {
		OfficeOverridePropertiesExtensionService.runWithProperties(() -> this.doOverrideSectionPropertiesTest(),
				"SECTION.function.name", "overridden_function", "SECTION.additional", "another");
	}

	private void doOverrideSectionPropertiesTest() {

		// Record creating the section types
		this.issues.recordCaptureIssues(false);

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "another");
		this.record_officeBuilder_addFunction("SECTION", "overridden_function");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeGovernance}.
	 */
	public void testOverrideGovernancePropertiesViaDirectory() {
		this.enableOverrideProperties();
		this.doOverrideGovernancePropertiesTest();
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeGovernance} via
	 * override {@link Property}.
	 */
	public void testOverrideGovernancePropertiesViaOverrides() {
		OfficeOverridePropertiesExtensionService.runWithProperties(() -> this.doOverrideGovernancePropertiesTest(),
				"OVERRIDE_GOVERNANCE.class.name", CompileGovernance.class.getName(), "OVERRIDE_GOVERNANCE.additional",
				"another");
	}

	private void doOverrideGovernancePropertiesTest() {

		// Record creating the section types
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addGovernance("OVERRIDE_GOVERNANCE", ClassGovernanceSource.class,
				CompileManagedObject.class);
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileFunction.class, "function");
		function.addGovernance("OVERRIDE_GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeAdministration}.
	 */
	public void testOverrideAdministrationPropertiesViaDirectory() {
		this.enableOverrideProperties();
		this.doOverrideAdministrationPropertiesTest();
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeAdministration} via
	 * override {@link Property}.
	 */
	public void testOverrideAdministrationPropertiesViaOverrides() {
		OfficeOverridePropertiesExtensionService.runWithProperties(() -> this.doOverrideAdministrationPropertiesTest(),
				"OVERRIDE_ADMINISTRATION.class.name", CompileAdministration.class.getName(),
				"OVERRIDE_ADMINISTRATION.additional", "another");
	}

	private void doOverrideAdministrationPropertiesTest() {

		// Record creating the section types
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION", CompileFunction.class, "function");
		this.record_functionBuilder_preAdministration("OVERRIDE_ADMINISTRATION", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link OfficeManagedObjectSource}.
	 */
	public void testOverrideManagedObjectSourcePropertiesViaDirectory() {
		this.enableOverrideProperties();
		this.doOverrideManagedObjectSourcePropertiesTest();
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link OfficeManagedObjectSource} via override {@link Property}.
	 */
	public void testOverrideManagedObjectSourcePropertiesViaOverrides() {
		OfficeOverridePropertiesExtensionService.runWithProperties(
				() -> this.doOverrideManagedObjectSourcePropertiesTest(), "OVERRIDE_MANAGED_OBJECT_SOURCE.class.name",
				CompileManagedObject.class.getName(), "OVERRIDE_MANAGED_OBJECT_SOURCE.additional", "another");
	}

	private void doOverrideManagedObjectSourcePropertiesTest() {

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.OVERRIDE_MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName(), "additional",
				"another");
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeSupplier}.
	 */
	public void testOverrideSupplierSourcePropertiesViaDirectory() {
		this.enableOverrideProperties();
		this.doOverrideSupplierSourcePropertiesTest();
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeSupplier} via
	 * override {@link Property}.
	 */
	public void testOverrideSupplierSourcePropertiesViaOverrides() {
		OfficeOverridePropertiesExtensionService.runWithProperties(() -> this.doOverrideSupplierSourcePropertiesTest(),
				"OVERRIDE_SUPPLIER.override", "SUPPLY_OVERRIDE");
	}

	private void doOverrideSupplierSourcePropertiesTest() {

		// Record the OfficeFloor
		CompileSupplierSource.propertyValue = null;
		this.record_supplierSetup();
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
		assertEquals("Should override property value", "SUPPLY_OVERRIDE", CompileSupplierSource.propertyValue);
	}

	public static class CompileManagedObject {
	}

	@TestSource
	public static class TestSectionSource extends AbstractSectionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("namespace");
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("NAMESPACE",
					ClassManagedFunctionSource.class.getName());
			namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, CompileFunction.class.getName());
			String functionName = context.getProperty("function.name");
			namespace.addSectionFunction(functionName, "function");
			String additional = context.getProperty("additional", null);
			if (additional != null) {
				namespace.addSectionFunction(additional, "function");
			}
		}
	}

	public static class CompileFunction {
		public void function() {
		}
	}

	public static class CompileAdministration {
		public void administer(CompileManagedObject[] extensions) {
		}
	}

	public static class CompileGovernance {
		@Govern
		public void govern(CompileManagedObject extensions) {
		}

		@Enforce
		public void enforce() {
		}
	}

	@TestSource
	public static class CompileSupplierSource extends AbstractSupplierSource {

		private static String propertyValue = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			propertyValue = context.getProperty("override");
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

}
