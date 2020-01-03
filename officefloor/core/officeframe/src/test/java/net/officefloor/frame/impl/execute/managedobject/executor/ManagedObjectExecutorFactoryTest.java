package net.officefloor.frame.impl.execute.managedobject.executor;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.executor.ManagedObjectExecutorFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;

/**
 * Tests the {@link ManagedObjectExecutorFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecutorFactoryTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to run {@link Runnable} via {@link Flow} key.
	 */
	public void testExecuteRunnableViaFlowKey() throws Exception {
		this.doExecuteRunnableTest(new FlowExecutorManagedObjectSource());
	}

	/**
	 * Ensure able to run {@link Runnable} via {@link Flow} index.
	 */
	public void testExecuteRunnableViaIndex() throws Exception {
		this.doExecuteRunnableTest(new IndexedExecutorManagedObjectSource());
	}

	/**
	 * Undertakes test.
	 * 
	 * @param mos {@link AbstractExecutorManagedObjectSource}.
	 */
	private <F extends Enum<F>> void doExecuteRunnableTest(AbstractExecutorManagedObjectSource<F> mos)
			throws Exception {

		// Construct
		String officeName = this.getOfficeName();
		ManagedObjectBuilder<F> moBuilder = this.constructManagedObject("MOS", mos, null);
		moBuilder.setManagingOffice(officeName).setInputManagedObjectName("MOS");
		this.constructTeam("of-MOS.TEAM", ExecutorCachedTeamSource.class);
		OfficeFloor officeFloor = this.constructOfficeFloor();
		try {
			officeFloor.openOfficeFloor();

			// Ensure able to run the runnable
			String result = "RESULT FLOW";
			ThreadSafeClosure<String> isExecuted = new ThreadSafeClosure<>();
			mos.executor.execute(() -> isExecuted.set(result));
			String value = isExecuted.waitAndGet();
			assertSame("Should have executed to set value", result, value);

		} finally {
			officeFloor.closeOfficeFloor();
		}
	}

	public static enum Flows {
		FLOW
	}

	@TestSource
	public static class FlowExecutorManagedObjectSource extends AbstractExecutorManagedObjectSource<Flows> {

		@Override
		protected ManagedObjectExecutorFactory<Flows> createManagedObjectExecutorFactory(
				MetaDataContext<None, Flows> context) {
			return new ManagedObjectExecutorFactory<>(context, Flows.FLOW, "TEAM");
		}
	}

	@TestSource
	public static class IndexedExecutorManagedObjectSource extends AbstractExecutorManagedObjectSource<Indexed> {

		@Override
		protected ManagedObjectExecutorFactory<Indexed> createManagedObjectExecutorFactory(
				MetaDataContext<None, Indexed> context) {
			return new ManagedObjectExecutorFactory<>(context, "TEAM");
		}
	}

	@TestSource
	private static abstract class AbstractExecutorManagedObjectSource<F extends Enum<F>>
			extends AbstractManagedObjectSource<None, F> implements ManagedObject {

		private ManagedObjectExecutorFactory<F> executorFactory;

		protected Executor executor;

		protected abstract ManagedObjectExecutorFactory<F> createManagedObjectExecutorFactory(
				MetaDataContext<None, F> context);

		/*
		 * =================== ManagedObjectSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, F> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.setManagedObjectClass(this.getClass());

			// Configure the executor
			this.executorFactory = this.createManagedObjectExecutorFactory(context);
		}

		@Override
		public void start(ManagedObjectExecuteContext<F> context) throws Exception {

			// Create the executor
			this.executor = this.executorFactory.createExecutor(context, this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not instantiate managed object");
			return null;
		}

		/*
		 * ======================= ManagedObject ============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}