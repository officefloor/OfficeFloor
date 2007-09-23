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
package net.officefloor.eclipse.common.editparts;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.model.Model;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

/**
 * Abstract {@link org.eclipse.gef.EditPart} for a diagram of the Office.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorDiagramEditPart<M extends Model>
		extends AbstractOfficeFloorEditPart<M> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		// Create the figure
		FreeformLayer figure = new FreeformLayer();
		figure.setLayoutManager(new FreeformLayout());

		// Return figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		// Install policies
		this.installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		this.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
		this.installEditPolicy(EditPolicy.LAYOUT_ROLE, this
				.createLayoutEditPolicy());

		// Initialise
		this.init();
	}

	/**
	 * Obtains the {@link LayoutEditPolicy} to be installed.
	 * 
	 * @return {@link LayoutEditPolicy} to be installed.
	 */
	protected abstract OfficeFloorLayoutEditPolicy createLayoutEditPolicy();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List getModelChildren() {
		// Create the list of model children
		List<Object> models = new LinkedList<Object>();

		// Populate the children
		this.populateChildren(models);

		// Return the children
		return models;
	}

	/**
	 * Populates the children of the diagram model.
	 * 
	 * @param childModels
	 *            List to be populated with the children models of the diagram
	 *            model.
	 */
	protected abstract void populateChildren(List<Object> childModels);

}
