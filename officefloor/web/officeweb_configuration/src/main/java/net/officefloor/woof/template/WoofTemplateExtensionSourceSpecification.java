package net.officefloor.woof.template;

/**
 * Specification of the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @return Property specification.
	 */
	WoofTemplateExtensionSourceProperty[] getProperties();

}