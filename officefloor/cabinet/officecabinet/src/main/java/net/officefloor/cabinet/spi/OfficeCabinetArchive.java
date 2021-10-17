package net.officefloor.cabinet.spi;

/**
 * Factory to create the {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeCabinetArchive<D> extends AutoCloseable {

	/**
	 * Creates the {@link OfficeCabinet}.
	 * 
	 * @return {@link OfficeCabinet}.
	 */
	OfficeCabinet<D> createOfficeCabinet();

}