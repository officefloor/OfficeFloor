/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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

import java.util.function.BiConsumer;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to augment {@link ManagedObjectSource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AugmentManagedObjectSourceTeamTest extends OfficeFrameTestCase {

	/**
	 * Ensure can augment the {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAugmentOfficeFloorManagedObjectSourceTeam() throws Exception {
		this.doAugmentedManagedObjectSourceTest("MOS", (compile, mos) -> {
			compile.officeFloor((context) -> {
				OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
				OfficeFloorManagedObjectSource source = deployer.addManagedObjectSource("MOS", mos);
				deployer.link(source.getManagingOffice(), context.getDeployedOffice());
				OfficeFloorInputManagedObject inputMo = deployer.addInputManagedObject("MOS",
						AugmentTeamManagedObjectSource.class.getName());
				deployer.link(source, inputMo);
			});
		});
	}

	/**
	 * Ensure can augment the {@link OfficeManagedObjectSource}.
	 */
	public void testAugmentOfficeManagedObjectSourceTeam() throws Exception {
		this.doAugmentedManagedObjectSourceTest("OFFICE.MOS", (compile, mos) -> {
			compile.office((context) -> {
				context.getOfficeArchitect().addOfficeManagedObjectSource("MOS", mos);
			});
		});
	}

	/**
	 * Ensure can augment the {@link SectionManagedObjectSource}.
	 */
	public void testAugmentSectionManagedObjectSourceTeam() throws Exception {
		this.doAugmentedManagedObjectSourceTest("OFFICE.SECTION.MOS", (compile, mos) -> {
			compile.section((context) -> {
				context.getSectionDesigner().addSectionManagedObjectSource("MOS", mos);
			});
		});
	}

	/**
	 * Undertakes the augment {@link ManagedObjectSource} test.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param loader                  Loads the {@link ManagedObjectSource}.
	 */
	private void doAugmentedManagedObjectSourceTest(String managedObjectSourceName,
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

			// Augment the managed object source
			deployer.addManagedObjectSourceAugmentor((augment) -> {

				// Ensure correct name
				assertEquals("Incorrect managed object source name", managedObjectSourceName,
						augment.getManagedObjectSourceName());

				// Obtain the team
				AugmentedManagedObjectTeam responsibility = augment.getManagedObjectTeam("TEAM");
				assertEquals("Incorrect team name", "TEAM", responsibility.getManagedObjectTeamName());

				// Link the team
				augment.link(responsibility, team);
			});
		});
		loader.accept(compile, mos);
		compile.compileAndOpenOfficeFloor();

		// Execute the flow (ensures augmented)
		mos.executeContext.invokeProcess(Flows.FLOW, null, mos, 0, null);
		assertNotNull("Should have job from team", teamSource.job);
		assertFalse("Should not yet invoke function", mos.isInvoked);

		// Undertake job (ensuring invokes function)
		teamSource.job.run();
		assertTrue("Should now invoke function", mos.isInvoked);
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

		private ManagedObjectExecuteContext<Flows> executeContext;

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
			mosContext.addManagedFunction("FUNCTION", () -> (functionContext) -> {
				this.isInvoked = true;
				return null;
			}).setResponsibleTeam("TEAM");
			mosContext.getFlow(Flows.FLOW).linkFunction("FUNCTION");
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.executeContext = context;
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