/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskObjectModel.WorkTaskObjectEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WorkTaskObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkTaskObjectEditPart
		extends
		AbstractOfficeFloorEditPart<WorkTaskObjectModel, WorkTaskObjectEvent, WorkTaskObjectFigure>
		implements WorkTaskObjectFigureContext {

	@Override
	protected WorkTaskObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createWorkTaskObjectFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getExternalManagedObject());
	}

	@Override
	protected Class<WorkTaskObjectEvent> getPropertyChangeEventType() {
		return WorkTaskObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(WorkTaskObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_IS_PARAMETER:
			// Indicate is parameter changed
			WorkTaskObjectEditPart.this.getOfficeFloorFigure().setIsParameter(
					WorkTaskObjectEditPart.this.getCastedModel()
							.getIsParameter());
			break;
		case CHANGE_EXTERNAL_MANAGED_OBJECT:
			WorkTaskObjectEditPart.this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * =================== DeskTaskObjectFigureContext ========================
	 */

	@Override
	public String getObjectType() {
		return this.getCastedModel().getObjectType();
	}

	@Override
	public boolean isParameter() {
		return this.getCastedModel().getIsParameter();
	}

	@Override
	public void setIsParameter(final boolean isParameter) {

		// Obtain the current state
		final boolean currentIsParameter = this.getCastedModel()
				.getIsParameter();

		// Specify the change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Set new value
				WorkTaskObjectEditPart.this.getCastedModel().setIsParameter(
						isParameter);
			}

			@Override
			protected void undoCommand() {
				// Reset to old value
				WorkTaskObjectEditPart.this.getCastedModel().setIsParameter(
						currentIsParameter);
			}
		});
	}

}