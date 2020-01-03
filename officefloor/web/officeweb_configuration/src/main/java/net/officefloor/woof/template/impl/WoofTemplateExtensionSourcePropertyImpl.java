package net.officefloor.woof.template.impl;

import net.officefloor.woof.template.WoofTemplateExtensionSourceProperty;

/**
 * Implementation of the {@link WoofTemplateExtensionSourceProperty}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionSourcePropertyImpl implements
		WoofTemplateExtensionSourceProperty {

	/**
	 * Name of property.
	 */
	protected final String name;

	/**
	 * Label of property.
	 */
	protected final String label;

	/**
	 * Initiate with name and label of property.
	 * 
	 * @param name
	 *            Name of property.
	 * @param label
	 *            Label of property.
	 */
	public WoofTemplateExtensionSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * =============== WoofTemplateExtensionSourceProperty =================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}