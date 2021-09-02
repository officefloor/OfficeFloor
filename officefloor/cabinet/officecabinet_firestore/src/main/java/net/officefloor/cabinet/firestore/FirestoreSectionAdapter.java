package net.officefloor.cabinet.firestore;

import java.util.Map;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;

/**
 * {@link Firestore} {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreSectionAdapter extends AbstractSectionAdapter<FirestoreSectionAdapter> {

	/**
	 * Creates the {@link FirestoreSectionMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param adapter      {@link FirestoreSectionAdapter}.
	 * @return {@link FirestoreSectionMetaData}.
	 * @throws Exception If fails to create {@link FirestoreSectionMetaData}.
	 */
	private <D> FirestoreSectionMetaData<D> createSectionMetaData(Class<D> documentType,
			FirestoreSectionAdapter adapter) throws Exception {
		return new FirestoreSectionMetaData<>(adapter, documentType);
	}

	/*
	 * ==================== AbstractSectionAdapter =========================
	 */

	@Override
	protected void initialise(
			AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, FirestoreSectionAdapter>.Initialise init)
			throws Exception {

		// Document meta-data
		init.setDocumentMetaDataFactory(this::createSectionMetaData);
	}

}
