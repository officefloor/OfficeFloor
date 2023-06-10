package net.officefloor.cabinet.dynamo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateGlobalSecondaryIndexAction;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.util.CabinetUtil;

/**
 * Meta-data for the {@link DynamoOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDocumentMetaData<D> {

	private static String getIndexName(Index index) {
		String partitionFieldName = index.getFields()[0].fieldName;
		String sortFieldName = index.getSortFieldName();
		return partitionFieldName + (sortFieldName != null ? "-" + sortFieldName : "");
	}

	private static KeySchemaElement[] getKeySchemaElements(Index index) {

		// Provide key schemas
		List<KeySchemaElement> keySchemas = new ArrayList<>(1);

		// Provide partition key
		String partitionFieldName = index.getFields()[0].fieldName;
		keySchemas.add(new KeySchemaElement(partitionFieldName, KeyType.HASH));

		// Provide sort key (if required)
		String sortFieldName = index.getSortFieldName();
		if (sortFieldName != null) {
			keySchemas.add(new KeySchemaElement(sortFieldName, KeyType.RANGE));
		}

		// Return the key schema elements
		return keySchemas.toArray(KeySchemaElement[]::new);
	}

	private static AttributeDefinition getAttributeDefinition(String fieldName) {
		switch (fieldName) {
		case "testName":
			return new AttributeDefinition(fieldName, ScalarAttributeType.S.name());
		case "intPrimitive":
			return new AttributeDefinition(fieldName, ScalarAttributeType.N.name());
		case "offset":
			return new AttributeDefinition(fieldName, ScalarAttributeType.N.name());
		default:
			throw new UnsupportedOperationException("TODO implement typing fields - " + fieldName);
		}
	}

	/**
	 * Name of table for {@link Document}.
	 */
	final String tableName;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Document type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param dynamoDb     {@link DynamoDB}.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	public DynamoDocumentMetaData(
			DocumentMetaData<Item, Item, D, DynamoDocumentMetaData<D>, DynamoTransaction> metaData, Index[] indexes,
			DynamoDB dynamoDb) throws Exception {

		// Obtain the table name
		this.tableName = CabinetUtil.getDocumentName(metaData.documentType);

		// Load provisioned through put
		// TODO configure read/write provisioned throughput
		ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(25L, 25L);

		// Obtain the table
		Table table;
		TableDescription tableDesc;
		try {
			// Determine if table exists
			table = dynamoDb.getTable(this.tableName);
			tableDesc = table.describe();

		} catch (ResourceNotFoundException ex) {
			// Table not exists

			// Create table request
			List<AttributeDefinition> attributeDefinitions = new LinkedList<>();
			List<KeySchemaElement> keys = new LinkedList<>();
			keys.add(new KeySchemaElement(metaData.getKeyName(), KeyType.HASH));
			attributeDefinitions.add(new AttributeDefinition(metaData.getKeyName(), ScalarAttributeType.S.name()));
			CreateTableRequest createTable = new CreateTableRequest(attributeDefinitions, this.tableName, keys,
					provisionedThroughput);

			// Create the table
			table = dynamoDb.createTable(createTable);
			tableDesc = table.waitForActive();
		}

		// Create global secondary indexes for new indexes
		List<GlobalSecondaryIndexDescription> gsis = tableDesc.getGlobalSecondaryIndexes();
		Set<String> existingIndexNames = gsis != null
				? gsis.stream().map((index) -> index.getIndexName()).collect(Collectors.toSet())
				: Collections.emptySet();
		NEXT_INDEX: for (Index index : indexes) {

			// Ensure not already exists
			String indexName = getIndexName(index);
			if (existingIndexNames.contains(indexName)) {
				continue NEXT_INDEX;
			}

			// Add the global secondary index
			KeySchemaElement[] keySchemaElements = getKeySchemaElements(index);
			CreateGlobalSecondaryIndexAction createGsi = new CreateGlobalSecondaryIndexAction().withIndexName(indexName)
					.withKeySchema(keySchemaElements).withProvisionedThroughput(provisionedThroughput)
					.withProjection(new Projection().withProjectionType(ProjectionType.ALL));
			com.amazonaws.services.dynamodbv2.document.Index gsi;
			switch (keySchemaElements.length) {
			case 1:
				gsi = table.createGSI(createGsi, getAttributeDefinition(keySchemaElements[0].getAttributeName()));
				break;

			case 2:
				gsi = table.createGSI(createGsi, getAttributeDefinition(keySchemaElements[0].getAttributeName()),
						getAttributeDefinition(keySchemaElements[1].getAttributeName()));
				break;

			default:
				throw new IllegalStateException("Can only have 1 or 2 " + KeySchemaElement.class.getSimpleName()
						+ " entries for " + Index.class.getSimpleName());
			}
			gsi.waitForActive();
		}
	}

}