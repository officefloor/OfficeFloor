package net.officefloor.cabinet.common;

import java.util.Iterator;

import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.adapt.StartAfterDocumentValueGetter;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * {@link InternalDocument} {@link DocumentBundle}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InternalDocumentBundle<R> extends Iterator<R> {

	/**
	 * Obtains the next {@link InternalDocumentBundle} starting after the input
	 * {@link InternalDocument}.
	 * 
	 * @param startAfterDocumentValueGetter {@link StartAfterDocumentValueGetter} to
	 *                                      start after.
	 * @return Next {@link InternalDocumentBundle} or <code>null</code> to indicate
	 *         no further {@link InternalDocumentBundle} instances.
	 */
	InternalDocumentBundle<R> nextDocumentBundle(StartAfterDocumentValueGetter startAfterDocumentValueGetter);

}