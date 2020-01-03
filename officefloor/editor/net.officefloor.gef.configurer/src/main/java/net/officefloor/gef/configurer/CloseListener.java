package net.officefloor.gef.configurer;

/**
 * Listens on close of configuration either by apply or cancel.
 * 
 * @author Daniel Sagenschneider
 */
public interface CloseListener {

	/**
	 * Notified that completed applying the configuration.
	 */
	void applied();

	/**
	 * Notified that cancelled applying the configuration.
	 */
	void cancelled();

}