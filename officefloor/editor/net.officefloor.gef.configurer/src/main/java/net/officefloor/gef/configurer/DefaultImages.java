/*-
 * #%L
 * [bundle] OfficeFloor Configurer
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.gef.configurer;

/**
 * Default images.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultImages {

	/**
	 * Error image path.
	 */
	public static final String ERROR_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/error.png";

	/**
	 * Delete image path.
	 */
	public static final String DELETE_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/delete.png";

	/**
	 * Add image path.
	 */
	public static final String ADD_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/add.png";

	/**
	 * All access via static methods.
	 */
	private DefaultImages() {
	}
}
