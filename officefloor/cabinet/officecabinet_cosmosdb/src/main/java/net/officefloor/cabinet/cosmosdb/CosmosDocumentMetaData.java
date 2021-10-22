package net.officefloor.cabinet.cosmosdb;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKeyDefinition;

import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.nosql.cosmosdb.CosmosDbUtil;

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
	 * @param indexes        {@link Index} instances for the {@link Document}.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @param logger         {@link Logger}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	CosmosDocumentMetaData(CosmosDocumentAdapter adapter, Class<D> documentType, Index[] indexes,
			CosmosDatabase cosmosDatabase, Logger logger) throws Exception {
		super(adapter, documentType);

		// Obtain the container id
		String containerId = CabinetUtil.getDocumentName(documentType);

		// Create the container
		CosmosContainerProperties createContainer = new CosmosContainerProperties(containerId,
				new PartitionKeyDefinition().setPaths(Arrays.asList("/" + Constants.Properties.ID)));
		CosmosDbUtil.createContainers(cosmosDatabase, Arrays.asList(createContainer), 120, logger, Level.INFO);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

}
