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
