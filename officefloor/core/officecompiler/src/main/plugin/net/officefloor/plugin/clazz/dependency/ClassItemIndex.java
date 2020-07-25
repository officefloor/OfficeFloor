package net.officefloor.plugin.clazz.dependency;

/**
 * Index for {@link Class} item.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassItemIndex {

	/**
	 * Obtains the index of the item.
	 * 
	 * @return Index of the item.
	 */
	int getIndex();

	/**
	 * Allows adding further annotations to the item.
	 * 
	 * @param annotation Further annotation for the item.
	 */
	void addAnnotation(Object annotation);

}