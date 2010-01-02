/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.office.OfficeImpl;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupTask;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorImpl implements OfficeFloor {

	/**
	 * {@link OfficeFloorMetaData} for this {@link OfficeFloor}.
	 */
	private final OfficeFloorMetaData officeFloorMetaData;

	/**
	 * {@link Office} instances by their name.
	 */
	private Map<String, Office> offices = null;

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

	@Override
	public synchronized void openOfficeFloor() throws Exception {

		// Ensure not already open
		if (this.offices != null) {
			throw new IllegalStateException("Office floor is already open");
		}

		// Create the offices to open floor for work
		OfficeMetaData[] officeMetaDatas = this.officeFloorMetaData
				.getOfficeMetaData();
		this.offices = new HashMap<String, Office>(officeMetaDatas.length);
		for (OfficeMetaData officeMetaData : officeMetaDatas) {

			// Create the office
			String officeName = officeMetaData.getOfficeName();
			Office office = new OfficeImpl(officeMetaData);

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
		for (Team team : this.officeFloorMetaData.getTeams()) {
			team.startWorking();
		}

		// Invoke the startup tasks for each office
		for (OfficeMetaData officeMetaData : officeMetaDatas) {
			for (OfficeStartupTask officeStartupTask : officeMetaData
					.getStartupTasks()) {
				JobNode startupTask = officeMetaData.createProcess(
						officeStartupTask.getFlowMetaData(), officeStartupTask
								.getParameter());
				startupTask.activateJob();
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

		// Start the managed object source
		ManagedObjectSource<?, F> mos = mosInstance.getManagedObjectSource();
		mos.start(mosInstance.getManagedObjectExecuteContext());

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

		/*
		 * TODO provide notification to managed objects to stop working.
		 *
		 * DETAILS: Likely candidate for this is to add a stop() method to the
		 * ManagedObjectSource interface. The requirements of this method would
		 * be to stop ManagedObjectSource instances invoking further
		 * ProcessStates. An example being ServerSockets no longer accepting new
		 * connections (but would allow existing connections to be completed).
		 * Once this is added then no further ProcessStates should be created
		 * and when Job counts with Teams reach zero the OfficeFloor is ready to
		 * be closed.
		 */

		// Stop the teams working as closing
		/*
		 * FIXME stopping teams must tie in with stopping ManagedObjectSources.
		 *
		 * DETAILS: Stopping a team when has no further Jobs does not lead to
		 * closing as another Team may require handing it another Job for
		 * completion. As the Team is no longer processing the Job does not get
		 * completed leaving OfficeFloor hanging waiting for the Job to
		 * complete. This is not so much of an issues as OfficeFloor is closing
		 * but would be nice to have graceful close.
		 *
		 * MITIGATION: Will still close OfficeFloor (just some ProcessStates may
		 * end up half processed giving a not so graceful close).
		 */
		for (Team team : this.officeFloorMetaData.getTeams()) {
			team.stopWorking();
		}

		// Stop the office managers
		for (OfficeMetaData officeMetaData : this.officeFloorMetaData
				.getOfficeMetaData()) {
			officeMetaData.getOfficeManager().stopManaging();
		}

		// Flag that no longer open
		this.offices = null;
	}

	@Override
	public synchronized Office getOffice(String officeName) {

		// Ensure is open
		if (this.offices == null) {
			throw new IllegalStateException(
					"Must open the Office Floor before obtaining Offices");
		}

		// Return the office (if available)
		return this.offices.get(officeName);
	}

}