package net.officefloor.cabinet.cosmosdb;

import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * Cosmos DB {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	/**
	 * {@link CosmosDocumentMetaData}.
	 */
	private final CosmosDocumentMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link CosmosDocumentAdapter}.
	 * @param documentType Document type.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	public CosmosOfficeCabinetArchive(CosmosDocumentAdapter adapter, Class<D> documentType) throws Exception {
		this.metaData = (CosmosDocumentMetaData<D>) adapter.createDocumentMetaData(documentType);
	}

	/*
	 * ==================== OfficeCabinetArchive ====================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		return new CosmosOfficeCabinet<>(this.metaData);
	}

	@Override
	public void close() throws Exception {
		// TODO look at closing Cosmos connection
	}

}