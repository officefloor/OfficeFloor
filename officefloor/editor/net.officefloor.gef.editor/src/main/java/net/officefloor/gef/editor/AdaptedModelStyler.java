package net.officefloor.gef.editor;

import javafx.beans.property.Property;
import net.officefloor.model.Model;

/**
 * Styler of the {@link AdaptedModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedModelStyler {

	/**
	 * Obtains the {@link Model}.
	 * 
	 * @return {@link Model}.
	 */
	Model getModel();

	/**
	 * Obtains the {@link Property} to style the {@link AdaptedModel}.
	 * 
	 * @return {@link Property} to style the {@link AdaptedModel}.
	 */
	Property<String> style();

}