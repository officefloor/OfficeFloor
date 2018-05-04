/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor;

import javafx.beans.property.Property;
import net.officefloor.eclipse.editor.internal.models.AdaptedAction;
import net.officefloor.model.Model;

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
	 * @param style
	 *            {@link Property} to enable configuring the style of the palette
	 *            indicator.
	 */
	void paletteIndicator(Property<String> style);

	/**
	 * Notified that the palette was selected.
	 * 
	 * @param style
	 *            {@link Property} to enable configuring the style of the palette.
	 */
	void palette(Property<String> style);

	/**
	 * Notified that the content was selected.
	 * 
	 * @param contentStyler
	 *            {@link ContentStyler}.
	 */
	void content(ContentStyler contentStyler);

	/**
	 * Model has been selected.
	 * 
	 * @param model
	 *            Model is selected.
	 */
	void model(Model model);

}