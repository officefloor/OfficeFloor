package net.officefloor.server.http.impl;

import java.io.Serializable;

import net.officefloor.server.http.HttpHeader;

/**
 * {@link Serializable} {@link HttpHeader}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpHeader implements HttpHeader, Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public SerialisableHttpHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/*
	 * ================= HttpHeader =====================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
