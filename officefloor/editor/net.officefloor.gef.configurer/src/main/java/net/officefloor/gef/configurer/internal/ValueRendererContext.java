package net.officefloor.gef.configurer.internal;

import javafx.beans.property.Property;
import net.officefloor.gef.configurer.Actioner;
import net.officefloor.gef.configurer.Builder;
import net.officefloor.gef.configurer.ErrorListener;

/**
 * Context for the {@link ValueRenderer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRendererContext<M> {

	/**
	 * Obtains the model.
	 * 
	 * @return Model.
	 */
	M getModel();

	/**
	 * Triggers reloading the value for the provided {@link Builder}.
	 * 
	 * @param builder
	 *            {@link Builder} to identify the value to reload from the model.
	 */
	void reload(Builder<?, ?, ?> builder);

	/**
	 * Triggered to refresh the error display.
	 */
	void refreshError();

	/**
	 * Obtains the {@link Actioner}.
	 * 
	 * @return {@link Actioner} or <code>null</code> if not able to apply
	 *         configuration.
	 */
	Actioner getOptionalActioner();

	/**
	 * Obtains the dirty {@link Property}.
	 * 
	 * @return Dirty {@link Property}.
	 */
	Property<Boolean> dirtyProperty();

	/**
	 * Obtains the valid {@link Property}.
	 * 
	 * @return Valid {@link Property}.
	 */
	Property<Boolean> validProperty();

	/**
	 * Obtains the {@link ErrorListener}.
	 * 
	 * @return {@link ErrorListener}.
	 */
	ErrorListener getErrorListener();

}