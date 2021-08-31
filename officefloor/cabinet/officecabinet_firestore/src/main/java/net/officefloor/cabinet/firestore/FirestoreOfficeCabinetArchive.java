package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * {@link Firestore} {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	/**
	 * {@link FirestoreDocumentMetaData}.
	 */
	private final FirestoreDocumentMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link FirestoreDocumentAdapter}.
	 * @param documentType Type of document.
	 * @param firestore    {@link Firestore}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreOfficeCabinetArchive(FirestoreDocumentAdapter adapter, Class<D> documentType,
			Firestore firestore) throws Exception {
		this.metaData = new FirestoreDocumentMetaData<>(adapter, documentType, firestore);
	}

	/*
	 * =================== OfficeCabinetArchive ===================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		return new FirestoreOfficeCabinet<>(this.metaData);
	}

	@Override
	public void close() throws Exception {
		// TODO consider closing the connection
	}

}
