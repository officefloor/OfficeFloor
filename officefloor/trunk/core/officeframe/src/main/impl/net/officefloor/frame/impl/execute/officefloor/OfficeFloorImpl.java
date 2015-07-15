/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.officefloor;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.NameAwareWorkFactory;
import net.officefloor.frame.api.build.OfficeAwareWorkFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.impl.execute.office.OfficeImpl;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupTask;
import net.officefloor.frame.internal.structure.ProcessTicker;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Implementation of {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorImpl implements OfficeFloor {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloorImpl.class
			.getName());

	/**
	 * {@link OfficeFloorMetaData} for this {@link OfficeFloor}.
	 */
	private final OfficeFloorMetaData officeFloorMetaData;

	/**
	 * {@link Office} instances by their name.
	 */
	private Map<String, Office> offices = null;

	/**
	 * {@link ProcessTicker}.
	 */
	private ProcessTicker processTicker = null;

	/**
	 * {@link Timer}.
	 */
	private Timer timer = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorMetaData
	 *            {@link OfficeFloorMetaData}.
	 */
	public OfficeFloorImpl(OfficeFloorMetaData officeFloorMetaData) {
		this.officeFloorMetaData = officeFloorMetaData;
	}

	/*
	 * ====================== OfficeFloor ================================
	 */

	/**
	 * {@link TeamIdentifier} for the startup {@link Task} instances.
	 */
	public static final TeamIdentifier STARTUP_TEAM = new TeamIdentifier() {
	};

	@Override
	public synchronized void openOfficeFloor() throws Exception {

		// Ensure not already open
		if (this.offices != null) {
			throw new IllegalStateException("OfficeFloor is already open");
		}

		// Create the process ticker
		final AtomicInteger ticker = new AtomicInteger(0);
		this.processTicker = new ProcessTicker() {

			@Override
			public int activeProcessCount() {
				return ticker.get();
			}

			@Override
			public void processStarted() {
				// Increment the number of active processes
				ticker.incrementAndGet();
			}

			@Override
			public void processComplete(TeamIdentifier currentTeam) {
				// Decrement the number of active processes
				ticker.decrementAndGet();
			}
		};

		// Create the timer
		this.timer = new Timer(true);

		// Create the offices to open floor for work
		OfficeMetaData[] officeMetaDatas = this.officeFloorMetaData
				.getOfficeMetaData();
		this.offices = new HashMap<String, Office>(officeMetaDatas.length);
		for (OfficeMetaData officeMetaData : officeMetaDatas) {

			// Create the office
			String officeName = officeMetaData.getOfficeName();
			Office office = new OfficeImpl(officeMetaData, this.processTicker);

			// Iterate over Office meta-data providing additional functionality
			for (WorkMetaData<?> workMetaData : officeMetaData
					.getWorkMetaData()) {
				WorkFactory<?> workFactory = workMetaData.getWorkFactory();

				// Handle if name aware
				if (workFactory instanceof NameAwareWorkFactory<?>) {
					NameAwareWorkFactory<?> nameAwareWorkFactory = (NameAwareWorkFactory<?>) workFactory;
					nameAwareWorkFactory.setBoundWorkName(workMetaData
							.getWorkName());
				}

				// Handle if Office aware
				if (workFactory instanceof OfficeAwareWorkFactory<?>) {
					OfficeAwareWorkFactory<?> officeAwareWorkFactory = (OfficeAwareWorkFactory<?>) workFactory;
					officeAwareWorkFactory.setOffice(office);
				}
			}

			// Maintain reference to office for returning
			this.offices.put(officeName, office);
		}

		// Start the managed object source instances
		for (ManagedObjectSourceInstance<?> mosInstance : this.officeFloorMetaData
				.getManagedObjectSourceInstances()) {
			this.startManagedObjectSourceInstance(mosInstance);
		}

		// Start the office managers
		for (OfficeMetaData officeMetaData : officeMetaDatas) {
			officeMetaData.getOfficeManager().startManaging();
		}

		// Start the teams working within the offices
		for (TeamManagement teamManagement : this.officeFloorMetaData
				.getTeams()) {
			teamManagement.getTeam().startWorking();
		}

		// Invoke the startup tasks for each office
		for (OfficeMetaData officeMetaData : officeMetaDatas) {
			for (OfficeStartupTask officeStartupTask : officeMetaData
					.getStartupTasks()) {

				// Ensure have startup task
				if (officeStartupTask == null) {
					continue; // failure in configuring startup task
				}

				// Create and activate the startup task
				JobNode startupTask = officeMetaData.createProcess(
						officeStartupTask.getFlowMetaData(),
						officeStartupTask.getParameter(), null, null, null);
				startupTask.activateJob(STARTUP_TEAM);
			}
		}
	}

	/**
	 * Starts the {@link ManagedObjectSourceInstance}.
	 * 
	 * @param mosInstance
	 *            {@link ManagedObjectSourceInstance}.
	 * @throws Exception
	 *             If fails to start the {@link ManagedObjectSourceInstance}.
	 */
	private <F extends Enum<F>> void startManagedObjectSourceInstance(
			ManagedObjectSourceInstance<F> mosInstance) throws Exception {

		// Obtain the managed object source
		ManagedObjectSource<?, F> mos = mosInstance.getManagedObjectSource();

		// Start the managed object source
		ManagedObjectExecuteContext<F> executeContext = mosInstance
				.getManagedObjectExecuteContextFactory()
				.createManagedObjectExecuteContext(this.processTicker,
						this.timer);
		mos.start(executeContext);

		// Determine if pooled
		ManagedObjectPool pool = mosInstance.getManagedObjectPool();
		if (pool != null) {
			// Have pool, so start the pool
			pool.init(new ManagedObjectPoolContextImpl(mos));
		}
	}

	@Override
	public synchronized void closeOfficeFloor() {

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

			// Wait until processes complete (or reasonable amount of time)
			long startTime = System.currentTimeMillis();
			try {
				while ((this.processTicker.activeProcessCount() > 0)
						&& ((System.currentTimeMillis() - startTime) < 10000)) {
					Thread.sleep(100);
				}
			} catch (InterruptedException ex) {
				// Carry on to close the OfficeFloor
			}

			// Provide warning that timed out waiting for completion
			if (this.processTicker.activeProcessCount() > 0) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.warning("Timed out waiting for processes to complete ("
							+ this.processTicker.activeProcessCount()
							+ " remaining).  Forcing close of OfficeFloor.");
				}
			}

			// Stop the timer
			this.timer.cancel();

			// Stop the teams working as closing
			for (TeamManagement teamManagement : this.officeFloorMetaData
					.getTeams()) {
				teamManagement.getTeam().stopWorking();
			}

			// Stop the office managers
			for (OfficeMetaData officeMetaData : this.officeFloorMetaData
					.getOfficeMetaData()) {
				officeMetaData.getOfficeManager().stopManaging();
			}

		} finally {
			// Flag that no longer open
			this.offices = null;
			this.processTicker = null;
			this.timer = null;
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
	public synchronized Office getOffice(String officeName)
			throws UnknownOfficeException {

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
	 * @throws IllegalStateException
	 *             If not open.
	 */
	private void ensureOfficeFloorOpen() throws IllegalStateException {
		if (this.offices == null) {
			throw new IllegalStateException(
					"Must open the Office Floor before obtaining Offices");
		}
	}

}