package net.officefloor.cabinet.firestore;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.SectionMetaData;

/**
 * {@link Firestore} {@link SectionMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreSectionMetaData<D> extends SectionMetaData<FirestoreSectionAdapter, D> {

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link FirestoreSectionAdapter}.
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to create {@link FirestoreSectionMetaData}.
	 */
	public FirestoreSectionMetaData(FirestoreSectionAdapter adapter, Class<D> documentType) throws Exception {
		super(adapter, documentType);
	}

}