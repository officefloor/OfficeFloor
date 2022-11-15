package net.officefloor.cabinet.spi;

import net.officefloor.cabinet.Document;

/**
 * Office Store.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeStore {

	/**
	 * <p>
	 * Sets up the {@link OfficeCabinet} for the {@link Document} type.
	 * <p>
	 * This must be called for all {@link Document} types before creating the first
	 * {@link CabinetManager}.
	 * 
	 * @param <D>          {@link Document} type.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document} type.
	 * @throws Exception If fails to create the {@link OfficeCabinet}.
	 */
	<D> void setupOfficeCabinet(Class<D> documentType, Index... indexes) throws Exception;

	/**
	 * Creates a {@link CabinetManager}.
	 * 
	 * @return New {@link CabinetManager}.
	 */
	CabinetManager createCabinetManager();

}