package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
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
	 * @param indexes      {@link Index} instances of the {@link Document}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreOfficeCabinetArchive(FirestoreDocumentAdapter adapter, Class<D> documentType, Index... indexes)
			throws Exception {
		this.metaData = (FirestoreDocumentMetaData<D>) adapter.createDocumentMetaData(documentType, indexes);
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
