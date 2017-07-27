/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectFigure;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectModel.ManagedFunctionObjectEvent;

/**
 * {@link EditPart} for the {@link ManagedFunctionObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionObjectEditPart extends
		AbstractOfficeFloorEditPart<ManagedFunctionObjectModel, ManagedFunctionObjectEvent, ManagedFunctionObjectFigure>
		implements ManagedFunctionObjectFigureContext {

	@Override
	protected ManagedFunctionObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory().createManagedFunctionObjectFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getExternalManagedObject());
		EclipseUtil.addToList(models, this.getCastedModel().getSectionManagedObject());
	}

	@Override
	protected Class<ManagedFunctionObjectEvent> getPropertyChangeEventType() {
		return ManagedFunctionObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(ManagedFunctionObjectEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_IS_PARAMETER:
			// Indicate is parameter changed
			ManagedFunctionObjectEditPart.this.getOfficeFloorFigure()
					.setIsParameter(ManagedFunctionObjectEditPart.this.getCastedModel().getIsParameter());
			break;

		case CHANGE_OBJECT_NAME:
		case CHANGE_OBJECT_TYPE:
			this.getOfficeFloorFigure().setWorkTaskObjectName(this);
			break;

		case CHANGE_EXTERNAL_MANAGED_OBJECT:
		case CHANGE_SECTION_MANAGED_OBJECT:
			ManagedFunctionObjectEditPart.this.refreshSourceConnections();
			break;

		case CHANGE_KEY:
			// Non visual change
			break;
		}
	}

	/*
	 * ================= ManagedFunctionObjectFigureContext =================
	 */

	@Override
	public String getManagedFunctionObjectName() {
		return this.getCastedModel().getObjectName();
	}

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
		final boolean currentIsParameter = this.getCastedModel().getIsParameter();

		// Specify the change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Set new value
				ManagedFunctionObjectEditPart.this.getCastedModel().setIsParameter(isParameter);
			}

			@Override
			protected void undoCommand() {
				// Reset to old value
				ManagedFunctionObjectEditPart.this.getCastedModel().setIsParameter(currentIsParameter);
			}
		});
	}

}