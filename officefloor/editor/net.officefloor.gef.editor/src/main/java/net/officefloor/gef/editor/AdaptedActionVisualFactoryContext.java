package net.officefloor.gef.editor;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * Context for the {@link AdaptedActionVisualFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedActionVisualFactoryContext {

	/**
	 * <p>
	 * Add the {@link Node} to the parent {@link Pane} returning it.
	 * <p>
	 * This allows for convenient adding new {@link Node} instances to {@link Pane}.
	 * 
	 * @param <N>
	 *            {@link Node} type.
	 * @param parent
	 *            Parent {@link Pane}.
	 * @param node
	 *            {@link Node}.
	 * @return Input {@link Node}
	 */
	<N extends Node> N addNode(Pane parent, N node);

	/**
	 * Convenience method to create a {@link Node} with {@link Image} and hover
	 * {@link Image}. Typically this is to create button for the action.
	 * 
	 * @param resourceClass
	 *            {@link Class} within the class path containing the images.
	 * @param imageFilePath
	 *            Path to the {@link Image}.
	 * @param hoverImageFilePath
	 *            Path to the hover {@link Image}.
	 * @return {@link Node} for the {@link Image} with hover.
	 * 
	 * @see DefaultImages
	 */
	Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath);

}