package net.officefloor.gef.configurer;

/**
 * Default images.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultImages {

	/**
	 * Error image path.
	 */
	public static final String ERROR_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/error.png";

	/**
	 * Delete image path.
	 */
	public static final String DELETE_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/delete.png";

	/**
	 * Add image path.
	 */
	public static final String ADD_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/add.png";

	/**
	 * All access via static methods.
	 */
	private DefaultImages() {
	}
}