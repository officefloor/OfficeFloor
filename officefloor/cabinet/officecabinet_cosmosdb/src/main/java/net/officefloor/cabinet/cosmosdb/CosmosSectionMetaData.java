package net.officefloor.cabinet.cosmosdb;

import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.Document;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.metadata.AbstractSectionMetaData;

/**
 * Meta-data for the {@link CosmosOfficeCabinet} {@link Document} section.
 * 
 * @author Daniel Sagenschneider
 */
class CosmosSectionMetaData<D> extends AbstractSectionMetaData<CosmosSectionAdapter, D> {

	/**
	 * Instantiate.
	 * 
	 * @param adapter        {@link CosmosSectionAdapter}.
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	CosmosSectionMetaData(CosmosSectionAdapter adapter, Class<D> documentType) throws Exception {
		super(adapter, documentType);
	}

}