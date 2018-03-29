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
package net.officefloor.eclipse.configurer.internal.text;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;

/**
 * {@link TextBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TextBuilderImpl<M> extends AbstractBuilder<M, String, TextBuilder<M>> implements TextBuilder<M> {

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public TextBuilderImpl(String label) {
		super(label);
	}

	/*
	 * ============= AbstractBuilder ==============
	 */

	@Override
	protected Node createInput(Property<String> value) {
		TextField text = new TextField(value.getValue());
		text.textProperty().bindBidirectional(value);
		text.getStyleClass().add("configurer-input-text");
		return text;
	}

}