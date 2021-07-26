package net.officefloor.cabinet.cosmosdb;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosDatabaseResponse;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesEntity;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;

/**
 * Tests the {@link DynamoOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeCabinetTest extends AbstractOfficeCabinetTest {

	@RegisterExtension
	public static final CosmosDbExtension cosmosDb = new CosmosDbExtension();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeCabinet<AttributeTypesEntity> getAttributeTypesOfficeCabinet() throws Exception {

		// Create the database (if required)
		CosmosClient client = cosmosDb.getCosmosClient();
		CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("test");
		CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId());

		// Create and return cabinet
		return new CosmosOfficeCabinet<>(AttributeTypesEntity.class, database);
	}

}