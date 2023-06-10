package net.officefloor.cabinet.dynamo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Put;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;

import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Transaction for {@link DynamoDB}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoTransaction {

	/**
	 * {@link AmazonDynamoDB}.
	 */
	private final AmazonDynamoDB amazonDynamoDb;

	/**
	 * {@link TransactWriteItem}.
	 */
	private final List<TransactWriteItem> transactItems = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param amazonDynamoDb {@link AmazonDynamoDB}.
	 */
	public DynamoTransaction(AmazonDynamoDB amazonDynamoDb) {
		this.amazonDynamoDb = amazonDynamoDb;
	}

	/**
	 * Include the {@link InternalDocument} instances within the transaction.
	 * 
	 * @param tableName         Name of the table.
	 * @param internalDocuments {@link InternalDocument} instances.
	 */
	public void add(String tableName, List<InternalDocument<Map<String, AttributeValue>>> internalDocuments) {
		for (InternalDocument<Map<String, AttributeValue>> internalDocument : internalDocuments) {

			// Obtain the item
			Map<String, AttributeValue> item = internalDocument.getInternalDocument();

			// Include within transaction
			Put put = new Put().withTableName(tableName).withItem(item);
			this.transactItems.add(new TransactWriteItem().withPut(put));
		}
	}

	/**
	 * Commits the transaction.
	 */
	public void commit() {

		// Create the transaction
		TransactWriteItemsRequest transaction = new TransactWriteItemsRequest().withTransactItems(this.transactItems);

		// Write the transaction
		this.amazonDynamoDb.transactWriteItems(transaction);
	}

}
