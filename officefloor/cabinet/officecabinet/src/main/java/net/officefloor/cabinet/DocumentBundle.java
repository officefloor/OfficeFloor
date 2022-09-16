package net.officefloor.cabinet;

import java.util.Iterator;

/**
 * Bundle of {@link Document} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface DocumentBundle<D> extends Iterator<D>, Iterable<D> {

	/**
	 * Obtains the next {@link DocumentBundle}.
	 * 
	 * @return Next {@link DocumentBundle} or <code>null</code> if no further
	 *         {@link DocumentBundle} instances.
	 */
	DocumentBundle<D> nextDocumentBundle();

	/**
	 * Obtains the token to indicate starting point for next {@link DocumentBundle}.
	 * 
	 * @return Token to indicate starting point for next {@link DocumentBundle}.
	 */
	String getNextDocumentBundleToken();

}