package net.officefloor.cabinet.cosmosdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;

import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.util.CabinetUtil;
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

		// Configure the container
		CosmosContainerProperties createContainer = new CosmosContainerProperties(containerId,
				new PartitionKeyDefinition().setPaths(Arrays.asList("/" + Constants.Properties.ID)));

		// Provide the indexing policies
		List<List<CompositePath>> compositeIndexes = new ArrayList<>();
		for (Index index : indexes) {

			// Determine if sort
			String sortFieldName = index.getSortFieldName();
			if (sortFieldName != null) {

				// Sort path
				CompositePath sortPath = new CompositePath();
				sortPath.setPath("/" + sortFieldName);

				// Id path for bundling
				CompositePath idPath = new CompositePath();
				idPath.setPath("/" + Constants.Properties.ID);

				// Include the composite index
				compositeIndexes.add(Arrays.asList(sortPath, idPath));
			}
		}
		IndexingPolicy indexingPolicy = createContainer.getIndexingPolicy();
		indexingPolicy.setCompositeIndexes(compositeIndexes);

		// Create the container
		CosmosDbUtil.createContainers(cosmosDatabase, Arrays.asList(createContainer), 120, logger, Level.INFO);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

}
