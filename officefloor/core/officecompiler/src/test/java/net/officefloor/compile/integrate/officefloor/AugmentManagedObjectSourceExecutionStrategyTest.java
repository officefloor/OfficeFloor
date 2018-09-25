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

import java.util.concurrent.ThreadFactory;
import java.util.function.BiConsumer;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to augment {@link ManagedObjectSource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AugmentManagedObjectSourceExecutionStrategyTest extends OfficeFrameTestCase {

	/**
	 * Ensure can augment the {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAugmentOfficeFloorManagedObjectSourceExecutionStrategy() throws Exception {
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
	public void testAugmentOfficeManagedObjectSourceExecutionStrategy() throws Exception {
		this.doAugmentedManagedObjectSourceTest("OFFICE.MOS", (compile, mos) -> {
			compile.office((context) -> {
				context.getOfficeArchitect().addOfficeManagedObjectSource("MOS", mos);
			});
		});
	}

	/**
	 * Ensure can augment the {@link SectionManagedObjectSource}.
	 */
	public void testAugmentSectionManagedObjectSourceExecutionStrategy() throws Exception {
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
		AugmentExecutiveSource executiveSource = new AugmentExecutiveSource();

		// Compile with the augmented managed object source
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Add the executive
			OfficeFloorExecutive executive = deployer.setExecutive(executiveSource);
			OfficeFloorExecutionStrategy executionStrategy = executive.getOfficeFloorExecutionStrategy("TEST");

			// Augment the managed object source
			deployer.addManagedObjectSourceAugmentor((augment) -> {

				// Ensure correct name
				assertEquals("Incorrect managed object source name", managedObjectSourceName,
						augment.getManagedObjectSourceName());

				// Obtain the execution strategy
				AugmentedManagedObjectExecutionStrategy required = augment
						.getManagedObjectExecutionStrategy("EXECUTION_STRATEGY");
				assertEquals("Incorrect execution strategy name", "EXECUTION_STRATEGY",
						required.getManagedObjectExecutionStrategyName());

				// Link the execution strategy
				augment.link(required, executionStrategy);
			});
		});
		loader.accept(compile, mos);
		compile.compileAndOpenOfficeFloor();

		// Ensure configured execution strategy
		assertEquals("Should have execution strategy", 1, mos.threadFactories.length);
		assertSame("Incorrect execution strategy", executiveSource, mos.threadFactories[0]);
	}

	@TestSource
	public static class AugmentExecutiveSource extends AbstractExecutiveSource
			implements Executive, ExecutionStrategy, ThreadFactory {

		/*
		 * ===================== ExecutiveSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ======================= Executive ===========================
		 */

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return new ExecutionStrategy[] { this };
		}

		/*
		 * ==================== ExecutionStrategy =======================
		 */

		@Override
		public String getExecutionStrategyName() {
			return "TEST";
		}

		@Override
		public ThreadFactory[] getThreadFactories() {
			return new ThreadFactory[] { this };
		}

		/*
		 * ===================== ThreadFactory ===========================
		 */

		@Override
		public Thread newThread(Runnable r) {
			fail("Should not create thread");
			return null;
		}

	}

	@TestSource
	public static class AugmentTeamManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private ThreadFactory[] threadFactories;

		/*
		 * =================== ManagedObjectSource ============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.addExecutionStrategy().setLabel("EXECUTION_STRATEGY");
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {
			threadFactories = context.getExecutionStrategy(0);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not obtain managed object");
			return null;
		}
	}

}