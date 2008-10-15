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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.DeskModel.DeskEvent;

import org.eclipse.draw2d.geometry.Point;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.DeskModel}.
 * 
 * @author Daniel
 */
public class DeskEditPart extends AbstractOfficeFloorDiagramEditPart<DeskModel> {

	/**
	 * Obtains the unique {@link FlowItemModel} id for the input
	 * {@link FlowItemModel}.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @return Unique {@link FlowItemModel} id.
	 */
	public String getUniqueFlowItemId(FlowItemModel flowItem) {

		// Create the set of existing flow items
		Map<String, FlowItemModel> existingFlowItems = new HashMap<String, FlowItemModel>();
		for (FlowItemModel existingFlowItem : this.getCastedModel()
				.getFlowItems()) {
			existingFlowItems.put(existingFlowItem.getId(), existingFlowItem);
		}

		// Obtain the unique id
		String suffix = "";
		int index = 0;
		for (;;) {
			// Try for uniqueness
			String uniqueIdAttempt = flowItem.getTaskName() + suffix;

			// Determine if unique (not registered or same flow item)
			FlowItemModel existingFlowItem = existingFlowItems
					.get(uniqueIdAttempt);
			if ((existingFlowItem == null) || (existingFlowItem == flowItem)) {
				return uniqueIdAttempt;
			}

			// Setup for next attempt
			index++;
			suffix = String.valueOf(index);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #createLayoutEditPolicy()
	 */
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new DeskLayoutEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #populateChildren(java.util.List)
	 */
	protected void populateChildren(List<Object> childModels) {
		// Add the children
		childModels.addAll(this.getCastedModel().getWorks());
		childModels.addAll(this.getCastedModel().getExternalManagedObjects());
		childModels.addAll(this.getCastedModel().getFlowItems());
		childModels.addAll(this.getCastedModel().getExternalFlows());
		childModels.addAll(this.getCastedModel().getExternalEscalations());
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
		handlers.add(new PropertyChangeHandler<DeskEvent>(DeskEvent.values()) {
			protected void handlePropertyChange(DeskEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
					DeskEditPart.this.refresh();
					break;
				case ADD_WORK:
				case REMOVE_WORK:
					DeskEditPart.this.refresh();
					break;
				case ADD_EXTERNAL_FLOW:
				case REMOVE_EXTERNAL_FLOW:
					DeskEditPart.this.refresh();
					break;
				case ADD_EXTERNAL_ESCALATION:
				case REMOVE_EXTERNAL_ESCALATION:
					DeskEditPart.this.refresh();
					break;
				case ADD_FLOW_ITEM:
				case REMOVE_FLOW_ITEM:
					DeskEditPart.this.refresh();
					break;
				}
			}
		});
	}
}

/**
 * {@link org.eclipse.gef.editpolicies.LayoutEditPolicy} for the
 * {@link net.officefloor.model.desk.DeskModel}.
 */
class DeskLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<DeskModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy
	 * #createCreateComand(P, java.lang.Object,
	 * org.eclipse.draw2d.geometry.Point)
	 */
	protected CreateCommand<?, ?> createCreateComand(DeskModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}