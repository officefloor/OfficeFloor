package net.officefloor.cabinet.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Dynamo {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeStore extends AbstractOfficeStore<DynamoDocumentMetaData<?>> {

	/**
	 * {@link DynamoDB}.
	 */
	private final DynamoDB dynamoDb;

	/**
	 * Instantiate.
	 * 
	 * @param amazonDynamoDb {@link AmazonDynamoDB}.
	 */
	public DynamoOfficeStore(AmazonDynamoDB amazonDynamoDb) {
		this.dynamoDb = new DynamoDB(amazonDynamoDb);
	}

	/*
	 * ==================== AbstractOfficeStore ==========================
	 */

	@Override
	protected <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType) {
		return (AbstractDocumentAdapter<R, S>) new DynamoDocumentAdapter(this.dynamoDb, this);
	}

	@Override
	public <R, S, D> DynamoDocumentMetaData<?> createExtraMetaData(
			DocumentMetaData<R, S, D, DynamoDocumentMetaData<?>> metaData, Index[] indexes) throws Exception {
		return new DynamoDocumentMetaData<>((DocumentMetaData) metaData, indexes, dynamoDb);
	}

	@Override
	public AbstractSectionAdapter createSectionAdapter() throws Exception {
		return new DynamoSectionAdapter(this);
	}

	@Override
	public <D, R, S> OfficeCabinet<D> createOfficeCabinet(
			DocumentMetaData<R, S, D, DynamoDocumentMetaData<?>> metaData) {
		return new DynamoOfficeCabinet<>((DocumentMetaData) metaData, this.dynamoDb);
	}

}