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

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.model.Model;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

/**
 * Abstract {@link EditPart} for a diagram.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorDiagramEditPart<M extends Model>
		extends AbstractOfficeFloorEditPart<M, Indexed, OfficeFloorFigure> {

	/*
	 * ================= AbstractOfficeFloorEditPart ========================
	 */

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		// Create the figure
		final FreeformLayer figure = new FreeformLayer();
		figure.setLayoutManager(new FreeformLayout());

		// Return the figure
		return new OfficeFloorFigure() {

			@Override
			public IFigure getContentPane() {
				return figure;
			}

			@Override
			public IFigure getFigure() {
				return figure;
			}

			@Override
			public ConnectionAnchor getSourceConnectionAnchor(
					Class<?> connectionModelType) {
				return null;
			}

			@Override
			public ConnectionAnchor getTargetConnectionAnchor(
					Class<?> connectionModelType) {
				return null;
			}
		};
	}

	@Override
	protected void createEditPolicies() {
		// Install policies
		this.installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		this.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);

		// Install the layout edit policy
		LayoutEditPolicy layoutEditPolicy = this.getEditor()
				.createLayoutEditPolicy();
		this.installEditPolicy(EditPolicy.LAYOUT_ROLE, layoutEditPolicy);

		// Initialise
		this.init();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Diagram to refresh children on any event
		this.refreshChildren();
	}

	@Override
	protected Class<Indexed> getPropertyChangeEventType() {
		this.messageError("Diagram should always refresh children");
		return null;
	}

	@Override
	protected void handlePropertyChange(Indexed property,
			PropertyChangeEvent evt) {
		this.messageError("Diagram should always refresh children");
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		this.populateChildren(childModels);
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