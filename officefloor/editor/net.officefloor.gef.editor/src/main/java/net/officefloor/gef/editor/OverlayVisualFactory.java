package net.officefloor.gef.editor;

import javafx.scene.Node;

/**
 * Factory for an overlay visual {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OverlayVisualFactory {

	/**
	 * Loads the overlay.
	 * 
	 * @param context
	 *            {@link OverlayVisualContext}.
	 */
	void loadOverlay(OverlayVisualContext context);

}