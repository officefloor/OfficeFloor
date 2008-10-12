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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFigureContext;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel.OfficeFloorOfficeEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorOfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeEditPart
		extends
		AbstractOfficeFloorNodeEditPart<OfficeFloorOfficeModel, OfficeFloorFigure>
		implements RemovableEditPart, OfficeFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeFloorOfficeEvent>(
				OfficeFloorOfficeEvent.values()) {
			@Override
			protected void handlePropertyChange(
					OfficeFloorOfficeEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_MANAGED_OBJECT:
				case REMOVE_MANAGED_OBJECT:
				case ADD_TEAM:
				case REMOVE_TEAM:
				case ADD_TASK:
				case REMOVE_TASK:
					OfficeEditPart.this.refreshChildren();
					break;
				case ADD_RESPONSIBLE_MANAGED_OBJECT:
				case REMOVE_RESPONSIBLE_MANAGED_OBJECT:
					OfficeEditPart.this.refreshTargetConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTeams());
		childModels.addAll(this.getCastedModel().getManagedObjects());
		childModels.addAll(this.getCastedModel().getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Never a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getResponsibleManagedObjects());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#delete()
	 */
	@Override
	public void delete() {
		// Disconnect and remove Office
		RemoveConnectionsAction<OfficeFloorOfficeModel> office = this
				.getCastedModel().removeConnections();
		OfficeFloorModel officeFloor = (OfficeFloorModel) this.getParent()
				.getModel();
		officeFloor.removeOffice(office.getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.RemovableEditPart#undelete()
	 */
	@Override
	public void undelete() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeEditPart.undelete");
	}

	/*
	 * ======================= OfficeFigureContext ======================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.OfficeFigureContext#getOfficeName
	 * ()
	 */
	@Override
	public String getOfficeName() {
		return this.getCastedModel().getName();
	}

}
