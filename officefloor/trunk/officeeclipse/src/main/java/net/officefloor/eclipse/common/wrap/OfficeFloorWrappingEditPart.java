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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.ConnectionModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;

/**
 * Abstract {@link net.officefloor.eclipse.common.wrap.WrappingEditPart}.
 * 
 * @author Daniel
 */
public abstract class OfficeFloorWrappingEditPart extends
		AbstractOfficeFloorEditPart<WrappingModel<?>, OfficeFloorFigure>
		implements WrappingEditPart {

	/**
	 * Allow default construction.
	 */
	public OfficeFloorWrappingEditPart() {
	}

	/**
	 * Convenience constructor to initialise with the {@link Figure}.
	 * 
	 * @param figure
	 *            {@link Figure} for this {@link org.eclipse.gef.EditPart}.
	 */
	public OfficeFloorWrappingEditPart(Figure figure) {
		this.figure = figure;
	}

	/**
	 * {@link Figure}.
	 */
	private Figure figure;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return new OfficeFloorFigure() {

			@Override
			public IFigure getContentPane() {
				return OfficeFloorWrappingEditPart.this.figure;
			}

			@Override
			public IFigure getFigure() {
				return OfficeFloorWrappingEditPart.this.figure;
			}

			@Override
			public ConnectionAnchor getSourceConnectionAnchor(
					Class<? extends ConnectionModel> connectionModelType) {
				return null;
			}

			@Override
			public ConnectionAnchor getTargetConnectionAnchor(
					Class<? extends ConnectionModel> connectionModelType) {
				return null;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.wrap.WrappingEditPart#setFigure(org.eclipse
	 * .draw2d.Figure)
	 */
	public void setFigure(Figure figure) {
		this.figure = figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		// Defaultly no property change handlers
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List<?> getModelChildren() {
		List<Object> modelChildren = new LinkedList<Object>();
		this.populateModelChildren(modelChildren);
		return modelChildren;
	}

	/**
	 * Override to populate the model children.
	 * 
	 * @param children
	 *            Model children.
	 */
	protected void populateModelChildren(List<Object> children) {
		// Defaultly no children
	}

}
