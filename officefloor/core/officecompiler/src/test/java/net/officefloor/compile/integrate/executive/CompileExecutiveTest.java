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
package net.officefloor.compile.integrate.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.compile.impl.structure.ExecutiveNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;

/**
 * Tests comping the {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileExecutiveTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling without an {@link Executive}.
	 */
	public void testNoExecutive() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling with an {@link Executive}.
	 */
	public void testExecutive() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if unknown {@link TeamOversight}.
	 */
	public void testUnknownTeamOversight() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.issues.recordIssue("Executive", ExecutiveNodeImpl.class, "Unknown TeamOversight 'UNKNOWN'");

		// Compile the OfficeFloor
		this.compile(false);
	}

	/**
	 * Ensure can load {@link TeamOversight}.
	 */
	public void testTeamOversight() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		TeamBuilder<?> team = this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		team.setTeamOversight("MOCK");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if unknown {@link ExecutionStrategy}.
	 */
	public void testUnknownExecutionStrategy() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ExecutionStrategyManagedObject.class,
				10);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.issues.recordIssue("Executive", ExecutiveNodeImpl.class, "Unknown ExecutionStrategy 'UNKNOWN'");

		// Compile the OfficeFloor
		this.compile(false);
	}

	/**
	 * Ensure link {@link ExecutionStrategy}.
	 */
	public void testExecutionStrategy() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ExecutionStrategyManagedObject.class,
				0);
		ManagingOfficeBuilder<?> mo = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		mo.linkExecutionStrategy(0, "MOCK");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Mock {@link ExecutiveSource} for testing.
	 * 
	 * @author Daniel Sagenschneider
	 */
	@TestSource
	public static class MockExecutiveSource extends AbstractExecutiveSource implements Executive {

		/*
		 * ================ ExecutiveSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			return this;
		}

		/*
		 * =================== Executive ===========================
		 */

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return new ExecutionStrategy[] { new ExecutionStrategy() {
				@Override
				public String getExecutionStrategyName() {
					return "MOCK";
				}

				@Override
				public ThreadFactory[] getThreadFactories() {
					return new ThreadFactory[] { (runnable) -> new Thread(runnable) };
				}
			} };
		}

		@Override
		public TeamOversight[] getTeamOversights() {
			return new TeamOversight[] { new TeamOversight() {

				@Override
				public String getTeamOversightName() {
					return "MOCK";
				}
			} };
		}
	}

	/**
	 * {@link ManagedObjectSource} requiring a {@link ExecutionStrategy}.
	 */
	@TestSource
	public static class ExecutionStrategyManagedObject extends AbstractManagedObjectSource<None, None> {

		/*
		 * ================= AbstractManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);

			// Register execution strategy
			context.addExecutionStrategy().setLabel("STRATEGY");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require obtaining managed object in compiling");
			return null;
		}
	}

}