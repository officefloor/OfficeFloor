package net.officefloor.cabinet.cosmosdb;

import java.util.Arrays;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKeyDefinition;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;

/**
 * Meta-data for the {@link CosmosOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
class CosmosDocumentMetaData<D>
		extends AbstractDocumentMetaData<InternalObjectNode, InternalObjectNode, CosmosDocumentAdapter, D> {

	/**
	 * {@link CosmosContainer}.
	 */
	final CosmosContainer container;

	/**
	 * Instantiate.
	 * 
	 * @param adapter        {@link CosmosDocumentAdapter}.
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	CosmosDocumentMetaData(CosmosDocumentAdapter adapter, Class<D> documentType, CosmosDatabase cosmosDatabase)
			throws Exception {
		super(adapter, documentType);

		// Obtain the container id
		String containerId = CabinetUtil.getDocumentName(documentType);

		// Create the container
		CosmosContainerProperties createContainer = new CosmosContainerProperties(containerId,
				new PartitionKeyDefinition().setPaths(Arrays.asList("/id")));
		cosmosDatabase.createContainerIfNotExists(createContainer);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

}
