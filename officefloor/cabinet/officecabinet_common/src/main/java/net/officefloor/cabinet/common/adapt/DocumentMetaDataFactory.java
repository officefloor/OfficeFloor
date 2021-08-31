package net.officefloor.cabinet.common.adapt;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;

/**
 * Factory for the {@link AbstractDocumentMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DocumentMetaDataFactory<R, S, A extends AbstractDocumentAdapter<R, S, A>> {

	/**
	 * Creates the {@link AbstractDocumentMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param adapter      {@link AbstractDocumentAdapter}.
	 * @return {@link AbstractDocumentMetaData}.
	 * @throws Exception If fails to create the {@link AbstractDocumentMetaData}.
	 */
	<D> AbstractDocumentMetaData<R, S, A, D> createDocumentMetaData(Class<D> documentType, A adapter) throws Exception;

}