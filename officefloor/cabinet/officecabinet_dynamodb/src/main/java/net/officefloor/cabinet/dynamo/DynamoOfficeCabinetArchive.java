package net.officefloor.cabinet.dynamo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * Dynamo DB {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	/**
	 * {@link DynamoOfficeCabinetArchive}.
	 */
	private final DynamoDocumentMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link DynamoDocumentAdapter}.
	 * @param documentType Document type.
	 * @param dynamoDb     {@link DynamoDB}.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	public DynamoOfficeCabinetArchive(DynamoDocumentAdapter adapter, Class<D> documentType, DynamoDB dynamoDb)
			throws Exception {
		this.metaData = new DynamoDocumentMetaData<>(adapter, documentType, dynamoDb);
	}

	/*
	 * ================= OfficeCabinetArchive ======================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		return new DynamoOfficeCabinet<>(this.metaData);
	}

	@Override
	public void close() throws Exception {
		// TODO consider closing connection
	}

}