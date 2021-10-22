package net.officefloor.cabinet.spi;

import java.util.function.Consumer;

/**
 * Factory to create the {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeCabinetArchive<D, C> extends AutoCloseable {

	/**
	 * Creates the {@link OfficeCabinet}.
	 * 
	 * @return {@link OfficeCabinet}.
	 */
	OfficeCabinet<D> createOfficeCabinet();

	/**
	 * Creates the domain specific {@link OfficeCabinet}.
	 * 
	 * @param officeCabinetListener {@link Consumer} to obtain the underlying
	 *                              {@link OfficeCabinet}.
	 * @return Domain specific {@link OfficeCabinet}.
	 */
	C createDomainSpecificOfficeCabinet(Consumer<OfficeCabinet<D>> officeCabinetListener);

}