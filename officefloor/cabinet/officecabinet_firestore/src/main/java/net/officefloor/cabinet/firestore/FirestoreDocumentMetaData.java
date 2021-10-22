package net.officefloor.cabinet.firestore;

import java.util.Map;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Meta-data for the {@link FirestoreOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreDocumentMetaData<D>
		extends AbstractDocumentMetaData<DocumentSnapshot, Map<String, Object>, FirestoreDocumentAdapter, D> {

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
	 * @param adapter      {@link FirestoreDocumentAdapter}.
	 * @param documentType Type of document.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param firestore    {@link Firestore}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreDocumentMetaData(FirestoreDocumentAdapter adapter, Class<D> documentType, Index[] indexes,
			Firestore firestore) throws Exception {
		super(adapter, documentType);
		this.firestore = firestore;

		// Obtain the collection id
		this.collectionId = CabinetUtil.getDocumentName(documentType);
	}

}