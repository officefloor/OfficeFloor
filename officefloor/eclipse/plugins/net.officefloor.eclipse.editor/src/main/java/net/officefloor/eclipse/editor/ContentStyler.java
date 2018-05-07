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

import org.eclipse.gef.mvc.fx.models.GridModel;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.paint.Paint;

/**
 * Enables styling the content.
 * 
 * @author Daniel Sagenschneider
 */
public interface ContentStyler {

	/**
	 * <p>
	 * Obtains the content.
	 * <p>
	 * This allows for interrogating the structure of the content.
	 * 
	 * @return Content.
	 */
	Node getContent();

	/**
	 * Specifies the content background.
	 * 
	 * @param background
	 *            {@link Paint} background. <code>null</code> to resent to default.
	 */
	void setContentBackground(Paint background);

	/**
	 * Obtains the {@link GridModel} to configure the content grid.
	 * 
	 * @return {@link GridModel} to configure the content grid.
	 */
	GridModel getGridModel();

	/**
	 * Obtains the {@link Property} to the style sheet rules for the content.
	 * 
	 * @return {@link Property} to specify the style sheet rules for the content.
	 */
	Property<String> contentStyle();
}