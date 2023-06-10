package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.TransactionOptions;

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
 * {@link Firestore} {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeStore extends AbstractOfficeStore<FirestoreDocumentMetaData<?>, FirestoreTransaction> {

	/**
	 * {@link Firestore}.
	 */
	private final Firestore firestore;

	/**
	 * Instantiate.
	 * 
	 * @param firestore {@link Firestore}.
	 */
	public FirestoreOfficeStore(Firestore firestore) {
		this.firestore = firestore;
	}

	/*
	 * ========================== AbstractOfficeStore =============================
	 */

	@Override
	protected <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType) throws Exception {
		return (AbstractDocumentAdapter<R, S>) new FirestoreDocumentAdapter(this);
	}

	@Override
	public <R, S, D> FirestoreDocumentMetaData<?> createExtraMetaData(
			DocumentMetaData<R, S, D, FirestoreDocumentMetaData<?>, FirestoreTransaction> metaData, Index[] indexes)
			throws Exception {
		return new FirestoreDocumentMetaData<>(metaData.documentType);
	}

	@Override
	public AbstractSectionAdapter createSectionAdapter() throws Exception {
		return new FirestoreSectionAdapter(this);
	}

	@Override
	public void transact(TransactionalChange<FirestoreTransaction> change) throws Exception {

		// Undertake within a transaction
		this.firestore.runTransaction((transaction) -> {

			// Undertake functionality of transaction
			change.transact(new FirestoreTransaction(this.firestore, transaction));

			// No state from transaction
			return null;
		}).get();
	}

	@Override
	public <D, R, S> AbstractOfficeCabinet<R, S, D, FirestoreDocumentMetaData<?>, FirestoreTransaction> createOfficeCabinet(
			DocumentMetaData<R, S, D, FirestoreDocumentMetaData<?>, FirestoreTransaction> metaData,
			CabinetManager cabinetManager) {
		return new FirestoreOfficeCabinet<>((DocumentMetaData) metaData, cabinetManager, firestore);
	}

}
