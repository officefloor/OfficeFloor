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

package net.officefloor.compile.integrate.executive;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.integrate.CompileTestSupport;
import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests comping the {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class CompileExecutiveTest {

	private final CompileTestSupport compile = new CompileTestSupport();

	/**
	 * Tests compiling without an {@link Executive}.
	 */
	@Test
	public void noExecutive() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling with an {@link Executive}.
	 */
	@Test
	public void executive() {

		// Record building the OfficeFloor
		this.compile.record_init();
		ExecutiveBuilder<?> executive = this.compile.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		executive.addProperty("ONE", "1");
		executive.addProperty("TWO", "2");
		this.compile.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Ensure can request no {@link TeamOversight}.
	 */
	@Test
	public void requestNoTeamOversight() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		TeamBuilder<?> team = this.compile.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		team.requestNoTeamOversight();

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Ensure link {@link ExecutionStrategy}.
	 */
	@Test
	public void executionStrategy() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_setExecutive(new MockExecutiveSource());
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE",
				ExecutionStrategyManagedObject.class, 10);
		ManagingOfficeBuilder<?> mo = this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		mo.linkExecutionStrategy(0, "MOCK");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Ensure able to use default {@link ExecutionStrategy}.
	 */
	@Test
	public void defaultExecutionStrategy() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE",
				ExecutionStrategyManagedObject.class, 10);
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		// allow default execution strategy (by frame)

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Mock {@link ExecutiveSource} for testing.
	 * 
	 * @author Daniel Sagenschneider
	 */
	@TestSource
	public static class MockExecutiveSource extends DefaultExecutive {

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
			return fail("Should not require obtaining managed object in compiling");
		}
	}

}
