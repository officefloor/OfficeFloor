package net.officefloor.gef.editor;

import javafx.beans.property.Property;
import javafx.scene.Node;

/**
 * Styler of the palette.
 * 
 * @author Daniel Sagenschneider
 */
public interface PaletteStyler {

	/**
	 * <p>
	 * Obtains the palette.
	 * <p>
	 * This allows for interrogating the structure of the palette.
	 * 
	 * @return Palette.
	 */
	Node getPalette();

	/**
	 * Obtains the {@link Property} to style of the palette.
	 * 
	 * @return {@link Property} to style of the palette.
	 */
	Property<String> paletteStyle();

}