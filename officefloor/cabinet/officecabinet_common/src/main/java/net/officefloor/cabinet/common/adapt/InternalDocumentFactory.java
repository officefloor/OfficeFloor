package net.officefloor.cabinet.common.adapt;

import net.officefloor.cabinet.Document;

/**
 * Creates the internal {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface InternalDocumentFactory<S> {

	/**
	 * Creates the internal {@link Document}.
	 * 
	 * @return Internal {@link Document}.
	 */
	S createInternalDocument();

}
