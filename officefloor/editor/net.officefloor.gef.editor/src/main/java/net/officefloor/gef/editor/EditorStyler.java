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

import org.eclipse.gef.mvc.fx.models.GridModel;

import javafx.beans.property.Property;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Enables styling the content.
 * 
 * @author Daniel Sagenschneider
 */
public interface EditorStyler {

	/**
	 * <p>
	 * Obtains the root {@link Parent} for the editor.
	 * <p>
	 * This allows for interrogating the structure of the editor.
	 * 
	 * @return Editor {@link Parent}.
	 */
	Parent getEditor();

	/**
	 * Obtains the {@link GridModel} to configure the content grid.
	 * 
	 * @return {@link GridModel} to configure the content grid.
	 */
	GridModel getGridModel();

	/**
	 * Obtains the {@link Property} to the style sheet rules for the {@link Scene}.
	 * 
	 * @return {@link Property} to specify the style sheet rules for the
	 *         {@link Scene}.
	 */
	Property<String> editorStyle();
}
