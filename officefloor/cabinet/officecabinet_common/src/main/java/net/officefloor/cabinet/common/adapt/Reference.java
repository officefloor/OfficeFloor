package net.officefloor.cabinet.common.adapt;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * Reference to another {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class Reference {

	/**
	 * {@link Document} being referenced.
	 */
	private final Object document;

	/**
	 * Instantiate.
	 * 
	 * @param document {@link Document} being referenced.
	 */
	public Reference(Object document) {
		this.document = document;
	}

	/**
	 * Obtains the {@link Document} being referenced.
	 * 
	 * @return {@link Document} being referenced.
	 */
	public Object getDocument() {
		return document;
	}

}