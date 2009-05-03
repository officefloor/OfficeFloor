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
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.office.OfficeEditor;
import net.officefloor.eclipse.officefloor.operations.RemoveOfficeOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFigureContext;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeModel.DeployedOfficeEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

/**
 * {@link EditPart} for the {@link OfficeFloorOfficeModel}.
 * 
 * @author Daniel
 */
// TODO rename to DeployedOfficeEditPart
public class OfficeEditPart extends
		AbstractOfficeFloorNodeEditPart<DeployedOfficeModel, OfficeFloorFigure>
		implements RemovableEditPart, OfficeFigureContext {

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<DeployedOfficeEvent>(
				DeployedOfficeEvent.values()) {
			@Override
			protected void handlePropertyChange(DeployedOfficeEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_DEPLOYED_OFFICE_OBJECT:
				case REMOVE_DEPLOYED_OFFICE_OBJECT:
				case ADD_DEPLOYED_OFFICE_TEAM:
				case REMOVE_DEPLOYED_OFFICE_TEAM:
				case ADD_DEPLOYED_OFFICE_INPUT:
				case REMOVE_DEPLOYED_OFFICE_INPUT:
					OfficeEditPart.this.refreshChildren();
					break;
				// case ADD_RESPONSIBLE_MANAGED_OBJECT:
				// case REMOVE_RESPONSIBLE_MANAGED_OBJECT:
				// OfficeEditPart.this.refreshTargetConnections();
				// break;
				}
			}
		});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFigure(this);
	}

	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getDeployedOfficeTeams());
		childModels.addAll(this.getCastedModel().getDeployedOfficeObjects());
		childModels.addAll(this.getCastedModel().getDeployedOfficeInputs());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Never a source
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// models.addAll(this.getCastedModel().getResponsibleManagedObjects());
	}

	@Override
	public Operation getRemoveOperation() {
		return new RemoveOfficeOperation();
	}

	@Override
	protected Command handleDoubleClick(Request request) {
		// Open the office
		this.openClasspathFile(this.getCastedModel().getDeployedOfficeName(),
				OfficeEditor.EDITOR_ID);
		return null;
	}

	/*
	 * ======================= OfficeFigureContext ======================
	 */

	@Override
	public String getOfficeName() {
		return this.getCastedModel().getDeployedOfficeName();
	}

}