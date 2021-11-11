package net.officefloor.cabinet;

import java.util.Iterator;

/**
 * Bundle of {@link Document} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface DocumentBundle<D> extends Iterator<D> {

	/**
	 * Obtains the next {@link DocumentBundle}.
	 * 
	 * @return Next {@link DocumentBundle} or <code>null</code> if no further
	 *         {@link DocumentBundle} instances.
	 */
	DocumentBundle<D> nextDocumentBundle();

}