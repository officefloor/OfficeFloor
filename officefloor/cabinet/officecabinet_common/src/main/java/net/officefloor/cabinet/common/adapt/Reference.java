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
	 * {@link Key} to {@link Document} being referenced.
	 */
	private String key;

	/**
	 * Instantiate.
	 * 
	 * @param document {@link Document} being referenced.
	 * @param key      {@link Key} to {@link Document} being referenced.
	 */
	public Reference(Object document, String key) {
		this.document = document;
		this.key = key;
	}

	/**
	 * Obtains the {@link Key} to {@link Document} being referenced.
	 * 
	 * @return {@link Key} to {@link Document} being referenced.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Specifies the {@link Key} to {@link Document} being referenced.
	 * 
	 * @param key {@link Key} to {@link Document} being referenced.
	 */
	public void setKey(String key) {
		this.key = key;
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