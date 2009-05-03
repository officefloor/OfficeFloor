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
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.desk.operations.RemoveExternalManagedObjectOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.ExternalManagedObjectModel.ExternalManagedObjectEvent;

/**
 * {@link EditPart} for the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class ExternalManagedObjectEditPart
		extends
		AbstractOfficeFloorNodeEditPart<ExternalManagedObjectModel, OfficeFloorFigure>
		implements RemovableEditPart, ExternalManagedObjectFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createExternalManagedObjectFigure(this);
	}

	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Not a source
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getTaskObjects());
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<ExternalManagedObjectEvent>(
				ExternalManagedObjectEvent.values()) {
			protected void handlePropertyChange(
					ExternalManagedObjectEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_TASK_OBJECT:
				case REMOVE_TASK_OBJECT:
					ExternalManagedObjectEditPart.this
							.refreshTargetConnections();
					break;
				}
			}
		});
	}

	@Override
	public Operation getRemoveOperation() {
		return new RemoveExternalManagedObjectOperation();
	}

	/*
	 * ============= ExternalManagedObjectFigureContext ===================
	 */

	@Override
	public String getExternalManagedObjectName() {
		return this.getCastedModel().getExternalManagedObjectName();
	}

}
