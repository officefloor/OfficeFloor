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

package net.officefloor.compile.run.managedobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.run.OpenOfficeFloorTestSupport;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensures able to run {@link ManagedFunction} instances on open.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class RunManagedObjectStartFunctionTest {

	/**
	 * {@link OpenOfficeFloorTestSupport}.
	 */
	private final OpenOfficeFloorTestSupport open = new OpenOfficeFloorTestSupport();

	@BeforeEach
	public void reset() {
		StartupManagedObjectSource.isFlow = null;
		StartupManagedObjectSource.responsibleTeam = null;
		StartupManagedObjectSource.dependency = null;
		StartupTeamSource.jobs.clear();
	}

	/**
	 * Ensure can run start up {@link ManagedFunction} without {@link Team}.
	 */
	@Test
	public void startupFunction() {
		StartupManagedObjectSource.isFlow = false;
		this.doDefaultTeamTest();
	}

	/**
	 * Ensure can run start up {@link ManagedFunction} with configured {@link Team}.
	 */
	@Test
	public void startupFunctionByConfiguredTeam() {
		StartupManagedObjectSource.isFlow = false;
		StartupManagedObjectSource.responsibleTeam = "MO_TEAM";
		this.doAssignedTeamTest();
	}

	/**
	 * Ensure can run start up {@link ManagedFunction} with configured {@link Team}
	 * that is auto-wired.
	 */
	@Test
	public void startupFunctionByConfiguredTeamAutoWired() {
		StartupManagedObjectSource.isFlow = false;
		StartupManagedObjectSource.responsibleTeam = "MO_TEAM";
		this.doAssignedTeamTest();
	}

	/**
	 * Ensure can run start up {@link ManagedFunction} auto wired to a {@link Team}.
	 */
	@Test
	public void startupFunctionByAutoWiredTeam() {
		StartupManagedObjectSource.isFlow = false;
		this.doAssignedTeamTest();
	}

	/**
	 * Ensure can run start up {@link Flow} without {@link Team}.
	 */
	@Test
	public void startupFlow() {
		StartupManagedObjectSource.isFlow = true;
		this.doDefaultTeamTest();
	}

	/**
	 * Ensure can run start up {@link Flow} with configured {@link Team}.
	 */
	@Test
	public void startupFlowByConfiguredTeam() {
		StartupManagedObjectSource.isFlow = true;
		StartupManagedObjectSource.responsibleTeam = "MO_TEAM";
		this.doAssignedTeamTest();
	}

	/**
	 * Ensure can run start up {@link Flow} with configured {@link Team} that is
	 * auto-wired.
	 */
	@Test
	public void startupFlowByConfiguredTeamAutoWired() {
		StartupManagedObjectSource.isFlow = true;
		StartupManagedObjectSource.responsibleTeam = "MO_TEAM";
		this.doAssignedTeamTest();
	}

	/**
	 * Ensure can run start up {@link Flow} auto wired to a {@link Team}.
	 */
	@Test
	public void startupFlowByAutoWiredTeam() {
		StartupManagedObjectSource.isFlow = true;
		this.doAssignedTeamTest();
	}

	/**
	 * Undertakes the start up with the default {@link Team}.
	 */
	private void doDefaultTeamTest() {

		// Open
		this.open.open();

		// Start up function should be executed by default team
		assertEquals(0, StartupTeamSource.jobs.size(), "Should start up by default team");
		assertNotNull(StartupManagedObjectSource.dependency, "Should be started");
	}

	/**
	 * Undertakes the start up with an assigned {@link Team}.
	 */
	private void doAssignedTeamTest() {

		// Open
		this.open.open();

		// Start up function should be executed by auto-wire team
		assertEquals(1, StartupTeamSource.jobs.size(), "Should be assigned to auto-wire team");
		assertNull(StartupManagedObjectSource.dependency, "Should not yet be started");

		// Execute the start up function
		StartupTeamSource.jobs.get(0).run();
		assertNotNull(StartupManagedObjectSource.dependency, "Should be started");
	}

	/**
	 * Mock dependency.
	 */
	public static class MockDependency {
	}

	public static enum DependencyKeys {
		MOCK_DEPENDENCY
	}

	@TestSource
	public static class StartupManagedObjectSource extends AbstractManagedObjectSource<None, Indexed> implements
			ManagedObject, ManagedFunctionFactory<DependencyKeys, None>, ManagedFunction<DependencyKeys, None> {

		private static Boolean isFlow;

		private static String responsibleTeam = null;

		private static MockDependency dependency;

		/*
		 * ================== ManagedObjectSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

			// Load meta-data
			context.setObjectClass(this.getClass());

			// Add start up function
			final String FUNCTION_NAME = "function";
			ManagedObjectFunctionBuilder<DependencyKeys, None> function = mosContext.addManagedFunction(FUNCTION_NAME,
					this);
			function.linkObject(DependencyKeys.MOCK_DEPENDENCY,
					mosContext.addFunctionDependency("DEPENDENCY", MockDependency.class));
			if (responsibleTeam != null) {
				function.setResponsibleTeam(responsibleTeam);
			}

			// Invoke function appropriately on start up
			if (isFlow) {
				context.addFlow(null);
				mosContext.getFlow(0).linkFunction(FUNCTION_NAME);
			} else {
				mosContext.addStartupFunction(FUNCTION_NAME, null);
			}
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {

			if (isFlow) {
				context.invokeStartupProcess(0, null, this, null);
			}
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObject ===========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== ManagedFunction ==========================
		 */

		@Override
		public ManagedFunction<DependencyKeys, None> createManagedFunction() throws Throwable {
			return this;
		}

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, None> context) throws Throwable {
			dependency = (MockDependency) context.getObject(DependencyKeys.MOCK_DEPENDENCY);
		}
	}

	@TestSource
	public static class StartupTeamSource extends AbstractTeamSource implements Team {

		private static final List<Job> jobs = new LinkedList<>();

		/*
		 * ======================= TeamSource ============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ========================= Team =================================
		 */

		@Override
		public void startWorking() {
			// Nothing to start
		}

		@Override
		public void assignJob(Job job) throws TeamOverloadException, Exception {
			jobs.add(job);
		}

		@Override
		public void stopWorking() {
			// Nothing to stop
		}
	}

}
