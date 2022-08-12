package net.officefloor.cabinet.common;

import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.adapt.StartAfterDocumentValueGetter;

/**
 * Context for the next {@link DocumentBundle}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NextDocumentBundleContext {

	/**
	 * Obtains the next {@link DocumentBundle} token.
	 * 
	 * @return Next {@link DocumentBundle} token.
	 */
	String getNextDocumentBundleToken();

	/**
	 * Obtains the {@link StartAfterDocumentValueGetter}.
	 * 
	 * @return {@link StartAfterDocumentValueGetter}.
	 */
	StartAfterDocumentValueGetter getStartAfterDocumentValueGetter();

}
