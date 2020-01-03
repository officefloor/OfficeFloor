package net.officefloor.frame.impl.execute.officefloor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.NameAwareManagedFunctionFactory;
import net.officefloor.frame.api.function.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.office.OfficeImpl;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManager;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.ManagedObjectStartupRunnable;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Implementation of {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorImpl implements OfficeFloor {

	/**
	 * Obtains the {@link OfficeFloor} framework {@link Logger}.
	 * 
	 * @return {@link OfficeFloor} framework {@link Logger}.
	 */
	public static Logger getFrameworkLogger() {
		return logger;
	}

	/**
	 * {@link Logger} of the {@link OfficeFloor} framework.
	 */
	private static final Logger logger = OfficeFrame.getLogger(OfficeFloor.class.getName());

	/**
	 * {@link OfficeFloorMetaData} for this {@link OfficeFloor}.
	 */
	private final OfficeFloorMetaData officeFloorMetaData;

	/**
	 * {@link OfficeFloorListener} instances.
	 */
	private final OfficeFloorListener[] listeners;

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * {@link Executor} to break the thread stack execution chain.
	 */
	private final Executor breakChainExecutor;

	/**
	 * {@link Office} instances by their name.
	 */
	private Map<String, Office> offices = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorMetaData {@link OfficeFloorMetaData}.
	 * @param listeners           {@link OfficeFloorListener} instances.
	 * @param executive           {@link Executive}.
	 * @param breakChainExecutor  {@link Executor} to break the thread stack
	 *                            execution chain.
	 */
	public OfficeFloorImpl(OfficeFloorMetaData officeFloorMetaData, OfficeFloorListener[] listeners,
			Executive executive, Executor breakChainExecutor) {
		this.officeFloorMetaData = officeFloorMetaData;
		this.listeners = listeners;
		this.executive = executive;
		this.breakChainExecutor = breakChainExecutor;
	}

	/*
	 * ====================== OfficeFloor ================================
	 */

	@Override
	public void openOfficeFloor() throws Exception {

		// Ensure not already open
		if (this.offices != null) {
			throw new IllegalStateException("OfficeFloor is already open");
		}

		// Start the break chain team
		Team breakChainTeam = this.officeFloorMetaData.getBreakChainTeam().getTeam();
		breakChainTeam.startWorking();

		// Ensure the break chain team is safe
		Object processIdentifier = this.executive.createProcessIdentifier();
		Thread[] isComplete = new Thread[] { null };
		breakChainTeam.assignJob(new Job() {

			@Override
			public Object getProcessIdentifier() {
				return processIdentifier;
			}

			@Override
			public void run() {
				// Capture thread
				synchronized (isComplete) {
					isComplete[0] = Thread.currentThread();
					isComplete.notifyAll();
				}
			}

			@Override
			public void cancel(Throwable cause) {
				this.run(); // capture thread to complete
			}
		});
		this.waitOrTimeout(isComplete, () -> isComplete[0] == null,
				(maxWaitTime) -> "Test of Break Chain team timed out after " + maxWaitTime + " milliseconds");
		synchronized (isComplete) {
			if (Thread.currentThread().equals(isComplete[0])) {
				// Break chain team is re-using thread (therefore unsafe)
				throw new IllegalStateException("Break chain " + Team.class.getSimpleName()
						+ " is not safe.  The configured " + Team.class.getSimpleName()
						+ " must not re-use the current Thread to run " + Job.class.getSimpleName() + "s.");
			}
		}

		// Create the offices to open floor for work
		OfficeMetaData[] officeMetaDatas = this.officeFloorMetaData.getOfficeMetaData();
		this.offices = new HashMap<String, Office>(officeMetaDatas.length);
		for (OfficeMetaData officeMetaData : officeMetaDatas) {

			// Create the office
			String officeName = officeMetaData.getOfficeName();
			Office office = new OfficeImpl(officeMetaData);

			// Iterate over Office meta-data providing additional functionality
			for (ManagedFunctionMetaData<?, ?> functionMetaData : officeMetaData.getManagedFunctionMetaData()) {
				ManagedFunctionFactory<?, ?> functionFactory = functionMetaData.getManagedFunctionFactory();

				// Handle if name aware
				if (functionFactory instanceof NameAwareManagedFunctionFactory) {
					NameAwareManagedFunctionFactory<?, ?> nameAwareFactory = (NameAwareManagedFunctionFactory<?, ?>) functionFactory;
					nameAwareFactory.setBoundFunctionName(functionMetaData.getFunctionName());
				}

				// Handle if Office aware
				if (functionFactory instanceof OfficeAwareManagedFunctionFactory) {
					OfficeAwareManagedFunctionFactory<?, ?> officeAwareFactory = (OfficeAwareManagedFunctionFactory<?, ?>) functionFactory;
					officeAwareFactory.setOffice(office);
				}
			}

			// Maintain reference to office for returning
			this.offices.put(officeName, office);
		}

		// Start the managed object source instances
		List<ManagedObjectStartupRunnable> managedObjectSourceStartupProcesses = new LinkedList<>();
		for (ManagedObjectSourceInstance<?> mosInstance : this.officeFloorMetaData.getManagedObjectSourceInstances()) {
			ManagedObjectStartupRunnable[] startupProcesses = this.startManagedObjectSourceInstance(mosInstance);
			managedObjectSourceStartupProcesses.addAll(Arrays.asList(startupProcesses));
		}

		// Start the office managers
		for (OfficeMetaData officeMetaData : officeMetaDatas) {
			officeMetaData.getOfficeManager().startManaging();
		}

		// Start the teams working within the offices
		for (TeamManagement teamManagement : this.officeFloorMetaData.getTeams()) {
			teamManagement.getTeam().startWorking();
		}

		// Invoke the managed object source startup processes
		int[] concurrentStartups = new int[] { 0 };
		for (ManagedObjectStartupRunnable startupProcess : managedObjectSourceStartupProcesses) {

			// Determine if concurrent
			if (!startupProcess.isConcurrent()) {
				// Run sequentially
				startupProcess.run();

			} else {
				// Run concurrently
				concurrentStartups[0]++;
				this.breakChainExecutor.execute(() -> {

					// Start concurrently
					startupProcess.run();

					// Notify once complete
					synchronized (concurrentStartups) {
						concurrentStartups[0]--;
						concurrentStartups.notify();
					}
				});
			}
		}
		if (concurrentStartups[0] > 0) {
			this.waitOrTimeout(concurrentStartups, () -> concurrentStartups[0] > 0,
					(maxWaitTime) -> OfficeFloor.class.getSimpleName() + " took longer than " + maxWaitTime
							+ " milliseconds to start");
		}

		// Invoke the startup functions for each office
		for (OfficeMetaData officeMetaData : officeMetaDatas) {
			for (OfficeStartupFunction officeStartupTask : officeMetaData.getStartupFunctions()) {

				// Ensure have startup task
				if (officeStartupTask == null) {
					continue; // failure in configuring startup task
				}

				// Create and activate the startup functions
				FunctionState startupFunction = officeMetaData.createProcess(officeStartupTask.getFlowMetaData(),
						officeStartupTask.getParameter(), null, null);
				officeMetaData.getFunctionLoop().delegateFunction(startupFunction);
			}
		}

		// Notify the OfficeFloor is open
		for (OfficeFloorListener listener : this.listeners) {
			listener.officeFloorOpened(new OfficeFloorEvent() {
				@Override
				public OfficeFloor getOfficeFloor() {
					return OfficeFloorImpl.this;
				}
			});
		}
	}

	/**
	 * Waits for check to pass or times out.
	 * 
	 * @param lock           Lock for synchronising.
	 * @param keepWaiting    Check to determine if keep waiting.
	 * @param timeoutMessage Creates the timeout message.
	 * @throws Exception If fails or times out.
	 */
	private void waitOrTimeout(Object lock, Supplier<Boolean> keepWaiting, Function<Long, String> timeoutMessage)
			throws Exception {

		// Calculate time to time out
		long maxWaitTime = this.officeFloorMetaData.getMaxStartupWaitTime();
		long maxTime = System.currentTimeMillis() + maxWaitTime;

		// Maintain lock as run checks
		synchronized (lock) {
			while (keepWaiting.get()) {

				// Determine if timed out
				if (System.currentTimeMillis() > maxTime) {
					throw new TimeoutException(timeoutMessage.apply(maxWaitTime));
				}

				// Sleep some time to check again
				lock.wait(100);
			}
		}
	}

	/**
	 * Starts the {@link ManagedObjectSourceInstance}.
	 * 
	 * @param mosInstance {@link ManagedObjectSourceInstance}.
	 * @return {@link ManagedObjectStartupRunnable} instances to invoke once ready
	 *         to process.
	 * @throws Exception If fails to start the {@link ManagedObjectSourceInstance}.
	 */
	private <F extends Enum<F>> ManagedObjectStartupRunnable[] startManagedObjectSourceInstance(
			ManagedObjectSourceInstance<F> mosInstance) throws Exception {

		// Obtain the managed object source
		ManagedObjectSource<?, F> mos = mosInstance.getManagedObjectSource();

		// Start the managed object source
		ManagedObjectExecuteManager<F> executeManager = mosInstance.getManagedObjectExecuteManagerFactory()
				.createManagedObjectExecuteManager();
		mos.start(executeManager.getManagedObjectExecuteContext());

		// Flag start completed and return further startup executions
		return executeManager.startComplete();
	}

	/*
	 * =================== AutoCloseable ===================
	 */

	@Override
	public void close() throws Exception {

		// Ensure open to be closed
		if (this.offices == null) {
			// Not open, so do nothing
			return;
		}

		try {
			// Stop the managed object sources
			for (ManagedObjectSourceInstance<?> mosInstance : this.officeFloorMetaData
					.getManagedObjectSourceInstances()) {
				mosInstance.getManagedObjectSource().stop();
			}

			// Stop the office managers
			for (OfficeMetaData officeMetaData : this.officeFloorMetaData.getOfficeMetaData()) {
				officeMetaData.getOfficeManager().stopManaging();
			}

			// Stop the teams working as closing
			for (TeamManagement teamManagement : this.officeFloorMetaData.getTeams()) {
				teamManagement.getTeam().stopWorking();
			}

			// Stop the break chain team
			this.officeFloorMetaData.getBreakChainTeam().getTeam().stopWorking();

			// Empty the managed object pools
			for (ManagedObjectSourceInstance<?> mosInstance : this.officeFloorMetaData
					.getManagedObjectSourceInstances()) {
				ManagedObjectPool pool = mosInstance.getManagedObjectPool();
				if (pool != null) {
					pool.empty();
				}
			}

		} finally {
			// Flag that no longer open
			this.offices = null;

			// Notify the OfficeFloor is closed
			for (OfficeFloorListener listener : this.listeners) {
				listener.officeFloorClosed(new OfficeFloorEvent() {
					@Override
					public OfficeFloor getOfficeFloor() {
						return OfficeFloorImpl.this;
					}
				});
			}
		}
	}

	@Override
	public String[] getOfficeNames() {

		// Ensure open
		this.ensureOfficeFloorOpen();

		// Return the listing of office names
		return this.offices.keySet().toArray(new String[0]);
	}

	@Override
	public Office getOffice(String officeName) throws UnknownOfficeException {

		// Ensure open
		this.ensureOfficeFloorOpen();

		// Ensure Office is available
		Office office = this.offices.get(officeName);
		if (office == null) {
			throw new UnknownOfficeException(officeName);
		}

		// Return the Office
		return office;
	}

	/**
	 * Ensures open.
	 * 
	 * @throws IllegalStateException If not open.
	 */
	private void ensureOfficeFloorOpen() throws IllegalStateException {
		if (this.offices == null) {
			throw new IllegalStateException("Must open the OfficeFloor before obtaining Offices");
		}
	}

}