package net.officefloor.cabinet.common.metadata;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * Internal {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class InternalDocument<I> {

	/**
	 * {@link Key} for {@link Document}.
	 */
	private final String key;

	/**
	 * Internal {@link Document}.
	 */
	private final I internalDocument;

	/**
	 * Indicates if new {@link Document}.
	 */
	private final boolean isNew;

	/**
	 * Instantiate.
	 * 
	 * @param key              {@link Key} for {@link Document}.
	 * @param internalDocument Internal {@link Document}.
	 * @param isNew            Indicates if new {@link Document}.
	 */
	public InternalDocument(String key, I internalDocument, boolean isNew) {
		this.key = key;
		this.internalDocument = internalDocument;
		this.isNew = isNew;
	}

	/**
	 * Obtains the {@link Key} for the {@link Document}.
	 * 
	 * @return {@link Key} for the {@link Document}.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Obtains the internal {@link Document}.
	 * 
	 * @return Internal {@link Document}.
	 */
	public I getInternalDocument() {
		return internalDocument;
	}

	/**
	 * Indicates if new {@link Document}.
	 * 
	 * @return <code>true</code> if new {@link Document}.
	 */
	public boolean isNew() {
		return isNew;
	}

}