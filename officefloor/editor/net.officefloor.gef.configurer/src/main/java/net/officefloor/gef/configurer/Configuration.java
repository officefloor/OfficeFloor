package net.officefloor.gef.configurer;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;

/**
 * Configured configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface Configuration {

	/**
	 * Obtains the {@link Node} containing the configuration.
	 * 
	 * @return {@link Node} containing the configuration.
	 */
	Node getConfigurationNode();

	/**
	 * Obtains the title.
	 * 
	 * @return Title.
	 */
	String getTitle();

	/**
	 * Indicates if configuration has been changed.
	 * 
	 * @return {@link Property} to indicate if the configuration has been changed.
	 */
	Property<Boolean> dirtyProperty();

	/**
	 * Indicates if the configuration is valid.
	 * 
	 * @return {@link ReadOnlyProperty} to indicate if the configuration is valid.
	 */
	Property<Boolean> validProperty();

	/**
	 * <p>
	 * Obtains the {@link Actioner} for the configuration
	 * <p>
	 * Should this be invoked, it is assumed the {@link Actioner} will be triggered
	 * externally to the configuration.
	 * 
	 * @return {@link Actioner} for the configuration.
	 */
	Actioner getActioner();

}