package net.officefloor.woof.model.woof;

/**
 * Property for the {@link WoofTemplateExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionProperty {

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
	 */
	String getName();

	/**
	 * Obtains the value of the property.
	 * 
	 * @return Value of the property.
	 */
	String getValue();

}