/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.skin;

import net.officefloor.model.ConnectionModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * Provides details of the {@link IFigure} for an {@link EditPart}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorFigure {

	/**
	 * Obtains the {@link IFigure} to represent the {@link EditPart}.
	 *
	 * @return {@link IFigure} to represent the {@link EditPart}.
	 */
	IFigure getFigure();

	/**
	 * Obtains the {@link IFigure} to add children of this {@link EditPart}.
	 *
	 * @return {@link IFigure} to add children of this {@link EditPart}.
	 */
	IFigure getContentPane();

	/**
	 * Obtains the source {@link ConnectionAnchor} for the particular
	 * {@link ConnectionModel} type.
	 *
	 * @param connectionModelType
	 *            {@link ConnectionModel} type.
	 * @return {@link ConnectionAnchor}.
	 */
	ConnectionAnchor getSourceConnectionAnchor(Class<?> connectionModelType);

	/**
	 * Obtains the target {@link ConnectionAnchor} for the particular
	 * {@link ConnectionModel} type.
	 *
	 * @param connectionModelType
	 *            {@link ConnectionModel} type.
	 * @return {@link ConnectionAnchor}.
	 */
	ConnectionAnchor getTargetConnectionAnchor(Class<?> connectionModelType);

}