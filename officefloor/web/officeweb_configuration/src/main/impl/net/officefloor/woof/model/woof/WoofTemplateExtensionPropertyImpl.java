package net.officefloor.woof.model.woof;

import net.officefloor.woof.model.woof.WoofTemplateExtensionProperty;

/**
 * {@link WoofTemplateExtensionProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionPropertyImpl implements
		WoofTemplateExtensionProperty {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public WoofTemplateExtensionPropertyImpl(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/*
	 * ================== WoofTemplateExtensionProperty =====================
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