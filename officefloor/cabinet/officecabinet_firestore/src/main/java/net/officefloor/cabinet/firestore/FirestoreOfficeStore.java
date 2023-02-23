package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * {@link Firestore} {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeStore extends AbstractOfficeStore<FirestoreDocumentMetaData<?>> {

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
			DocumentMetaData<R, S, D, FirestoreDocumentMetaData<?>> metaData, Index[] indexes) throws Exception {
		return new FirestoreDocumentMetaData<>(metaData.documentType);
	}

	@Override
	public AbstractSectionAdapter createSectionAdapter() throws Exception {
		return new FirestoreSectionAdapter(this);
	}

	@Override
	public <D, R, S> OfficeCabinet<D> createOfficeCabinet(
			DocumentMetaData<R, S, D, FirestoreDocumentMetaData<?>> metaData) {
		return new FirestoreOfficeCabinet<>((DocumentMetaData) metaData, firestore);
	}

}
