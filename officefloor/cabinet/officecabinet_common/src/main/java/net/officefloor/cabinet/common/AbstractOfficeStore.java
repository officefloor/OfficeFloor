package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Abstract {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeStore implements OfficeStore {

	/**
	 * {@link DocumentMetaData} instances by their {@link Document} type.
	 */
	protected final Map<Class<?>, DocumentMetaData<?, ?, ?>> documentMetaDatas = new HashMap<>();

	/**
	 * Creates the {@link AbstractDocumentAdapter}.
	 * 
	 * @param <R>          {@link Document} type.
	 * @param <S>          {@link InternalDocument} type.
	 * @param <D>          {@link Document} type.
	 * @param documentType {@link Document} type.
	 * @return {@link AbstractDocumentAdapter}.
	 */
	protected abstract <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType);

	/**
	 * Creates the {@link AbstractSectionAdapter} for the section type.
	 * 
	 * @return {@link AbstractSectionAdapter}.
	 * @throws Exception If fails to create {@link AbstractSectionAdapter}.
	 */
	public AbstractSectionAdapter createSectionAdapter() throws Exception {
		return new AbstractSectionAdapter(this) {

			@Override
			protected void initialise(AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>>.Initialise init)
					throws Exception {
				// Nothing to initialise
			}
		};
	}

	/**
	 * Creates the {@link OfficeCabinet}.
	 * 
	 * @param <D>      {@link Document} type.
	 * @param <R>      Retrieving {@link InternalDocument} type.
	 * @param <S>      Storing {@link InternalDocument} type.
	 * @param metaData {@link DocumentMetaData}.
	 * @return {@link OfficeCabinet}.
	 */
	public abstract <D, R, S> OfficeCabinet<D> createOfficeCabinet(DocumentMetaData<R, S, D> metaData);

	/*
	 * ====================== OfficeStore =========================
	 */

	@Override
	public <D> void setupOfficeCabinet(Class<D> documentType, Index... indexes) throws Exception {

		// Ensure document type not already registered
		if (this.documentMetaDatas.containsKey(documentType)) {
			throw new IllegalStateException(
					OfficeCabinet.class.getSimpleName() + " already setup for document type " + documentType.getName());
		}

		// Create the adapter
		@SuppressWarnings("rawtypes")
		AbstractDocumentAdapter adapter = this.createDocumentAdapter(documentType);

		// Create to register itself
		new DocumentMetaData<>(adapter, documentType, this, (type, metaData) -> {
			this.documentMetaDatas.put(type, metaData);
		});
	}

	@Override
	public CabinetManager createCabinetManager() {
		return new CabinetManagerImpl(this.documentMetaDatas, this);
	}

}