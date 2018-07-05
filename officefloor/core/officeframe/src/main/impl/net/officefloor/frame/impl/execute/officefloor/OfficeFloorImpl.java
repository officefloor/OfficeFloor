/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.officefloor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.NameAwareManagedFunctionFactory;
import net.officefloor.frame.api.function.OfficeAwareManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.execute.office.OfficeImpl;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
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
	private static final Logger logger = Logger.getLogger(OfficeFloor.class.getName());

	/**
	 * {@link OfficeFloorMetaData} for this {@link OfficeFloor}.
	 */
	private final OfficeFloorMetaData officeFloorMetaData;

	/**
	 * {@link OfficeFloorListener} instances.
	 */
	private final OfficeFloorListener[] listeners;

	/**
	 * {@link Office} instances by their name.
	 */
	private Map<String, Office> offices = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorMetaData
	 *            {@link OfficeFloorMetaData}.
	 * @param listeners
	 *            {@link OfficeFloorListener} instances.
	 */
	public OfficeFloorImpl(OfficeFloorMetaData officeFloorMetaData, OfficeFloorListener[] listeners) {
		this.officeFloorMetaData = officeFloorMetaData;
		this.listeners = listeners;
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
		for (ManagedObjectSourceInstance<?> mosInstance : this.officeFloorMetaData.getManagedObjectSourceInstances()) {
			this.startManagedObjectSourceInstance(mosInstance);
		}

		// Start the office managers
		for (OfficeMetaData officeMetaData : officeMetaDatas) {
			officeMetaData.getOfficeManager().startManaging();
		}

		// Start the teams working within the offices
		for (TeamManagement teamManagement : this.officeFloorMetaData.getTeams()) {
			teamManagement.getTeam().startWorking();
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
	 * Starts the {@link ManagedObjectSourceInstance}.
	 * 
	 * @param mosInstance
	 *            {@link ManagedObjectSourceInstance}.
	 * @throws Exception
	 *             If fails to start the {@link ManagedObjectSourceInstance}.
	 */
	private <F extends Enum<F>> void startManagedObjectSourceInstance(ManagedObjectSourceInstance<F> mosInstance)
			throws Exception {

		// Obtain the managed object source
		ManagedObjectSource<?, F> mos = mosInstance.getManagedObjectSource();

		// Start the managed object source
		ManagedObjectExecuteContext<F> executeContext = mosInstance.getManagedObjectExecuteContextFactory()
				.createManagedObjectExecuteContext();
		mos.start(executeContext);
	}

	@Override
	public void closeOfficeFloor() throws Exception {

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
	 * @throws IllegalStateException
	 *             If not open.
	 */
	private void ensureOfficeFloorOpen() throws IllegalStateException {
		if (this.offices == null) {
			throw new IllegalStateException("Must open the OfficeFloor before obtaining Offices");
		}
	}

}