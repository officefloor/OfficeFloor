package net.officefloor.cloud.test;

import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Manages the {@link OfficeCabinet} and underlying test data store.
 * 
 * @author Daniel Sagenschneider
 */
public interface CloudTestCabinet {

	/**
	 * Starts the data store for the {@link OfficeCabinet}.
	 */
	void startDataStore();

	/**
	 * Obtains the {@link OfficeStore} to the test data store.
	 * 
	 * @return {@link OfficeStore} to the test data store.
	 */
	OfficeStore getOfficeStore();

	/**
	 * Stops the data store for the {@link OfficeCabinet}.
	 */
	void stopDataStore();
}