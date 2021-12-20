package net.officefloor.cabinet.key;

import net.officefloor.cabinet.Document;

/**
 * Means to obtain the {@link DocumentKey} from the input {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DocumentKey<D> {

	/**
	 * Obtains the name to locate the key.
	 * 
	 * @return Name to locate the key.
	 */
	String getKeyName();

	/**
	 * Obtains the {@link Document} key for the {@link Document}.
	 * 
	 * @param document {@link Document}.
	 * @return {@link Document} Key.
	 * @throws Exception If fails to obtain the {@link Document} key.
	 */
	String getKey(D document) throws Exception;

	/**
	 * Specifies the {@link Document} key on the {@link Document}.
	 * 
	 * @param document {@link Document}.
	 * @param key      Key for the {@link Document}.
	 * @throws Exception If fails to specify the key on the {@link Document}.
	 */
	void setKey(D document, String key) throws Exception;

}