package net.officefloor.frame.impl.execute.managedobject.flow;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests registering a startup {@link ProcessState} from
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceStartupProcessTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure startup {@link ProcessState} instances are invoked after all
	 * {@link ManagedObjectSource} instances and {@link Team} instances are started.
	 */
	public void testStartupProcess() throws Exception {

		// Obtain Office name
		String officeName = this.getOfficeName();

		// Load the managed object sources
		MockStartupManagedObjectSource[] sources = new MockStartupManagedObjectSource[] {
				new MockStartupManagedObjectSource("One"), new MockStartupManagedObjectSource("Two") };
		for (MockStartupManagedObjectSource source : sources) {

			// Construct the managed object
			ManagedObjectBuilder<Flows> moBuilder = this.constructManagedObject("Startup" + source.name, source, null);

			// Provide flow
			ManagingOfficeBuilder<Flows> managingOfficeBuilder = moBuilder.setManagingOffice(officeName);
			managingOfficeBuilder.setInputManagedObjectName("Input" + source.name);
			managingOfficeBuilder.linkFlow(Flows.FLOW, "handle");
		}

		// Provide team (to ensure processing after teams started)
		MockStartupTeamSource team = new MockStartupTeamSource();
		this.constructTeam("TEAM", team);

		// Provide function for managed object source input process
		MockWork handler = new MockWork(sources);
		ReflectiveFunctionBuilder function = this.constructFunction(handler, "handle");
		function.buildParameter();
		function.getBuilder().setResponsibleTeam("TEAM");

		// Build the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Ensure sources not yet started
		for (MockStartupManagedObjectSource source : sources) {
			assertFalse("Source " + source.name + " should not yet be started", source.isStarted);
		}

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Ensure the sources are started
		for (MockStartupManagedObjectSource source : sources) {
			assertTrue("Source " + source.name + " not started", source.isStarted);
		}

		// Ensure invoked start up process
		for (MockStartupManagedObjectSource source : sources) {
			assertTrue("Source " + source.name + " startup handler not invoked", handler.startedSources.remove(source));
		}
		assertEquals("Additional startup invocations", 0, handler.startedSources.size());

		// Ensure invoked via team (ensures team started before processing)
		assertTrue("Team should execute function", team.isAssignedJob);

		// Ensure start up process also complete
		// (should complete, as no multi-threaded teams)
		for (MockStartupManagedObjectSource source : sources) {
			assertTrue("Start up process for source " + source.name + " is not complete",
					source.isStartupProcessComplete);
		}

		// Ensure no longer able to invoke start up process
		MockStartupManagedObjectSource source = sources[0];
		try {
			source.context.registerStartupProcess(Flows.FLOW, null, source, null);
			fail("Should not be able to start up process now running");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", "May only register start up processes during start(...) method",
					ex.getMessage());
		}

		// Ensure, however, now able to invoke processes
		source.context.invokeProcess(Flows.FLOW, source, source, 0, null);
		assertEquals("Should invoke process", 1, handler.startedSources.size());
		assertSame("Incorrect source for invoked process", source, handler.startedSources.get(0));
	}

	public static class MockWork {

		private final MockStartupManagedObjectSource[] sources;

		private final List<MockStartupManagedObjectSource> startedSources = new ArrayList<>(2);

		private MockWork(MockStartupManagedObjectSource[] sources) {
			this.sources = sources;
		}

		public void handle(MockStartupManagedObjectSource startedSource) {

			// Ensure all sources are started
			for (MockStartupManagedObjectSource source : this.sources) {
				assertTrue("Managed Object Source " + source.name + " should be started", source.isStarted);
			}

			// Add the source as started
			this.startedSources.add(startedSource);
		}
	}

	private static enum Flows {
		FLOW
	}

	@TestSource
	private static class MockStartupManagedObjectSource extends AbstractManagedObjectSource<None, Flows>
			implements ManagedObject {

		private final String name;

		private boolean isStarted = false;

		private boolean isStartupProcessComplete = false;

		private ManagedObjectExecuteContext<Flows> context;

		private MockStartupManagedObjectSource(String name) {
			this.name = name;
		}

		/*
		 * ================= ManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
			context.setObjectClass(MockStartupManagedObjectSource.class);
			context.addFlow(Flows.FLOW, MockStartupManagedObjectSource.class);
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.context = context;

			// Ensure can register startup processes
			context.registerStartupProcess(Flows.FLOW, this, this, (error) -> {
				this.isStartupProcessComplete = true;
			});

			// Ensure not able to invoke process immediately
			try {
				context.invokeProcess(0, null, this, 0, null);
				fail("Should not successfully invoke process");
			} catch (IllegalStateException ex) {
				assertEquals("Incorrect cause", "During start(...) method, may only register start up processes",
						ex.getMessage());
			}

			// Indicate started
			this.isStarted = true;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObject =========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	@TestSource
	private static class MockStartupTeamSource extends AbstractTeamSource implements Team {

		private boolean isStarted = false;

		private boolean isAssignedJob = false;

		/*
		 * =================== TeamSource =========================
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
		 * ======================= Team ============================
		 */

		@Override
		public void startWorking() {
			this.isStarted = true;
		}

		@Override
		public void assignJob(Job job) throws TeamOverloadException, Exception {
			assertTrue("Team must be started before assigning job", this.isStarted);
			this.isAssignedJob = true;
			job.run();
		}

		@Override
		public void stopWorking() {
			// Do nothing
		}
	}

}