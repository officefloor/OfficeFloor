/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor;

/**
 * Default images.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultImages {

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
	 * All access via static methods.
	 */
	private DefaultImages() {
	}
}