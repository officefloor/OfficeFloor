package net.officefloor.woof.template;

/**
 * Individual property of the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceProperty {

	/**
	 * Obtains name of property.
	 * 
	 * @return Name of property.
	 */
	String getName();

	/**
	 * Obtains the display name of the property. If this returns
	 * <code>null</code> then the return value of {@link #getName()} is used.
	 * 
	 * @return Display name of property.
	 */
	String getLabel();

}