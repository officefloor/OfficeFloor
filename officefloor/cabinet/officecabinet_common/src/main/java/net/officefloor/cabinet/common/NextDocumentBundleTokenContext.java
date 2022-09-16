package net.officefloor.cabinet.common;

import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Context for the next {@link DocumentBundle} token.
 */
public interface NextDocumentBundleTokenContext<R> {

	/**
	 * Obtains the last {@link InternalDocument}.
	 * 
	 * @return Last {@link InternalDocument}.
	 */
	R getLastInternalDocument();

	/**
	 * Obtains the token derived from the last {@link InternalDocument}.
	 * 
	 * @return Token derived from the last {@link InternalDocument}.
	 */
	String getLastInternalDocumentToken();

}
