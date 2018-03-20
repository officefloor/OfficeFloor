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

import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * Default images.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultImages {

	/**
	 * Delete image name.
	 */
	public static final String DELETE_IMAGE = "/delete.gif";

	/**
	 * Delete image hover name.
	 */
	public static final String DELETE_IMAGE_HOVER = "/delete_hover.gif";

	/**
	 * {@link AdaptedActionVisualFactory} for default delete image.
	 */
	public static final AdaptedActionVisualFactory DELETE = (context) -> context
			.createImageWithHover(OfficeFloorModel.class, DELETE_IMAGE, DELETE_IMAGE_HOVER);

	/**
	 * All access via static methods.
	 */
	private DefaultImages() {
	}
}