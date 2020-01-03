package net.officefloor.web.resource.spi;

/**
 * Factory to create the {@link ResourceTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceTransformerFactory {

	/**
	 * Obtains the name of transformation.
	 * 
	 * @return Name of transformation.
	 */
	String getName();

	/**
	 * Creates the {@link ResourceTransformer}.
	 * 
	 * @return {@link ResourceTransformer}.
	 */
	ResourceTransformer createResourceTransformer();

}