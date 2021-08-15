package net.officefloor.cabinet.spi;

import net.officefloor.cabinet.OfficeCabinet;

/**
 * Factory to create the {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeCabinetArchive<D> {

	/**
	 * Creates the {@link OfficeCabinet}.
	 * 
	 * @return {@link OfficeCabinet}.
	 */
	OfficeCabinet<D> createOfficeCabinet();

}