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

package net.officefloor.compile.integrate.profile;

import java.util.List;

import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.compile.spi.section.SectionDesigner;
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
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to configure profiles.
 * 
 * @author Daniel Sagenschneider
 */
public class ProfileTest extends OfficeFrameTestCase {

	/**
	 * Ensure handle no additional profiles.
	 */
	public void testNoAdditionalProfiles() {
		this.doProfileTest(null, "profile");
	}

	/**
	 * Ensure can include additional profiles.
	 */
	public void testAdditionalProfiles() {
		this.doProfileTest("additional", "additional", "profile");
	}

	private void doProfileTest(String additionalOfficeProfile, String... officeProfiles) {

		// Provide sources to capture the profiles
		Closure<List<String>> profilesOfficeFloor = new Closure<>();
		Closure<List<String>> profilesOffice = new Closure<>();
		Closure<List<String>> profilesSection = new Closure<>();
		MockManagedObjectSource officeFloorMos = new MockManagedObjectSource();
		MockManagedObjectPoolSource officeFloorPool = new MockManagedObjectPoolSource();
		MockSupplierSource officeFloorSupplier = new MockSupplierSource();
		MockTeamSource team = new MockTeamSource();
		MockExecutiveSource executive = new MockExecutiveSource();
		MockManagedObjectSource officeMos = new MockManagedObjectSource();
		MockSupplierSource officeSupplier = new MockSupplierSource();
		MockAdministrationSource admin = new MockAdministrationSource();
		MockGovernanceSource govern = new MockGovernanceSource();
		MockManagedFunctionSource function = new MockManagedFunctionSource();

		// Compile
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.getOfficeFloorCompiler().addProfile("profile");
		compiler.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			DeployedOffice office = context.getDeployedOffice();
			if (additionalOfficeProfile != null) {
				office.addAdditionalProfile(additionalOfficeProfile);
			}

			// OfficeFloor
			profilesOfficeFloor.value = context.getOfficeFloorSourceContext().getProfiles();

			// Managed Object
			OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("MOS", officeFloorMos);
			deployer.link(mos.getManagingOffice(), office);
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.THREAD);

			// Managed Object Pool
			OfficeFloorManagedObjectPool pool = deployer.addManagedObjectPool("POOL", officeFloorPool);
			deployer.link(mos, pool);

			// Supplier
			deployer.addSupplier("SUPPLIER", officeFloorSupplier);

			// Team
			deployer.addTeam("TEAM", team);

			// Executive
			deployer.setExecutive(executive);
		});
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Office
			profilesOffice.value = context.getOfficeSourceContext().getProfiles();

			// Managed Object
			office.addOfficeManagedObjectSource("MOS", officeMos).addOfficeManagedObject("MO",
					ManagedObjectScope.THREAD);

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

			// Section
			profilesSection.value = context.getSectionSourceContext().getProfiles();

			// Function
			designer.addSectionFunctionNamespace("FUNCTION", function).addSectionFunction("function", "function");
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Verify OfficeFloor (does not add additional profiles)
			final String[] officeFloorProfiles = new String[] { "profile" };
			assertProfiles("OfficeFloor", profilesOfficeFloor.value, officeFloorProfiles);
			assertProfiles("OfficeFloor ManagedObject", officeFloorMos.profiles, officeFloorProfiles);
			assertProfiles("Pool", officeFloorPool.profiles, officeFloorProfiles);
			assertProfiles("OfficeFloor Supplier", officeFloorSupplier.profiles, officeFloorProfiles);
			assertProfiles("Team", team.profiles, officeFloorProfiles);
			assertProfiles("Executive", executive.profiles, officeFloorProfiles);

			// Verify Office (adds additional profiles)
			assertProfiles("Office", profilesOffice.value, officeProfiles);
			assertProfiles("Office ManagedObject", officeMos.profiles, officeProfiles);
			assertProfiles("Office Supplier", officeSupplier.profiles, officeProfiles);
			assertProfiles("Office Administration", admin.profiles, officeProfiles);
			assertProfiles("Office Governance", govern.profiles, officeProfiles);
			assertProfiles("Section", profilesSection.value, officeProfiles);
			assertProfiles("Function", function.profiles, officeProfiles);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	private static void assertProfiles(String message, List<String> profiles, String... expectedProfiles) {
		assertNotNull(message + ": should have profiles", profiles);
		assertEquals(message + ": incorrect number of profiles", expectedProfiles.length, profiles.size());
		for (int i = 0; i < expectedProfiles.length; i++) {
			assertEquals(message + ": incorrect profile " + i, expectedProfiles[i], profiles.get(i));
		}
	}

	@TestSource
	private static class MockManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
			this.profiles = context.getManagedObjectSourceContext().getProfiles();
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

		private List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {
			this.profiles = context.getManagedObjectPoolSourceContext().getProfiles();
			context.setPooledObjectType(Object.class);
			context.setManagedObjectPoolFactory(this);
		}

		@Override
		public ManagedObjectPool createManagedObjectPool(ManagedObjectPoolContext managedObjectPoolContext)
				throws Throwable {
			return null;
		}
	}

	@TestSource
	private static class MockSupplierSource extends AbstractSupplierSource {

		private List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			this.profiles = context.getProfiles();
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

	@TestSource
	private static class MockTeamSource extends AbstractTeamSource implements Team {

		private List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			this.profiles = context.getProfiles();
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

		private List<String> profiles;

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			this.profiles = context.getProfiles();
			return super.createExecutive(context);
		}
	}

	@TestSource
	private static class MockAdministrationSource extends AbstractAdministrationSource<Object, None, None> {

		private List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None, None> context) throws Exception {
			this.profiles = context.getAdministrationSourceContext().getProfiles();
			context.setExtensionInterface(Object.class);
			context.setAdministrationFactory(() -> (adminContext) -> {
			});
		}
	}

	@TestSource
	private static class MockGovernanceSource extends AbstractGovernanceSource<Object, None> {

		private List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None> context) throws Exception {
			this.profiles = context.getGovernanceSourceContext().getProfiles();
			context.setExtensionInterface(Object.class);
			context.setGovernanceFactory(() -> null);
		}
	}

	@TestSource
	private static class MockManagedFunctionSource extends AbstractManagedFunctionSource {

		private List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			this.profiles = context.getProfiles();
			functionNamespaceTypeBuilder.addManagedFunctionType("function", None.class, None.class)
					.setFunctionFactory(() -> (mfContext) -> {
					});
		}
	}

}
