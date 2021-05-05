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

import javafx.scene.layout.Pane;

/**
 * Context for an overlay.
 * 
 * @author Daniel Sagenschneider
 */
public interface OverlayVisualContext {

	/**
	 * <p>
	 * Obtains the parent {@link Pane} that loads as the overlay.
	 * <p>
	 * Content for the overlay to be added to this {@link Pane}.
	 * 
	 * @return Parent {@link Pane} to load in the overlay.
	 */
	Pane getOverlayParent();

	/**
	 * <p>
	 * Indicates whether the overlay is fixed width.
	 * <p>
	 * By default the overlay resizes.
	 * 
	 * @param isFixedWith
	 *            <code>true</code> for fixed width.
	 */
	void setFixedWidth(boolean isFixedWith);

	/**
	 * <p>
	 * Indicates whether the overlay is fixed height.
	 * <p>
	 * By default the overlay resizes.
	 * 
	 * @param isFixedHeight
	 *            <code>true</code> for fixed height.
	 */
	void setFixedHeight(boolean isFixedHeight);

	/**
	 * Closes the overlay.
	 */
	void close();

}
