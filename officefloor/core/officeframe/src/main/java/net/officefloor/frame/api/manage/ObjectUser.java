package net.officefloor.frame.api.manage;

/**
 * User of an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectUser<O> {

	/**
	 * Provides the object for using.
	 * 
	 * @param object  Object for use.
	 * @param failure {@link Throwable} if fails to load the {@link Object}.
	 */
	void use(O object, Throwable failure);
}