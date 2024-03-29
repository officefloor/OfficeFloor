package net.officefloor.cabinet.dynamo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	 * Registered items.
	 */
	private final Map<String, Set<String>> registeredItems = new HashMap<>();

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

		// Obtain the registered set for the table
		Set<String> registeredTableKeys = this.registeredItems.get(tableName);
		if (registeredTableKeys == null) {
			registeredTableKeys = new HashSet<>();
			this.registeredItems.put(tableName, registeredTableKeys);
		}

		// Store the internal documents
		NEXT_DOCUMENT: for (InternalDocument<Map<String, AttributeValue>> internalDocument : internalDocuments) {

			// Determine if registered
			String key = internalDocument.getKey();
			if (registeredTableKeys.contains(key)) {
				continue NEXT_DOCUMENT; // already registered in transaction
			}

			// Obtain the item
			Map<String, AttributeValue> item = internalDocument.getInternalDocument();

			// Include within transaction
			Put put = new Put().withTableName(tableName).withItem(item);
			this.transactItems.add(new TransactWriteItem().withPut(put));

			// Registered
			registeredTableKeys.add(key);
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
