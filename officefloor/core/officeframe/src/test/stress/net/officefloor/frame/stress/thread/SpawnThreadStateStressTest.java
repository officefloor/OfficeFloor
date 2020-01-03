package net.officefloor.frame.stress.thread;

import java.util.Map;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests running the same {@link ManagedFunction} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class SpawnThreadStateStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(SpawnThreadStateStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 100000;
	}

	@Override
	protected void overrideIterationCount(Map<Class<? extends TeamSource>, Integer> overrides) {
		overrides.put(ThreadLocalAwareTeamSource.class, 100);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {
		
		// Break chain (to avoid stack overflow)
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(1000);

		// Construct functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildFlow("spawn", null, true);
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		spawn.buildFlow("trigger", null, false);
		context.loadResponsibleTeam(spawn.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", null);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void trigger(ReflectiveFlow spawn) {

			// Spawn thread state
			spawn.doFlow(null, (escalation) -> {
			});
		}

		public void spawn(ReflectiveFlow flow) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Spawn again
			flow.doFlow(null, null);
		}
	}

}