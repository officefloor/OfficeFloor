/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin;

import net.officefloor.model.ConnectionModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * Provides details of the {@link IFigure} for an {@link EditPart}.
 * 
 * @author Daniel
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
	ConnectionAnchor getSourceConnectionAnchor(
			Class<? extends ConnectionModel> connectionModelType);

	/**
	 * Obtains the target {@link ConnectionAnchor} for the particular
	 * {@link ConnectionModel} type.
	 * 
	 * @param connectionModelType
	 *            {@link ConnectionModel} type.
	 * @return {@link ConnectionAnchor}.
	 */
	ConnectionAnchor getTargetConnectionAnchor(
			Class<? extends ConnectionModel> connectionModelType);

}
