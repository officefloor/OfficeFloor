package net.officefloor.cabinet.common;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;

/**
 * Registers the {@link Document} meta-data.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface RegisterDocumentMetaData {

	/**
	 * Registers the {@link Document} meta-data.
	 * 
	 * @param <D>              {@link Document} type.
	 * @param documentType     {@link Document} type.
	 * @param documentMetaData {@link Document} meta-data.
	 */
	void register(Class<?> documentType, DocumentMetaData<?, ?, ?> documentMetaData);

}
