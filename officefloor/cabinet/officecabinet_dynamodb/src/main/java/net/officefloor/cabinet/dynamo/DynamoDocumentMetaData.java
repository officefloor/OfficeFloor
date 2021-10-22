package net.officefloor.cabinet.dynamo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Index.IndexField;

/**
 * Meta-data for the {@link DynamoOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDocumentMetaData<D> extends AbstractDocumentMetaData<Item, Item, DynamoDocumentAdapter, D> {

	/**
	 * {@link DynamoDB}.
	 */
	final DynamoDB dynamoDb;

	/**
	 * Name of table for {@link Document}.
	 */
	final String tableName;

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link DynamoDocumentAdapter}.
	 * @param documentType Document type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param dynamoDb     {@link DynamoDB}.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	DynamoDocumentMetaData(DynamoDocumentAdapter adapter, Class<D> documentType, Index[] indexes, DynamoDB dynamoDb)
			throws Exception {
		super(adapter, documentType);
		this.dynamoDb = dynamoDb;

		// Obtain the table name
		this.tableName = CabinetUtil.getDocumentName(documentType);

		try {
			// Determine if table exists
			this.dynamoDb.getTable(this.tableName).describe();

		} catch (ResourceNotFoundException ex) {
			// Table not exists

			// Load provisioned through put
			// TODO configure read/write provisioned throughput
			ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(25L, 25L);

			// Include the key
			List<AttributeDefinition> attributeDefinitions = new LinkedList<>();
			List<KeySchemaElement> keys = new LinkedList<>();
			keys.add(new KeySchemaElement(this.getKeyName(), KeyType.HASH));
			attributeDefinitions.add(new AttributeDefinition(this.getKeyName(), ScalarAttributeType.S.name()));

			// Load the index attributes
			Set<String> indexIncluded = new HashSet<>();
			for (Index index : indexes) {
				for (IndexField field : index.getFields()) {
					indexIncluded.add(field.fieldName);
				}
			}
			for (String fieldName : indexIncluded) {

				// TODO determine attribute type
				attributeDefinitions.add(new AttributeDefinition(fieldName, ScalarAttributeType.N.name()));
			}

			// Create table request
			CreateTableRequest createTable = new CreateTableRequest(attributeDefinitions, this.tableName, keys,
					provisionedThroughput);

			// Add indexes for secondary indexes
			List<GlobalSecondaryIndex> secondaryIndexes = Arrays.stream(indexes).map((index) -> {

				// TODO handle more than one field
				String fieldName = index.getFields()[0].fieldName;
				return new GlobalSecondaryIndex().withIndexName(fieldName)
						.withKeySchema(new KeySchemaElement(fieldName, KeyType.HASH))
						.withProvisionedThroughput(provisionedThroughput)
						.withProjection(new Projection().withProjectionType(ProjectionType.ALL));
			}).collect(Collectors.toList());
			createTable.setGlobalSecondaryIndexes(secondaryIndexes);

			// Create the table
			Table table = this.dynamoDb.createTable(createTable);

			// TODO allow concurrent table creation
			table.waitForActive();
		}
	}

}