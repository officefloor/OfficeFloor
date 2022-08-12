package net.officefloor.cabinet.common;

import java.util.Iterator;

import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * {@link InternalDocument} {@link DocumentBundle}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InternalDocumentBundle<R> extends Iterator<R> {

	/**
	 * Obtains the next {@link DocumentBundle} token.
	 * 
	 * @param context {@link NextDocumentBundleContext}.
	 * @return Next {@link DocumentBundle} token or <code>null</code> if no further
	 *         {@link InternalDocumentBundle} instances.
	 */
	String getNextDocumentBundleToken(NextDocumentBundleTokenContext<R> context);

	/**
	 * Obtains the next {@link InternalDocumentBundle}.
	 * 
	 * @param context {@link NextDocumentBundleContext}.
	 * @return Next {@link InternalDocumentBundle} or <code>null</code> to indicate
	 *         no further {@link InternalDocumentBundle} instances.
	 */
	InternalDocumentBundle<R> nextDocumentBundle(NextDocumentBundleContext context);

}