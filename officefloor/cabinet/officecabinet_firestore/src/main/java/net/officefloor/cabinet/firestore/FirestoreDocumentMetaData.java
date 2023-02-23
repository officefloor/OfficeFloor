package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.CollectionReference;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.util.CabinetUtil;

/**
 * Meta-data for the {@link FirestoreOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreDocumentMetaData<D> {

	/**
	 * Id of {@link CollectionReference}.
	 */
	final String collectionId;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Type of document.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreDocumentMetaData(Class<D> documentType) throws Exception {

		// Obtain the collection id
		this.collectionId = CabinetUtil.getDocumentName(documentType);
	}

}