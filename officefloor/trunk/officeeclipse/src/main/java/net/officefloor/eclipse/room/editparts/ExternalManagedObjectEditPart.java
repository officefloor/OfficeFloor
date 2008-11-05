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
package net.officefloor.eclipse.room.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.room.operations.RemoveExternalManagedObjectOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.room.ExternalManagedObjectFigureContext;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.ExternalManagedObjectModel.ExternalManagedObjectEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.eclipse.desk.editparts.ExternalManagedObjectEditPart}.
 * 
 * @author Daniel
 */
public class ExternalManagedObjectEditPart
		extends
		AbstractOfficeFloorNodeEditPart<ExternalManagedObjectModel, OfficeFloorFigure>
		implements RemovableEditPart, ExternalManagedObjectFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionSourceModels(java.util.List)
	 */
	protected void populateConnectionSourceModels(List<Object> models) {
		// Not a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionTargetModels(java.util.List)
	 */
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getSubRoomManagedObjects());
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
		handlers.add(new PropertyChangeHandler<ExternalManagedObjectEvent>(
				ExternalManagedObjectEvent.values()) {
			protected void handlePropertyChange(
					ExternalManagedObjectEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_SUB_ROOM_MANAGED_OBJECT:
				case REMOVE_SUB_ROOM_MANAGED_OBJECT:
					ExternalManagedObjectEditPart.this
							.refreshTargetConnections();
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
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createExternalManagedObjectFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * isFreeformFigure()
	 */
	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.RemovableEditPart#getRemoveOperation
	 * ()
	 */
	@Override
	public Operation getRemoveOperation() {
		return new RemoveExternalManagedObjectOperation();
	}

	/*
	 * =================== ExternalManagedObjectFigureContext =================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.ExternalManagedObjectFigureContext#
	 * getExternalManagedObjectName()
	 */
	@Override
	public String getExternalManagedObjectName() {
		return this.getCastedModel().getName();
	}

}
