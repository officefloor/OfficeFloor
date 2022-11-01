package net.officefloor.cabinet.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Dynamo {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeStore implements OfficeStore {

	/**
	 * {@link DynamoDocumentAdapter}.
	 */
	private final DynamoDocumentAdapter adapter;

	/**
	 * Instantiate.
	 * 
	 * @param amazonDynamoDb {@link AmazonDynamoDB}.
	 */
	public DynamoOfficeStore(AmazonDynamoDB amazonDynamoDb) {
		this.adapter = new DynamoDocumentAdapter(new DynamoDB(amazonDynamoDb));
	}

	/*
	 * ==================== OfficeStore ==========================
	 */

	@Override
	public <D> OfficeCabinetArchive<D> setupOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception {
		return new DynamoOfficeCabinetArchive<>(adapter, documentType, indexes);
	}

}