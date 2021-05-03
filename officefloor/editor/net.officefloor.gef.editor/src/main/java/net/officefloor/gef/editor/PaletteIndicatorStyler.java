/*-
 * #%L
 * [bundle] OfficeFloor Editor
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
