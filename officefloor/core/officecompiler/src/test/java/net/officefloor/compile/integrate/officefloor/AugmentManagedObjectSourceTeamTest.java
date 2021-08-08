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

package net.officefloor.compile.integrate.officefloor;

import java.util.function.BiConsumer;

import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.integrate.officefloor.AugmentManagedObjectSourceFlowTest.AugmentFlowManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to augment {@link ManagedObjectSource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AugmentManagedObjectSourceTeamTest extends OfficeFrameTestCase {

	/**
	 * Ensure notifies of issue.
	 */
	public void testAddIssue() throws Exception {

		// Record the issues
		final Exception exception = new Exception("TEST");
		MockCompilerIssues issues = new MockCompilerIssues(this);
		issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class, "Issue One");
		issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class, "Issue Two", exception);

		// Test
		this.replayMockObjects();
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler().setCompilerIssues(issues);
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Augment
			deployer.addManagedObjectSourceAugmentor((augment) -> {

				// Add the issue
				augment.addIssue("Issue One");

				// Add issue with exception
				throw augment.addIssue("Issue Two", exception);
			});
		});
		compile.office((context) -> {
			// Add managed object source (to ensure visit)
			context.getOfficeArchitect().addOfficeManagedObjectSource("MOS",
					AugmentFlowManagedObjectSource.class.getName());
		});
		assertNull("Should not compile", compile.compileOfficeFloor());
		this.verifyMockObjects();
	}

	/**
	 * Ensure indicates already linked.
	 */
	public void testAugmentOfficeFloorManagedObjectSourceTeamAlreadyLinked() throws Exception {
		this.doAugmentedManagedObjectSourceTest("MOS", true, null);
	}

	/**
	 * Ensure can augment the {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAugmentOfficeFloorManagedObjectSourceTeam() throws Exception {
		this.doAugmentedManagedObjectSourceTest("MOS", false, (compile, mos) -> {
			compile.officeFloor((context) -> this.createOfficeFloorManagedObjectSource(context, mos));
		});
	}

	private OfficeFloorManagedObjectSource createOfficeFloorManagedObjectSource(CompileOfficeFloorContext context,
			AugmentTeamManagedObjectSource mos) {
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
		OfficeFloorManagedObjectSource source = deployer.addManagedObjectSource("MOS", mos);
		deployer.link(source.getManagingOffice(), context.getDeployedOffice());
		OfficeFloorInputManagedObject inputMo = deployer.addInputManagedObject("MOS",
				AugmentTeamManagedObjectSource.class.getName());
		deployer.link(source, inputMo);
		return source;
	}

	/**
	 * Ensure can augment the {@link OfficeManagedObjectSource}.
	 */
	public void testAugmentOfficeManagedObjectSourceTeam() throws Exception {
		this.doAugmentedManagedObjectSourceTest("OFFICE.MOS", false, (compile, mos) -> {
			compile.office((context) -> {
				context.getOfficeArchitect().addOfficeManagedObjectSource("MOS", mos);
			});
		});
	}

	/**
	 * Ensure can augment the {@link SectionManagedObjectSource}.
	 */
	public void testAugmentSectionManagedObjectSourceTeam() throws Exception {
		this.doAugmentedManagedObjectSourceTest("OFFICE.SECTION.MOS", false, (compile, mos) -> {
			compile.section((context) -> {
				context.getSectionDesigner().addSectionManagedObjectSource("MOS", mos);
			});
		});
	}

	/**
	 * Undertakes the augment {@link ManagedObjectSource} test.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param isAlreadyLinked         Indicates if already linked.
	 * @param loader                  Loads the {@link ManagedObjectSource}.
	 */
	private void doAugmentedManagedObjectSourceTest(String managedObjectSourceName, boolean isAlreadyLinked,
			BiConsumer<CompileOfficeFloor, AugmentTeamManagedObjectSource> loader) throws Exception {

		// Create the augment
		AugmentTeamManagedObjectSource mos = new AugmentTeamManagedObjectSource();
		AugmentTeamSource teamSource = new AugmentTeamSource();

		// Compile with the augmented managed object source
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Add the team
			OfficeFloorTeam team = deployer.addTeam("TEAM", teamSource);

			// Determine if already linked
			if (isAlreadyLinked) {
				OfficeFloorManagedObjectSource source = this.createOfficeFloorManagedObjectSource(context, mos);
				deployer.link(source.getOfficeFloorManagedObjectTeam("TEAM"), team);
			}

			// Augment the managed object source
			deployer.addManagedObjectSourceAugmentor((augment) -> {

				// Ensure correct name
				assertEquals("Incorrect managed object source name", managedObjectSourceName,
						augment.getManagedObjectSourceName());
				assertNotNull("Should have managed object type", augment.getManagedObjectType());

				// Obtain the team
				AugmentedManagedObjectTeam responsibility = augment.getManagedObjectTeam("TEAM");
				assertEquals("Incorrectly already linked", isAlreadyLinked, responsibility.isLinked());

				// Possibly link team
				if (!responsibility.isLinked()) {
					assertEquals("Incorrect team name", "TEAM", responsibility.getManagedObjectTeamName());
					augment.link(responsibility, team);
				}
			});
		});
		if (!isAlreadyLinked) {
			loader.accept(compile, mos);
		}
		compile.office((context) -> {
			// Ensure office available
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Execute the flow (ensures augmented)
			mos.serviceContext.invokeProcess(Flows.FLOW, null, mos, 0, null);
			assertNotNull("Should have job from team", teamSource.job);
			assertFalse("Should not yet invoke function", mos.isInvoked);

			// Undertake job (ensuring invokes function)
			teamSource.job.run();
			assertTrue("Should now invoke function", mos.isInvoked);
		}
	}

	@TestSource
	public static class AugmentTeamSource extends AbstractTeamSource implements Team {

		private Job job;

		/*
		 * ==================== TeamSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ======================== Team ============================
		 */

		@Override
		public void startWorking() {
		}

		@Override
		public void assignJob(Job job) {
			this.job = job;
		}

		@Override
		public void stopWorking() {
		}
	}

	public static enum Flows {
		FLOW
	}

	@TestSource
	public static class AugmentTeamManagedObjectSource extends AbstractManagedObjectSource<None, Flows>
			implements ManagedObject {

		private ManagedObjectServiceContext<Flows> serviceContext;

		private boolean isInvoked = false;

		/*
		 * =================== ManagedObjectSource ============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.addFlow(Flows.FLOW, null);

			// Configure the flow
			ManagedObjectSourceContext<Flows> mosContext = context.getManagedObjectSourceContext();
			mosContext.addManagedFunction("FUNCTION", () -> (functionContext) -> this.isInvoked = true)
					.setResponsibleTeam("TEAM");
			mosContext.getFlow(Flows.FLOW).linkFunction("FUNCTION");
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.serviceContext = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not obtain managed object");
			return null;
		}

		/*
		 * ====================== ManagedObject ==================================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
