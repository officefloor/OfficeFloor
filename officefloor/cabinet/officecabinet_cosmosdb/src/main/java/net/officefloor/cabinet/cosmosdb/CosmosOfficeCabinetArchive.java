package net.officefloor.cabinet.cosmosdb;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.cabinet.OfficeCabinet;
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
	 * @param adapter        {@link CosmosOfficeCabinetAdapter}.
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	public CosmosOfficeCabinetArchive(CosmosOfficeCabinetAdapter adapter, Class<D> documentType,
			CosmosDatabase cosmosDatabase) throws Exception {
		this.metaData = new CosmosDocumentMetaData<>(adapter, documentType, cosmosDatabase);
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