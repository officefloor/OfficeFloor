package net.officefloor.cabinet.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.AbstractOfficeStore.TransactionalChange;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Dynamo {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeStore extends AbstractOfficeStore<DynamoDocumentMetaData<?>, DynamoTransaction> {

	/**
	 * {@link AmazonDynamoDB}.
	 */
	private final AmazonDynamoDB amazonDynamoDb;

	/**
	 * {@link DynamoDB}.
	 */
	private final DynamoDB dynamoDb;

	/**
	 * Maximum batch size for writing to {@link DynamoDB}.
	 */
	private final int maxBatchSize;

	/**
	 * Instantiate.
	 * 
	 * @param amazonDynamoDb {@link AmazonDynamoDB}.
	 * @param maxBatchSize   Maximum batch size for writing to {@link DynamoDB}.
	 */
	public DynamoOfficeStore(AmazonDynamoDB amazonDynamoDb, int maxBatchSize) {
		this.amazonDynamoDb = amazonDynamoDb;
		this.dynamoDb = new DynamoDB(amazonDynamoDb);
		this.maxBatchSize = maxBatchSize;
	}

	/*
	 * ==================== AbstractOfficeStore ==========================
	 */

	@Override
	protected <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType) throws Exception {
		return (AbstractDocumentAdapter<R, S>) new DynamoDocumentAdapter(this.dynamoDb, this);
	}

	@Override
	public <R, S, D> DynamoDocumentMetaData<?> createExtraMetaData(
			DocumentMetaData<R, S, D, DynamoDocumentMetaData<?>, DynamoTransaction> metaData, Index[] indexes)
			throws Exception {
		return new DynamoDocumentMetaData<>((DocumentMetaData) metaData, indexes, dynamoDb);
	}

	@Override
	public AbstractSectionAdapter createSectionAdapter() throws Exception {
		return new DynamoSectionAdapter(this);
	}

	@Override
	public void transact(TransactionalChange<DynamoTransaction> change) throws Exception {
		DynamoTransaction transaction = new DynamoTransaction(this.amazonDynamoDb);
		change.transact(transaction);
		transaction.commit();
	}

	@Override
	public <D, R, S> AbstractOfficeCabinet<R, S, D, DynamoDocumentMetaData<?>, DynamoTransaction> createOfficeCabinet(
			DocumentMetaData<R, S, D, DynamoDocumentMetaData<?>, DynamoTransaction> metaData,
			CabinetManager cabinetManager) {
		return new DynamoOfficeCabinet<>((DocumentMetaData) metaData, cabinetManager, this.dynamoDb, this.maxBatchSize);
	}

}