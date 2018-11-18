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

import org.eclipse.gef.geometry.planar.Dimension;

import javafx.scene.Node;
import net.officefloor.model.Model;

/**
 * Adapted area.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedArea<M extends Model> extends AdaptedModel<M> {

	/**
	 * Obtains the {@link Dimension}.
	 * 
	 * @return {@link Dimension}.
	 */
	Dimension getDimension();

	/**
	 * Specifies the {@link Dimension}.
	 * 
	 * @param dimension {@link Dimension}.
	 */
	void setDimension(Dimension dimension);

	/**
	 * Creates the visual {@link Node}.
	 * 
	 * @param context {@link AdaptedModelVisualFactoryContext}.
	 * @return Visual {@link Node}.
	 */
	Node createVisual(AdaptedModelVisualFactoryContext<M> context);

}