package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * {@link Firestore} {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeStore implements OfficeStore {

	/**
	 * {@link FirestoreDocumentAdapter}.
	 */
	private final FirestoreDocumentAdapter adapter;

	/**
	 * Instantiate.
	 * 
	 * @param firestore {@link Firestore}.
	 */
	public FirestoreOfficeStore(Firestore firestore) {
		this.adapter = new FirestoreDocumentAdapter(firestore);
	}

	/*
	 * ========================== OfficeStore =============================
	 */

	@Override
	public <D> OfficeCabinetArchive<D> setupOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception {
		return new FirestoreOfficeCabinetArchive<>(this.adapter, documentType, indexes);
	}

}
