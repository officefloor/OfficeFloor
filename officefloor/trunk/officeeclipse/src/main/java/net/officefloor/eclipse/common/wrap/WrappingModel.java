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
package net.officefloor.eclipse.common.wrap;

import net.officefloor.eclipse.common.ModelEditPart;
import net.officefloor.model.AbstractModel;

import org.eclipse.draw2d.geometry.Point;

/**
 * Model to wrap a particular model.
 * 
 * @param M
 *            Type of model being wrapped.
 * 
 * @author Daniel
 */
public class WrappingModel<M extends AbstractModel> extends AbstractModel
		implements ModelEditPart {

	/**
	 * Model being wrapped.
	 */
	private final M model;

	/**
	 * {@link WrappingEditPart} for the model.
	 */
	private final WrappingEditPart editPart;

	/**
	 * Initiate.
	 * 
	 * @param model
	 *            Model being wrapped.
	 * @param editPart
	 *            {@link WrappingEditPart} for the model.
	 * @param initialLocation
	 *            Initial location to put the {@link org.eclipse.draw2d.Figure}.
	 */
	public WrappingModel(M model, WrappingEditPart editPart,
			Point initialLocation) {
		this.model = model;
		this.editPart = editPart;
		this.setX(initialLocation.x);
		this.setY(initialLocation.y);
	}

	/**
	 * Initiate.
	 * 
	 * @param model
	 *            Model being wrapped.
	 * @param editPart
	 *            {@link WrappingEditPart} for the model.
	 */
	public WrappingModel(M model, WrappingEditPart editPart) {
		this.model = model;
		this.editPart = editPart;
	}

	/**
	 * Obtains the model being wrapped.
	 * 
	 * @return Model being wrapped.
	 */
	public M getWrappedModel() {
		return this.model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.ModelEditPart#getEditPart()
	 */
	public WrappingEditPart getEditPart() {
		return this.editPart;
	}

}
