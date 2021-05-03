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
