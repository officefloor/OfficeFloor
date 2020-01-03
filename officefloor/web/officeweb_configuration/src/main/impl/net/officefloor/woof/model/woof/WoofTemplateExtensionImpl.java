package net.officefloor.woof.model.woof;

import net.officefloor.woof.model.woof.WoofTemplateExtension;
import net.officefloor.woof.model.woof.WoofTemplateExtensionProperty;
import net.officefloor.woof.template.WoofTemplateExtensionSource;

/**
 * {@link WoofTemplateExtension} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionImpl implements WoofTemplateExtension {

	/**
	 * {@link WoofTemplateExtensionSource} class name.
	 */
	private final String sourceClassName;

	/**
	 * {@link WoofTemplateExtensionProperty} instances.
	 */
	private final WoofTemplateExtensionProperty[] properties;

	/**
	 * Initiate.
	 * 
	 * @param sourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 * @param properties
	 *            {@link WoofTemplateExtensionProperty} instances.
	 */
	public WoofTemplateExtensionImpl(String sourceClassName,
			WoofTemplateExtensionProperty... properties) {
		this.sourceClassName = sourceClassName;
		this.properties = properties;
	}

	/*
	 * ====================== WoofTemplateExtension ============================
	 */

	@Override
	public String getWoofTemplateExtensionSourceClassName() {
		return this.sourceClassName;
	}

	@Override
	public WoofTemplateExtensionProperty[] getWoofTemplateExtensionProperties() {
		return this.properties;
	}

}