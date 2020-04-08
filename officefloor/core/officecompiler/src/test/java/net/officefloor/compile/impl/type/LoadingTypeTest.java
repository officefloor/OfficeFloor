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

package net.officefloor.compile.impl.type;

import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure correct indicating of loading type.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadingTypeTest extends OfficeFrameTestCase {

	/**
	 * Ensure {@link OfficeFloor}.
	 */
	public void testOfficeFloor() throws Exception {

		// Load instances
		TypeOfficeSource office = new TypeOfficeSource();
		TypeManagedObjectSource mos = new TypeManagedObjectSource();
		TypeManagedObjectPoolSource pool = new TypeManagedObjectPoolSource();
		TypeSupplierSource supplier = new TypeSupplierSource();
		TypeTeamSource team = new TypeTeamSource();
		TypeExecutiveSource executive = new TypeExecutiveSource();

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			OfficeFloorSourceContext sourceContext = context.getOfficeFloorSourceContext();

			// Office type
			TypeOfficeSource officeType = new TypeOfficeSource();
			sourceContext.loadOfficeType("type", officeType, null, null);
			assertTrue("Office type", officeType.isType);

			// Office
			DeployedOffice linkOffice = deployer.addDeployedOffice("office", office, null);

			// Managed Object type
			TypeManagedObjectSource mosType = new TypeManagedObjectSource();
			sourceContext.loadManagedObjectType("type", mosType, null);
			assertTrue("MOS type", mosType.isType);

			// Managed Object
			OfficeFloorManagedObjectSource linkMos = deployer.addManagedObjectSource("mos", mos);
			deployer.link(linkMos.getManagingOffice(), linkOffice);

			// Managed Object Pool
			deployer.addManagedObjectPool("pool", pool);

			// Supplier type
			TypeSupplierSource supplierType = new TypeSupplierSource();
			sourceContext.loadSupplierType("type", supplierType, null);
			assertTrue("Supplier type", supplierType.isType);

			// Supplier
			deployer.addSupplier("supplier", supplier);

			// Team
			deployer.addTeam("team", team);

			// Executive
			deployer.setExecutive(executive);
		});
		compile.compileOfficeFloor();

		// Validate loading
		assertFalse("Office", office.isType);
		assertFalse("MOS", mos.isType);
		assertFalse("Pool", pool.isType);
		assertFalse("Supplier", supplier.isType);
		assertFalse("Team", team.isType);
		assertFalse("Executive", executive.isType);
	}

	/**
	 * Ensure {@link Office}.
	 */
	public void testOffice() throws Exception {

		// Load instances
		TypeSectionSource section = new TypeSectionSource();
		TypeManagedObjectSource mos = new TypeManagedObjectSource();
		TypeManagedObjectPoolSource pool = new TypeManagedObjectPoolSource();
		TypeSupplierSource supplier = new TypeSupplierSource();
		TypeAdministrationSource admin = new TypeAdministrationSource();
		TypeGovernanceSource governance = new TypeGovernanceSource();

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();
			OfficeSourceContext sourceContext = context.getOfficeSourceContext();

			// Section type
			TypeSectionSource sectionType = new TypeSectionSource();
			sourceContext.loadOfficeSectionType("section", sectionType, null, null);
			assertTrue("Section type", sectionType.isType);

			// Section
			architect.addOfficeSection("section", section, null);

			// Managed Object type
			TypeManagedObjectSource mosType = new TypeManagedObjectSource();
			sourceContext.loadManagedObjectType("type", mosType, null);
			assertTrue("MOS type", mosType.isType);

			// Managed Object
			architect.addOfficeManagedObjectSource("mos", mos);

			// Managed Object Pool
			architect.addManagedObjectPool("pool", pool);

			// Supplier type
			TypeSupplierSource supplierType = new TypeSupplierSource();
			sourceContext.loadSupplierType("type", supplierType, null);
			assertTrue("Supplier type", supplierType.isType);

			// Supplier
			architect.addSupplier("supplier", supplier);

			// Admin type
			TypeAdministrationSource adminType = new TypeAdministrationSource();
			sourceContext.loadAdministrationType("type", adminType, null);
			assertTrue("Admin type", adminType.isType);

			// Admin
			architect.addOfficeAdministration("admin", admin);

			// Governance type
			TypeGovernanceSource governanceType = new TypeGovernanceSource();
			sourceContext.loadGovernanceType("type", governanceType, null);
			assertTrue("Governance type", governanceType.isType);

			// Governance
			architect.addOfficeGovernance("governance", governance);
		});
		compile.compileOfficeFloor();

		// Validate loading
		assertFalse("Section", section.isType);
		assertFalse("MOS", mos.isType);
		assertFalse("Pool", pool.isType);
		assertFalse("Supplier", supplier.isType);
		assertFalse("Admin", admin.isType);
		assertFalse("Governance", governance.isType);
	}

	/**
	 * Ensure {@link OfficeSection}.
	 */
	public void testSection() throws Exception {

		// Load instances
		TypeSectionSource section = new TypeSectionSource();
		TypeManagedObjectSource mos = new TypeManagedObjectSource();
		TypeManagedObjectPoolSource pool = new TypeManagedObjectPoolSource();
		TypeManagedFunctionSource function = new TypeManagedFunctionSource();

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
		});
		compile.section((context) -> {
			SectionDesigner designer = context.getSectionDesigner();
			SectionSourceContext sourceContext = context.getSectionSourceContext();

			// Section type
			TypeSectionSource sectionType = new TypeSectionSource();
			sourceContext.loadSectionType("section", sectionType, null, null);
			assertTrue("Section type", sectionType.isType);

			// Section
			designer.addSubSection("section", section, null);

			// Managed Object type
			TypeManagedObjectSource mosType = new TypeManagedObjectSource();
			sourceContext.loadManagedObjectType("type", mosType, null);
			assertTrue("MOS type", mosType.isType);

			// Managed Object
			designer.addSectionManagedObjectSource("mos", mos);

			// Managed Object Pool
			designer.addManagedObjectPool("pool", pool);

			// Managed Function type
			TypeManagedFunctionSource functionType = new TypeManagedFunctionSource();
			sourceContext.loadManagedFunctionType("type", functionType, null);
			assertTrue("Function type", functionType.isType);

			// Managed Function
			designer.addSectionFunctionNamespace("function", function).addSectionFunction("function", "function");
		});
		compile.compileOfficeFloor();

		// Validate loading
		assertFalse("Section", section.isType);
		assertFalse("MOS", mos.isType);
		assertFalse("Pool", pool.isType);
		assertFalse("Function", function.isType);
	}

	@TestSource
	private static class TypeOfficeSource extends AbstractOfficeSource {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			this.isType = context.isLoadingType();
		}
	}

	@TestSource
	private static class TypeManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			this.isType = context.getManagedObjectSourceContext().isLoadingType();
			context.setObjectClass(Object.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return null;
		}
	}

	@TestSource
	private static class TypeManagedObjectPoolSource extends AbstractManagedObjectPoolSource {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {
			this.isType = context.getManagedObjectPoolSourceContext().isLoadingType();
			context.setPooledObjectType(Object.class);
			context.setManagedObjectPoolFactory((poolContext) -> null);
		}
	}

	@TestSource
	private static class TypeSupplierSource extends AbstractSupplierSource {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			this.isType = context.isLoadingType();
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

	@TestSource
	private static class TypeTeamSource extends AbstractTeamSource implements Team {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			this.isType = context.isLoadingType();
			return this;
		}

		@Override
		public void startWorking() {
			// nothing to start
		}

		@Override
		public void assignJob(Job job) throws TeamOverloadException, Exception {
			// only loading type
		}

		@Override
		public void stopWorking() {
			// nothing to stop
		}
	}

	@TestSource
	private static class TypeExecutiveSource extends AbstractExecutiveSource {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			this.isType = context.isLoadingType();
			return new DefaultExecutive(new ThreadFactoryManufacturer(null, null));
		}
	}

	@TestSource
	private static class TypeSectionSource extends AbstractSectionSource {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			this.isType = context.isLoadingType();
		}
	}

	@TestSource
	private static class TypeAdministrationSource extends AbstractAdministrationSource<Object, None, None> {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None, None> context) throws Exception {
			context.setExtensionInterface(Object.class);
			context.setAdministrationFactory(() -> null);
			this.isType = context.getAdministrationSourceContext().isLoadingType();
		}
	}

	@TestSource
	private static class TypeGovernanceSource extends AbstractGovernanceSource<Object, None> {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None> context) throws Exception {
			context.setExtensionInterface(Object.class);
			context.setGovernanceFactory(() -> null);
			this.isType = context.getGovernanceSourceContext().isLoadingType();
		}
	}

	@TestSource
	private static class TypeManagedFunctionSource extends AbstractManagedFunctionSource {

		private Boolean isType = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			functionNamespaceTypeBuilder.addManagedFunctionType("function", () -> null, None.class, None.class);
			this.isType = context.isLoadingType();
		}
	}

}
