package net.officefloor.gef.editor;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Creates the visual {@link Node} for the {@link ModelAction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedActionVisualFactory {

	/**
	 * Creates the visual {@link Node}.
	 * 
	 * @param context
	 *            {@link AdaptedActionVisualFactoryContext}.
	 * @return {@link Pane}.
	 */
	Node createVisual(AdaptedActionVisualFactoryContext context);

}
