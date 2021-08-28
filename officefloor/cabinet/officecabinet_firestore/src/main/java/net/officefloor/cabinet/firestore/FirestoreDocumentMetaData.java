package net.officefloor.cabinet.firestore;

import java.util.Map;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;

/**
 * Meta-data for the {@link FirestoreOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreDocumentMetaData<D> extends AbstractDocumentMetaData<DocumentSnapshot, Map<String, Object>, D> {

	/**
	 * {@link Firestore}.
	 */
	final Firestore firestore;

	/**
	 * Id of {@link CollectionReference}.
	 */
	final String collectionId;

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link FirestoreOfficeCabinetAdapter}.
	 * @param documentType Type of document.
	 * @param firestore    {@link Firestore}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreDocumentMetaData(FirestoreOfficeCabinetAdapter adapter, Class<D> documentType, Firestore firestore)
			throws Exception {
		super(adapter, documentType);
		this.firestore = firestore;

		// Obtain the collection id
		this.collectionId = CabinetUtil.getDocumentName(documentType);
	}

}