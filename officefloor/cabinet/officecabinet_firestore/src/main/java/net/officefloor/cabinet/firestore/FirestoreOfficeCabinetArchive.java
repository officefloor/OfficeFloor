package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.OfficeCabinetArchive;

/**
 * {@link Firestore} {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	/**
	 * {@link FirestoreOfficeCabinetMetaData}.
	 */
	private final FirestoreOfficeCabinetMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Type of document.
	 * @param firestore    {@link Firestore}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreOfficeCabinetArchive(Class<D> documentType, Firestore firestore) throws Exception {
		this.metaData = new FirestoreOfficeCabinetMetaData<>(documentType, firestore);
	}

	/*
	 * =================== OfficeCabinetArchive ===================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		return new FirestoreOfficeCabinet<>(this.metaData);
	}

}
