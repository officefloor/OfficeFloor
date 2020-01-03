package net.officefloor.gef.configurer;

/**
 * Triggers applying the configured model.
 * 
 * @author Daniel Sagenschneider
 */
public interface Actioner {

	/**
	 * Obtains the label for the action.
	 * 
	 * @return Label for the action.
	 */
	String getLabel();

	/**
	 * Applies the configured model.
	 */
	void action();

}