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

import java.net.URL;

import org.eclipse.gef.fx.nodes.HoverOverlayImageView;

import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 * Default images.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultImages {

	/**
	 * Add image name.
	 */
	public static final String ADD_IMAGE = "add.png";

	/**
	 * Add image hover name.
	 */
	public static final String ADD_IMAGE_HOVER = "add_hover.png";

	/**
	 * {@link AdaptedActionVisualFactory} for deafult add image.
	 */
	public static final AdaptedActionVisualFactory ADD = (context) -> context.createImageWithHover(DefaultImages.class,
			ADD_IMAGE, ADD_IMAGE_HOVER);

	/**
	 * Edit image name.
	 */
	public static final String EDIT_IMAGE = "edit.png";

	/**
	 * Edit image hover name.
	 */
	public static final String EDIT_IMAGE_HOVER = "edit_hover.png";

	/**
	 * {@link AdaptedActionVisualFactory} for default edit image.
	 */
	public static final AdaptedActionVisualFactory EDIT = (context) -> context.createImageWithHover(DefaultImages.class,
			EDIT_IMAGE, EDIT_IMAGE_HOVER);

	/**
	 * Delete image name.
	 */
	public static final String DELETE_IMAGE = "delete.png";

	/**
	 * Delete image hover name.
	 */
	public static final String DELETE_IMAGE_HOVER = "delete_hover.png";

	/**
	 * {@link AdaptedActionVisualFactory} for default delete image.
	 */
	public static final AdaptedActionVisualFactory DELETE = (context) -> context
			.createImageWithHover(DefaultImages.class, DELETE_IMAGE, DELETE_IMAGE_HOVER);

	/**
	 * Creates image with hover.
	 * 
	 * @param resourceClass
	 *            Resource {@link Class} to find image files.
	 * @param imageFilePath
	 *            Path to image.
	 * @param hoverImageFilePath
	 *            Path to hover image.
	 * @return {@link Node} for image with hover.
	 */
	public static Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath) {

		// Obtain the overlay image
		URL overlayImageResource = resourceClass.getResource(hoverImageFilePath);
		if (overlayImageResource == null) {
			throw new IllegalStateException(
					"Cannot find resource " + hoverImageFilePath + " from resource class " + resourceClass.getName());
		}
		Image overlayImage = new Image(overlayImageResource.toExternalForm());

		// Obtain the base image
		URL baseImageResource = resourceClass.getResource(imageFilePath);
		if (baseImageResource == null) {
			throw new IllegalStateException(
					"Cannot find resource " + imageFilePath + " from resource class " + resourceClass.getName());
		}
		Image baseImage = new Image(baseImageResource.toExternalForm());

		// Create the hover overlay image
		HoverOverlayImageView blendImageView = new HoverOverlayImageView();
		blendImageView.baseImageProperty().set(baseImage);
		blendImageView.overlayImageProperty().set(overlayImage);
		return blendImageView;
	}

	/**
	 * All access via static methods.
	 */
	private DefaultImages() {
	}

}
