/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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

package net.officefloor.gef.ide.preferences;

import javafx.scene.layout.Pane;

/**
 * Interface for generic preference styler.
 * 
 * @author Daniel Sagenschneider
 */
public interface PreferenceStyler {

	/**
	 * Creates a visual for the editing the style.
	 * 
	 * @param close Removes the view.
	 * @return Visual for editing the style.
	 */
	Pane createVisual(Runnable close);
}
