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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.office.operations.RemoveExternalTeamOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.ExternalTeamFigureContext;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.ExternalTeamModel.ExternalTeamEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExternalTeamModel}.
 * 
 * @author Daniel
 */
public class ExternalTeamEditPart extends
		AbstractOfficeFloorNodeEditPart<ExternalTeamModel, OfficeFloorFigure>
		implements RemovableEditPart, ExternalTeamFigureContext {

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
		models.addAll(this.getCastedModel().getFlowItems());
		models.addAll(this.getCastedModel().getAdministrators());
	}

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
		handlers.add(new PropertyChangeHandler<ExternalTeamEvent>(
				ExternalTeamEvent.values()) {
			@Override
			protected void handlePropertyChange(ExternalTeamEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_FLOW_ITEM:
				case REMOVE_FLOW_ITEM:
					ExternalTeamEditPart.this.refreshTargetConnections();
					break;
				case ADD_ADMINISTRATOR:
				case REMOVE_ADMINISTRATOR:
					ExternalTeamEditPart.this.refreshTargetConnections();
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
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createExternalTeamFigure(this);
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

	/* (non-Javadoc)
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#getRemoveOperation()
	 */
	@Override
	public Operation getRemoveOperation() {
		return new RemoveExternalTeamOperation();
	}

	/*
	 * =================== ExternalTeamFigureContext ========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.ExternalTeamFigureContext#getTeamName
	 * ()
	 */
	@Override
	public String getTeamName() {
		return this.getCastedModel().getName();
	}

}
