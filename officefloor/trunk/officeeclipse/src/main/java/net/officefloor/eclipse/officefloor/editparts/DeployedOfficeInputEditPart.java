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
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigureContext;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeInputModel.DeployedOfficeInputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DeployedOfficeInputModel}.
 * 
 * @author Daniel
 */
// TODO rename to DeployedOfficeInputEditPart
public class DeployedOfficeInputEditPart
		extends
		AbstractOfficeFloorNodeEditPart<DeployedOfficeInputModel, OfficeFloorFigure>
		implements DeployedOfficeInputFigureContext {

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<DeployedOfficeInputEvent>(
				DeployedOfficeInputEvent.values()) {
			@Override
			protected void handlePropertyChange(
					DeployedOfficeInputEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
				case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
					DeployedOfficeInputEditPart.this.refreshTargetConnections();
					break;
				}
			}
		});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createDeployedOfficeInputFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Never a source
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceFlows());
	}

	/*
	 * ====================== OfficeTaskFigureContext ====================
	 */

	// TODO rename to getSectionInputName
	@Override
	public String getWorkName() {
		return this.getCastedModel().getSectionInputName();
	}

	// TODO remove as only require section input name
	@Override
	public String getTaskName() {
		// return this.getCastedModel().getTaskName();
		return null;
	}

}
