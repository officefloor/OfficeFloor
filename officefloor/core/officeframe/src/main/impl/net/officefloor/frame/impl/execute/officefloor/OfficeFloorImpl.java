/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.officefloor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveOfficeContext;
import net.officefloor.frame.api.executive.ExecutiveStartContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.NameAwareManagedFunctionFactory;
import net.officefloor.frame.api.function.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.office.OfficeImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManager;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteStart;
import net.officefloor.frame.internal.structure.ManagedObjectServiceReady;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.ManagedObjectStartupRunnable;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeManager;
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
	 * Object to be notified about start up completions.
	 */
	private final Object startupNotify;

	/**
	 * {@link Office} instances by their name.
	 */
	private Map<String, Office> offices = null;

	/**
	 * {@link ManagedObjectExecuteStart} instances.
	 */
	private ManagedObjectExecuteStart<?>[][] executeStartups = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorMetaData {@link OfficeFloorMetaData}.
	 * @param listeners           {@link OfficeFloorListener} instances.
	 * @param executive           {@link Executive}.
	 * @param startupNotify       Object to be notified about start up completions.
	 */
	public OfficeFloorImpl(OfficeFloorMetaData officeFloorMetaData, OfficeFloorListener[] listeners,
			Executive executive, Object startupNotify) {
		this.officeFloorMetaData = officeFloorMetaData;
		this.listeners = listeners;
		this.executive = executive;
		this.startupNotify = startupNotify;
	}

	/*
	 * ====================== OfficeFloor ================================
	 */

	@Override
	public synchronized void openOfficeFloor() throws Exception {

		// Ensure not already open
		if (this.offices != null) {
			throw new IllegalStateException("OfficeFloor is already open");
		}

		// Ensure clean up if not opened successfully
		boolean isNotifyClose = false;
		boolean isOpened = false;
		try {

			// Capture open start time
			long openStartTime = System.currentTimeMillis();
			Function<Long, String> timeoutMessage = (maxWaitTime) -> OfficeFloor.class.getSimpleName()
					+ " took longer than " + maxWaitTime + " milliseconds to start";

			// Start managing the OfficeFloor
			OfficeMetaData[] officeMetaDatas = this.officeFloorMetaData.getOfficeMetaData();
			OfficeManager[] defaultOfficeManagers = new OfficeManager[officeMetaDatas.length];
			ExecutiveStartContext startContext = new ExecutiveStartContext() {

				@Override
				public OfficeManager[] getDefaultOfficeManagers() {

					// Determine if already setup default office managers
					if ((defaultOfficeManagers.length == 0) || (defaultOfficeManagers[0] != null)) {
						return defaultOfficeManagers;
					}

					// Obtain the list of default office managers
					for (int i = 0; i < officeMetaDatas.length; i++) {
						OfficeMetaData officeMetaData = officeMetaDatas[i];

						// Setup the default office manager
						defaultOfficeManagers[i] = officeMetaData.setupDefaultOfficeManager();
					}

					// Return the default office managers
					return defaultOfficeManagers;
				}
			};
			this.executive.startManaging(startContext);

			// Ensure default office managers are setup
			startContext.getDefaultOfficeManagers();

			// Create the offices to open floor for work
			Map<String, Office> offices = new HashMap<String, Office>(officeMetaDatas.length);
			for (int i = 0; i < officeMetaDatas.length; i++) {
				OfficeMetaData officeMetaData = officeMetaDatas[i];

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
				offices.put(officeName, office);
			}

			// Initiate opening tracking state
			this.offices = offices;

			// Ensure executor is always another thread for office
			OfficeMetaData finalOfficeMetaData = this.officeFloorMetaData.getOfficeMetaData()[0];
			ProcessIdentifier initiateProcessIdentifier = this.executive
					.createProcessIdentifier(new ExecutiveOfficeContext() {

						@Override
						public String getOfficeName() {
							return finalOfficeMetaData.getOfficeName();
						}

						@Override
						public OfficeManager hireOfficeManager() {
							return defaultOfficeManagers[0];
						}
					});
			Thread[] isComplete = new Thread[] { null };
			this.executive.createExecutor(initiateProcessIdentifier).execute(() -> {
				// Capture thread
				synchronized (isComplete) {
					isComplete[0] = Thread.currentThread();
					isComplete.notifyAll();
				}
			});
			this.waitOrTimeout(openStartTime, isComplete, () -> isComplete[0] == null, timeoutMessage);
			synchronized (isComplete) {
				if (Thread.currentThread().equals(isComplete[0])) {
					// Executor is re-using thread (therefore unsafe)
					throw new IllegalStateException(
							"Break thread stack is not safe.  The configured " + Executive.class.getSimpleName()
									+ " must not re-use the current Thread to break the thread stack.");
				}
			}

			// Create listing of execute start ups (respecting ordered groups)
			ManagedObjectSourceInstance<?>[][] mosInstances = this.officeFloorMetaData
					.getManagedObjectSourceInstances();
			this.executeStartups = new ManagedObjectExecuteStart[mosInstances.length][];

			// Start all the managed object source instances
			for (int groupIndex = 0; groupIndex < mosInstances.length; groupIndex++) {
				ManagedObjectSourceInstance<?>[] groupedInstances = mosInstances[groupIndex];

				// Start the group
				this.executeStartups[groupIndex] = new ManagedObjectExecuteStart[groupedInstances.length];
				for (int itemIndex = 0; itemIndex < groupedInstances.length; itemIndex++) {
					this.executeStartups[groupIndex][itemIndex] = this
							.startManagedObjectSourceInstance(groupedInstances[itemIndex]);
				}
			}

			// Start the teams working within the offices
			for (TeamManagement teamManagement : this.officeFloorMetaData.getTeams()) {
				Team team = teamManagement.getTeam();
				team.startWorking();
			}

			// Start the managed objects respecting the grouping
			for (int groupIndex = 0; groupIndex < this.executeStartups.length; groupIndex++) {
				ManagedObjectExecuteStart<?>[] startGroup = this.executeStartups[groupIndex];

				// Invoke the managed object source startup processes
				for (ManagedObjectExecuteStart<?> executeStartup : startGroup) {
					for (ManagedObjectStartupRunnable startupProcess : executeStartup.getStartups()) {

						// Determine if concurrent
						if (!startupProcess.isConcurrent()) {
							// Run sequentially
							startupProcess.run();

						} else {
							// Start concurrently
							Executor executor = this.executive.createExecutor(initiateProcessIdentifier);
							executor.execute(() -> startupProcess.run());
						}
					}
				}

				// Wait on service readiness of the group (before starting next group)
				final int finalGroupIndex = groupIndex;
				this.waitOrTimeout(openStartTime, this.startupNotify, () -> {
					boolean isContinueWaiting = false;
					for (ManagedObjectSourceInstance<?> mosInstance : mosInstances[finalGroupIndex]) {
						for (ManagedObjectServiceReady serviceReady : mosInstance.getServiceReadiness()) {
							if (!serviceReady.isServiceReady()) {
								isContinueWaiting = true; // continue waiting
							}
						}
					}
					return isContinueWaiting;
				}, timeoutMessage);
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

			// Start the services
			for (ManagedObjectExecuteStart<?>[] startGroup : this.executeStartups) {
				for (ManagedObjectExecuteStart<?> startItem : startGroup) {
					this.startServices(startItem);
				}
			}

			// Flag the initiation complete
			this.executive.processComplete(initiateProcessIdentifier);

			// Need to notify if close now that notifying open
			isNotifyClose = true;

			// Notify the OfficeFloor is open
			for (OfficeFloorListener listener : this.listeners) {
				listener.officeFloorOpened(new OfficeFloorEvent() {
					@Override
					public OfficeFloor getOfficeFloor() {
						return OfficeFloorImpl.this;
					}
				});
			}

			// Flag that opened
			isOpened = true;

		} finally {
			// Clean up if not successfully opened
			try {
				if (!isOpened) {
					this.close(isNotifyClose);
				}
			} catch (Throwable ex) {
				// Allow open failure to propagate
				logger.log(Level.INFO, "Failed to clean up opening " + OfficeFloor.class.getSimpleName(), ex);
			}
		}
	}

	/**
	 * Starts the {@link ManagedObjectService} instances.
	 * 
	 * @param <F>          {@link Flow} keys for {@link ManagedObjectSource}.
	 * @param executeStart {@link ManagedObjectExecuteStart}.
	 * @throws Exception If fails to start services.
	 */
	private <F extends Enum<F>> void startServices(ManagedObjectExecuteStart<F> executeStart) throws Exception {
		for (ManagedObjectService<F> service : executeStart.getServices()) {
			service.startServicing(executeStart.getManagedObjectServiceContext());
		}
	}

	/**
	 * Wait predicate.
	 */
	@FunctionalInterface
	private static interface WaitPredicate {

		/**
		 * Tests if complete.
		 * 
		 * @return <code>true</code> if continue to wait.
		 * @throws Exception If possible failure.
		 */
		boolean isWait() throws Exception;
	}

	/**
	 * Waits for check to pass or times out.
	 * 
	 * @param lock           Lock for synchronising.
	 * @param keepWaiting    Check to determine if keep waiting.
	 * @param timeoutMessage Creates the timeout message.
	 * @throws Exception If fails or times out.
	 */
	private void waitOrTimeout(long startTime, Object lock, WaitPredicate keepWaiting,
			Function<Long, String> timeoutMessage) throws Exception {

		// Calculate time to time out
		long maxWaitTime = this.officeFloorMetaData.getMaxStartupWaitTime();
		long maxTime = startTime + maxWaitTime;

		// Maintain lock as run checks
		synchronized (lock) {
			while (keepWaiting.isWait()) {

				// Determine if timed out
				if (System.currentTimeMillis() > maxTime) {
					throw new TimeoutException(timeoutMessage.apply(maxWaitTime));
				}

				// Sleep some time to check again
				lock.wait(10);
			}
		}
	}

	/**
	 * Starts the {@link ManagedObjectSourceInstance}.
	 * 
	 * @param mosInstance   {@link ManagedObjectSourceInstance}.
	 * @param startupNotify Object to notify on start up completion.
	 * @return {@link ManagedObjectExecuteStart} to invoke once ready to process.
	 * @throws Exception If fails to start the {@link ManagedObjectSourceInstance}.
	 */
	private <F extends Enum<F>> ManagedObjectExecuteStart<?> startManagedObjectSourceInstance(
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
	public synchronized void close() throws Exception {
		this.close(true);
	}

	/**
	 * Closes the {@link OfficeFloor}.
	 * 
	 * @param isNotifyOfficeFloorClosed Indicates if notify to
	 *                                  {@link OfficeFloorListener} instances that
	 *                                  closing.
	 * @throws Exception If fails to close.
	 */
	private void close(boolean isNotifyOfficeFloorClosed) throws Exception {

		// Ensure open to be closed
		if (this.offices == null) {
			// Not open, so do nothing
			return;
		}

		try {
			// Stop the services (in reverse order of start up - with possible not started)
			if (this.executeStartups != null) {
				for (int groupIndex = this.executeStartups.length - 1; groupIndex >= 0; groupIndex--) {
					ManagedObjectExecuteStart<?>[] startGroup = this.executeStartups[groupIndex];
					if (startGroup != null) {
						for (int itemIndex = startGroup.length - 1; itemIndex >= 0; itemIndex--) {
							ManagedObjectExecuteStart<?> startItem = startGroup[itemIndex];
							if (startItem != null) {
								ManagedObjectService<?>[] services = startItem.getServices();
								for (int serviceIndex = services.length - 1; serviceIndex >= 0; serviceIndex--) {
									services[serviceIndex].stopServicing();
								}
							}
						}
					}
				}
			}

			// Stop the managed object sources (in reverse order of start up)
			ManagedObjectSourceInstance<?>[][] mosInstances = this.officeFloorMetaData
					.getManagedObjectSourceInstances();
			for (int groupIndex = mosInstances.length - 1; groupIndex >= 0; groupIndex--) {
				for (int itemIndex = mosInstances[groupIndex].length - 1; itemIndex >= 0; itemIndex--) {
					mosInstances[groupIndex][itemIndex].getManagedObjectSource().stop();
				}
			}

			// Stop the teams working as closing
			for (TeamManagement teamManagement : this.officeFloorMetaData.getTeams()) {
				teamManagement.getTeam().stopWorking();
			}

			// Stop managing the OfficeFloor
			this.executive.stopManaging();

			// Empty the managed object pools (in reverse order of start up)
			for (int groupIndex = mosInstances.length - 1; groupIndex >= 0; groupIndex--) {
				for (int itemIndex = mosInstances[groupIndex].length - 1; itemIndex >= 0; itemIndex--) {
					ManagedObjectPool pool = mosInstances[groupIndex][itemIndex].getManagedObjectPool();
					if (pool != null) {
						pool.empty();
					}
				}
			}

		} finally {
			// Flag that no longer open
			this.offices = null;
			this.executeStartups = null;

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
	public synchronized String[] getOfficeNames() {

		// Ensure open
		this.ensureOfficeFloorOpen();

		// Return the listing of office names
		return this.offices.keySet().toArray(new String[0]);
	}

	@Override
	public synchronized Office getOffice(String officeName) throws UnknownOfficeException {

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
