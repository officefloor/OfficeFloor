/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
