package net.officefloor.cabinet.inmemory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Transaction.
 * 
 * @author Daniel Sagenschneider
 */
public class InMemoryTransaction {

	/**
	 * Listing of {@link TransactionAddition} instances.
	 */
	private final List<TransactionAddition> additions = new LinkedList<>();

	/**
	 * Adds {@link InternalDocument} instances to the transaction.
	 * 
	 * @param internalDocuments {@link InternalDocument} instances to be included in
	 *                          transaction.
	 */
	public void add(List<InternalDocument<Map<String, Object>>> internalDocuments,
			DocumentMetaData<Map<String, Object>, Map<String, Object>, ?, Map<String, Map<String, Object>>, InMemoryTransaction> metaData) {
		this.additions.add(new TransactionAddition(internalDocuments, metaData));
	}

	public void commit() {
		for (TransactionAddition addition : this.additions) {
			for (InternalDocument<Map<String, Object>> internalDocument : addition.internalDocuments) {
				Map<String, Object> document = internalDocument.getInternalDocument();
				String key = addition.metaData.getInternalDocumentKey(document);
				addition.metaData.extra.put(key, document);
			}
		}
	}

	private static class TransactionAddition {

		private final List<InternalDocument<Map<String, Object>>> internalDocuments;

		private final DocumentMetaData<Map<String, Object>, Map<String, Object>, ?, Map<String, Map<String, Object>>, InMemoryTransaction> metaData;

		private TransactionAddition(List<InternalDocument<Map<String, Object>>> internalDocuments,
				DocumentMetaData<Map<String, Object>, Map<String, Object>, ?, Map<String, Map<String, Object>>, InMemoryTransaction> metaData) {
			this.internalDocuments = internalDocuments;
			this.metaData = metaData;
		}
	}

}
