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
package net.officefloor.eclipse.woof.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.action.OperationUtil;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.woof.AccessFigureContext;
import net.officefloor.eclipse.woof.operations.RefactorAccessOperation;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofAccessModel.WoofAccessEvent;
import net.officefloor.model.woof.WoofChanges;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofAccessModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofAccessEditPart
		extends
		AbstractOfficeFloorEditPart<WoofAccessModel, WoofAccessEvent, OfficeFloorFigure>
		implements AccessFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createAccessFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getInputs());
		childModels.addAll(this.getCastedModel().getOutputs());
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<WoofAccessModel> policy) {
		policy.allowOpening(new OpenHandler<WoofAccessModel>() {
			@Override
			public void doOpen(OpenHandlerContext<WoofAccessModel> context) {

				// Obtain the changes
				WoofChanges changes = (WoofChanges) WoofAccessEditPart.this
						.getEditor().getModelChanges();

				// Refactor access
				WoofAccessModel model = context.getModel();
				OperationUtil.execute(new RefactorAccessOperation(changes),
						model.getX(), model.getY(), context.getEditPart());
			}
		});
	}

	@Override
	protected Class<WoofAccessEvent> getPropertyChangeEventType() {
		return WoofAccessEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofAccessEvent property,
			PropertyChangeEvent event) {
		switch (property) {
		case ADD_INPUT:
		case REMOVE_INPUT:
		case ADD_OUTPUT:
		case REMOVE_OUTPUT:
			this.refreshChildren();
			break;

		case CHANGE_HTTP_SECURITY_SOURCE_CLASS_NAME:
		case CHANGE_TIMEOUT:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

}