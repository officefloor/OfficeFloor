package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Session for {@link OfficeCabinet} interaction.
 * 
 * @author Daniel Sagenschneider
 */
public interface CabinetSession extends AutoCloseable {

	/**
	 * Obtains the {@link OfficeCabinet} for the {@link Document} type.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @return {@link OfficeCabinet} for the {@link Document} type.
	 */
	<D> OfficeCabinet<D> getOfficeCabinet(Class<D> documentType);

}
