package net.officefloor.cabinet.cosmosdb;

import java.util.Arrays;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKeyDefinition;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.DocumentKey;

/**
 * Meta-data for the {@link CosmosOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
class CosmosOfficeCabinetMetaData<D> {

	/**
	 * {@link CosmosContainer}.
	 */
	final CosmosContainer container;

	/**
	 * {@link Document} type.
	 */
	final Class<D> documentType;

	/**
	 * {@link DocumentKey}.
	 */
	final DocumentKey<D> documentKey;

	/**
	 * Instantiate.
	 * 
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	CosmosOfficeCabinetMetaData(Class<D> documentType, CosmosDatabase cosmosDatabase) throws Exception {
		this.documentType = documentType;

		// Obtain the container id
		String containerId = CabinetUtil.getDocumentName(documentType);

		// Search out the key
		this.documentKey = CabinetUtil.getDocumentKey(documentType);
		
		// Provide id field (if key different field)
		if ("id".equals(this.documentKey.getKeyName())) {
			
			// Should be enhanced to have id field added (necessary for CosmosDB)
		}

		// Create the container
		CosmosContainerProperties createContainer = new CosmosContainerProperties(containerId,
				new PartitionKeyDefinition().setPaths(Arrays.asList("/" + this.documentKey.getKeyName())));
		cosmosDatabase.createContainer(createContainer);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

}
