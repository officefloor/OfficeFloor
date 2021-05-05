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

import net.officefloor.gef.editor.internal.models.AdaptedAction;

/**
 * <p>
 * Configured into the {@link AdaptedEditorModule} before configuring to
 * indicate that select only behaviour.
 * <p>
 * This disables all creation of {@link AdaptedParent} instances and action of
 * {@link AdaptedAction} instances.
 * <p>
 * Targeted use of this is to enable styling of the editor by selecting various
 * aspects of the editor.
 * 
 * @author Daniel Sagenschneider
 */
public interface SelectOnly {

	/**
	 * Notified that the palette indicator was selected.
	 * 
	 * @param paletteIndiatorStyler
	 *            {@link PaletteIndicatorStyler}.
	 */
	void paletteIndicator(PaletteIndicatorStyler paletteIndiatorStyler);

	/**
	 * Notified that the palette was selected.
	 * 
	 * @param paletteStyler
	 *            {@link PaletteStyler}.
	 */
	void palette(PaletteStyler paletteStyler);

	/**
	 * Notified that the editor was selected.
	 * 
	 * @param editorStyler
	 *            {@link EditorStyler}.
	 */
	void editor(EditorStyler editorStyler);

	/**
	 * Model has been selected.
	 * 
	 * @param modelStyler
	 *            {@link AdaptedModelStyler}.
	 */
	void model(AdaptedModelStyler modelStyler);

}
