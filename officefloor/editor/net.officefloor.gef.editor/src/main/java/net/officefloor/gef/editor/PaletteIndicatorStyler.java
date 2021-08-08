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
