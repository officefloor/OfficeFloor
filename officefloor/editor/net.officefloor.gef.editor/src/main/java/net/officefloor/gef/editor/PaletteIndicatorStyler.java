package net.officefloor.gef.editor;

import javafx.beans.property.Property;
import javafx.scene.Node;

/**
 * Styler to the palette indicator.
 * 
 * @author Daniel Sagenschneider
 */
public interface PaletteIndicatorStyler {

	/**
	 * <p>
	 * Obtains the palette indicator.
	 * <p>
	 * This allows for interrogating the structure of the palette indicator.
	 * 
	 * @return Palette indicator.
	 */
	Node getPaletteIndicator();

	/**
	 * Obtains the {@link Property} to the palette indicator style.
	 * 
	 * @return {@link Property} to the palette indicator style.
	 */
	Property<String> paletteIndicatorStyle();

}