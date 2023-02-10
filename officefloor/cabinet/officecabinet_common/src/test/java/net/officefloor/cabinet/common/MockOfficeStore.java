package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Mock {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockOfficeStore extends AbstractOfficeStore {

	/**
	 * {@link Document} store.
	 */
	private final Map<Class<?>, Map<String, ?>> documentStores = new HashMap<>();

	/**
	 * Obtains the {@link Document} store for {@link Document} type.
	 * 
	 * @param <D>          {@link Document} type.
	 * @param documentType {@link Document} type.
	 * @return {@link Document} store.
	 */
	private <D> Map<String, D> getDocumentStore(Class<D> documentType) {

		// Lazy load the document store
		Map<String, ?> documentStore = this.documentStores.get(documentType);
		if (documentStore == null) {
			documentStore = new HashMap<>();
			this.documentStores.put(documentType, documentStore);
		}

		// Return the document store
		return (Map<String, D>) documentStore;
	}

	/*
	 * ======================== AbstractOfficeStore ===============================
	 */

	@Override
	public <D, R, S> OfficeCabinet<D> createOfficeCabinet(DocumentMetaData<R, S, D> metaData) {

		// Obtain the document store
		Map<String, D> documentStore = this.getDocumentStore(metaData.documentType);

		// Return the created office cabinet
		try {
			return new MockOfficeCabinet<>((DocumentMetaData<D, D, D>) metaData, documentStore);
		} catch (Exception ex) {
			return fail("Failed to create " + MockOfficeCabinet.class.getName(), ex);
		}
	}

	@Override
	protected <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType) {
		return (AbstractDocumentAdapter<R, S>) new MockDocumentAdapter<>(documentType, this);
	}

}