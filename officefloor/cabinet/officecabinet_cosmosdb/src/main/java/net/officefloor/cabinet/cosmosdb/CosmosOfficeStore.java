package net.officefloor.cabinet.cosmosdb;

import java.util.logging.Logger;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Cosmos {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeStore extends AbstractOfficeStore {

	/**
	 * {@link CosmosDocumentAdapter}.
	 */
	private final CosmosDocumentAdapter adapter;

	/**
	 * Instantiate.
	 * 
	 * @param database {@link CosmosDatabase}.
	 * @param logger   {@link Logger}.
	 */
	public CosmosOfficeStore(CosmosDatabase database, Logger logger) {

		// Create the adapter
		this.adapter = new CosmosDocumentAdapter(database, logger);
	}

	/*
	 * ====================== OfficeStore ===========================
	 */

	@Override
	protected <D> OfficeCabinetArchive<D> createOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception {
		return new CosmosOfficeCabinetArchive<>(this.adapter, documentType, indexes);
	}

}