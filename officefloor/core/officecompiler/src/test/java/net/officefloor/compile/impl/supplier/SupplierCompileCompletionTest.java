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

package net.officefloor.compile.impl.supplier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionManagedObjectPool;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompileContext;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to capture compile with {@link SupplierCompileCompletion}.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierCompileCompletionTest extends OfficeFrameTestCase {

	/**
	 * Ensure capture {@link OfficeFloor} scope for {@link SupplierSource}.
	 */
	public void testOfficeFloorCompileScope() {

		// Capture the sources
		CompleteSupplierSource completeSupplier = new CompleteSupplierSource();

		// Provide sources to capture the profiles
		MockManagedObjectSource officeFloorMos = new MockManagedObjectSource();
		MockManagedObjectPoolSource officeFloorPool = new MockManagedObjectPoolSource();
		MockTeamSource team = new MockTeamSource();
		MockExecutiveSource executive = new MockExecutiveSource();
		MockManagedObjectSource officeMos = new MockManagedObjectSource();
		MockManagedObjectPoolSource officePool = new MockManagedObjectPoolSource();
		MockSupplierSource officeSupplier = new MockSupplierSource();
		MockAdministrationSource admin = new MockAdministrationSource();
		MockGovernanceSource govern = new MockGovernanceSource();
		MockManagedObjectSource sectionMos = new MockManagedObjectSource();
		MockManagedObjectPoolSource sectionPool = new MockManagedObjectPoolSource();
		MockManagedFunctionSource function = new MockManagedFunctionSource();

		// Compile
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			DeployedOffice office = context.getDeployedOffice();

			// Managed Object
			OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("MOS", officeFloorMos);
			deployer.link(mos.getManagingOffice(), office);
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.THREAD);

			// Managed Object Pool
			OfficeFloorManagedObjectPool pool = deployer.addManagedObjectPool("POOL", officeFloorPool);
			deployer.link(mos, pool);

			// Team
			deployer.addTeam("TEAM", team);

			// Executive
			deployer.setExecutive(executive);

			// Supplier
			deployer.addSupplier("SUPPLIER", completeSupplier);

			// Only adding, so should not have sources
			// (Note compiling of added items happens after OfficeFloor sourcing)
			assertNull("Supplier should yet be created", compileScopedSources.get());
		});
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Ensure the supplier is now available
			assertNotNull("Supplier should have setup context", compileScopedSources.get());

			// Managed Object
			OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("MOS", officeMos);
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);

			// Managed Object Pool
			OfficeManagedObjectPool pool = office.addManagedObjectPool("POOL", officePool);
			office.link(mos, pool);

			// Supplier
			office.addSupplier("SUPPLIER", officeSupplier);

			// Administration
			OfficeAdministration administration = office.addOfficeAdministration("ADMIN", admin);
			OfficeSection section = office.getOfficeSection("SECTION");
			section.getOfficeSectionFunction("function").addPreAdministration(administration);

			// Governance
			office.addOfficeGovernance("GOVERNANCE", govern);
		});
		compiler.section((context) -> {
			SectionDesigner designer = context.getSectionDesigner();

			// Managed Object
			SectionManagedObjectSource mos = designer.addSectionManagedObjectSource("MOS", sectionMos);
			mos.addSectionManagedObject("MO", ManagedObjectScope.THREAD);

			// Managed Object Pool
			SectionManagedObjectPool pool = designer.addManagedObjectPool("POOL", sectionPool);
			designer.link(mos, pool);

			// Function
			designer.addSectionFunctionNamespace("FUNCTION", function).addSectionFunction("function", "function");
		});

		// Undertake compiling with clean state
		compileScopedSources.remove();
		try (OfficeFloor officeFloor = compiler.compileOfficeFloor()) {

			// Ensure complete supplier source context
			assertCompletedSupplierCompileContext(completeSupplier.initialContext);
			assertCompletedSupplierCompileContext(completeSupplier.completeContext);

			// Ensure all functionality are added
			assertTrue("Missing (OfficeFloor) supplier", completeSupplier.completed.contains(completeSupplier));
			assertTrue("Missing (OfficeFloor) managed object", completeSupplier.completed.contains(officeFloorMos));
			assertTrue("Missing (OfficeFloor) managed object pool",
					completeSupplier.completed.contains(officeFloorPool));
			assertTrue("Missing team", completeSupplier.completed.contains(team));
			assertTrue("Missing executive", completeSupplier.completed.contains(executive));
			assertTrue("Missing (office) managed object", completeSupplier.completed.contains(officeMos));
			assertTrue("Missing (office) managed object pool", completeSupplier.completed.contains(officePool));
			assertTrue("Missing (office) supplier", completeSupplier.completed.contains(officeSupplier));
			assertTrue("Missing administration", completeSupplier.completed.contains(admin));
			assertTrue("Missing governance", completeSupplier.completed.contains(govern));
			assertTrue("Missing (section) managed object", completeSupplier.completed.contains(sectionMos));
			assertTrue("Missing (section) managed object pool", completeSupplier.completed.contains(sectionPool));
			assertTrue("Missing function", completeSupplier.completed.contains(function));
			assertEquals("Incorrect number of sources in scope: " + completeSupplier.completed, 13,
					completeSupplier.completed.size());

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Ensure capture {@link Office} scope for {@link SupplierSource}.
	 */
	public void testOfficeCompileScope() {

		// Capture the sources
		CompleteSupplierSource completeSupplier = new CompleteSupplierSource();

		// Provide sources to capture the profiles
		MockManagedObjectSource officeMos = new MockManagedObjectSource();
		MockManagedObjectPoolSource officePool = new MockManagedObjectPoolSource();
		MockAdministrationSource admin = new MockAdministrationSource();
		MockGovernanceSource govern = new MockGovernanceSource();
		MockManagedObjectSource sectionMos = new MockManagedObjectSource();
		MockManagedObjectPoolSource sectionPool = new MockManagedObjectPoolSource();
		MockManagedFunctionSource function = new MockManagedFunctionSource();

		// Compile
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Managed Object
			OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("MOS", officeMos);
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);

			// Managed Object Pool
			OfficeManagedObjectPool pool = office.addManagedObjectPool("POOL", officePool);
			office.link(mos, pool);

			// Administration
			OfficeAdministration administration = office.addOfficeAdministration("ADMIN", admin);
			OfficeSection section = office.getOfficeSection("SECTION");
			section.getOfficeSectionFunction("function").addPreAdministration(administration);

			// Governance
			office.addOfficeGovernance("GOVERNANCE", govern);

			// Supplier
			office.addSupplier("SUPPLIER", completeSupplier);

			// Only adding, so should not have sources
			// (Note compiling of added items happens after Office sourcing)
			assertNull("Supplier should yet be created", compileScopedSources.get());
		});
		compiler.section((context) -> {
			SectionDesigner designer = context.getSectionDesigner();

			// Managed Object
			SectionManagedObjectSource mos = designer.addSectionManagedObjectSource("MOS", sectionMos);
			mos.addSectionManagedObject("MO", ManagedObjectScope.THREAD);

			// Managed Object Pool
			SectionManagedObjectPool pool = designer.addManagedObjectPool("POOL", sectionPool);
			designer.link(mos, pool);

			// Function
			designer.addSectionFunctionNamespace("FUNCTION", function).addSectionFunction("function", "function");
		});

		// Undertake compiling with clean state
		compileScopedSources.remove();
		try (OfficeFloor officeFloor = compiler.compileOfficeFloor()) {

			// Ensure complete supplier source context
			assertCompletedSupplierCompileContext(completeSupplier.initialContext);
			assertCompletedSupplierCompileContext(completeSupplier.completeContext);

			// Ensure all functionality are added
			assertTrue("Missing supplier", completeSupplier.completed.contains(completeSupplier));
			assertTrue("Missing (office) managed object", completeSupplier.completed.contains(officeMos));
			assertTrue("Missing (office) managed object pool", completeSupplier.completed.contains(officePool));
			assertTrue("Missing administration", completeSupplier.completed.contains(admin));
			assertTrue("Missing governance", completeSupplier.completed.contains(govern));
			assertTrue("Missing (section) managed object", completeSupplier.completed.contains(sectionMos));
			assertTrue("Missing (section) managed object pool", completeSupplier.completed.contains(sectionPool));
			assertTrue("Missing function", completeSupplier.completed.contains(function));
			assertEquals("Incorrect number of sources in scope: " + completeSupplier.completed, 8,
					completeSupplier.completed.size());

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	private static void assertCompletedSupplierCompileContext(SupplierCompileContext context) {
		assertIllegalStateException(() -> context.addManagedObjectSource(null, null, null),
				"Unable to add further ManagedObject as SupplierSource loaded");
		assertIllegalStateException(() -> context.addSupplierThreadLocal(null, null),
				"Unable to add further SupplierThreadLocal as SupplierSource loaded");
		assertIllegalStateException(() -> context.addThreadSynchroniser(null),
				"Unable to add further ThreadSynchroniser as SupplierSource loaded");
		assertIllegalStateException(() -> context.addInternalSupplier(null),
				"Unable to add further InternalSupplier as SupplierSource loaded");
		assertIllegalStateException(() -> ((SupplierSourceContext) context).addCompileCompletion(null),
				"Unable to add further SupplierCompileCompletion as SupplierSource completing");
	}

	private static void assertIllegalStateException(Runnable logic, String message) {
		try {
			logic.run();
			fail("Should not be successful. " + message);
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", message, ex.getMessage());
		}
	}

	private static final ThreadLocal<List<Object>> compileScopedSources = new ThreadLocal<List<Object>>();

	@TestSource
	private static class CompleteSupplierSource extends AbstractSupplierSource {

		private final List<Object> completed = new ArrayList<>();

		/**
		 * Initial {@link SupplierSourceContext}.
		 */
		private SupplierSourceContext initialContext;

		/**
		 * Completion {@link SupplierCompileContext}.
		 */
		private SupplierCompileContext completeContext;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			this.initialContext = context;

			// Clear scoped objects
			LinkedList<Object> sources = new LinkedList<>();
			sources.add(this);
			compileScopedSources.set(sources);

			// Capture sources on completion
			context.addCompileCompletion((completion) -> {
				this.completeContext = completion;
				this.completed.addAll(compileScopedSources.get());
			});
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

	@TestSource
	private static class MockSupplierSource extends AbstractSupplierSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			compileScopedSources.get().add(this);
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

	@TestSource
	private static class MockManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
			compileScopedSources.get().add(this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not be run");
			return null;
		}
	}

	@TestSource
	private static class MockManagedObjectPoolSource extends AbstractManagedObjectPoolSource
			implements ManagedObjectPoolFactory {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {
			context.setPooledObjectType(Object.class);
			context.setManagedObjectPoolFactory(this);
			compileScopedSources.get().add(this);
		}

		@Override
		public ManagedObjectPool createManagedObjectPool(ManagedObjectPoolContext managedObjectPoolContext)
				throws Throwable {
			return null;
		}
	}

	@TestSource
	private static class MockTeamSource extends AbstractTeamSource implements Team {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			compileScopedSources.get().add(this);
			return this;
		}

		@Override
		public void startWorking() {
			// nothing to start
		}

		@Override
		public void assignJob(Job job) throws TeamOverloadException, Exception {
			fail("Should not run");
		}

		@Override
		public void stopWorking() {
			// nothing to stop
		}
	}

	@TestSource
	private static class MockExecutiveSource extends DefaultExecutive {

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			compileScopedSources.get().add(this);
			return super.createExecutive(context);
		}
	}

	@TestSource
	private static class MockAdministrationSource extends AbstractAdministrationSource<Object, None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None, None> context) throws Exception {
			context.setExtensionInterface(Object.class);
			context.setAdministrationFactory(() -> (adminContext) -> {
			});
			compileScopedSources.get().add(this);
		}
	}

	@TestSource
	private static class MockGovernanceSource extends AbstractGovernanceSource<Object, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None> context) throws Exception {
			context.setExtensionInterface(Object.class);
			context.setGovernanceFactory(() -> null);
			compileScopedSources.get().add(this);
		}
	}

	@TestSource
	private static class MockManagedFunctionSource extends AbstractManagedFunctionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			functionNamespaceTypeBuilder.addManagedFunctionType("function", None.class, None.class)
					.setFunctionFactory(() -> (mfContext) -> {
					});
			compileScopedSources.get().add(this);
		}
	}

}
