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

package net.officefloor.gef.editor.style;

import java.net.URL;

import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import javafx.beans.property.ReadOnlyProperty;
import net.officefloor.gef.editor.AdaptedChild;

/**
 * Registry of styles for {@link IVisualPart} instances of the
 * {@link AdaptedChild} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface StyleRegistry {

	/**
	 * Registers the style for the {@link AdaptedChild}.
	 * 
	 * @param configurationPath
	 *            Configuration path to the style.
	 * @param stylesheetContent
	 *            Style sheet content for the {@link IVisualPart} of the
	 *            configuration item.
	 * @return {@link ReadOnlyProperty} to the {@link URL} {@link String} of the
	 *         style.
	 */
	ReadOnlyProperty<URL> registerStyle(String configurationPath, ReadOnlyProperty<String> stylesheetContent);

}
