package net.officefloor.cabinet.cosmosdb;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.OfficeCabinetArchive;

/**
 * Cosmos DB {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	/**
	 * {@link CosmosOfficeCabinetMetaData}.
	 */
	private final CosmosOfficeCabinetMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	public CosmosOfficeCabinetArchive(Class<D> documentType, CosmosDatabase cosmosDatabase) throws Exception {
		this.metaData = new CosmosOfficeCabinetMetaData<>(documentType, cosmosDatabase);
	}

	/*
	 * ==================== OfficeCabinetArchive ====================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		return new CosmosOfficeCabinet<>(this.metaData);
	}

}