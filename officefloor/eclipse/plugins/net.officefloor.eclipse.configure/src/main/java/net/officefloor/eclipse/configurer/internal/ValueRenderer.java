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
package net.officefloor.eclipse.configurer.internal;

import javafx.scene.Node;

/**
 * <p>
 * Renders the values.
 * <p>
 * Implementations must provide new instances of the {@link Node}, as there may
 * be different layouts requiring multiple {@link Node} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRenderer<M> {

	/**
	 * Initialises the {@link ValueRenderer}.
	 */
	void init(ValueRendererContext<M> context);

	/**
	 * Creates a new label {@link Node}.
	 * 
	 * @return New label {@link Node}.
	 */
	Node createLabel();

	/**
	 * Creates a new input {@link Node}. {@link Node} responsible for capturing the
	 * configuration via the UI.
	 * 
	 * @return New input {@link Node}.
	 */
	Node createInput();

	/**
	 * Loads the value to the model.
	 * 
	 * @param model
	 *            Model to have value loaded onto it.
	 */
	void loadValue(M model);

}