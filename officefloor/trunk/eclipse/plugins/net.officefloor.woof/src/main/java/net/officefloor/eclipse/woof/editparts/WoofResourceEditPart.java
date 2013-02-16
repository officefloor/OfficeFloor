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
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.skin.woof.ResourceFigure;
import net.officefloor.eclipse.skin.woof.ResourceFigureContext;
import net.officefloor.eclipse.woof.operations.RefactorResourceOperation;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofResourceModel;
import net.officefloor.model.woof.WoofResourceModel.WoofResourceEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofResourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofResourceEditPart
		extends
		AbstractOfficeFloorEditPart<WoofResourceModel, WoofResourceEvent, ResourceFigure>
		implements ResourceFigureContext {

	@Override
	protected ResourceFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createResourceFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getWoofTemplateOutputs());
		models.addAll(this.getCastedModel().getWoofSectionOutputs());
		models.addAll(this.getCastedModel().getWoofAccessOutputs());
		models.addAll(this.getCastedModel().getWoofExceptions());
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<WoofResourceModel> policy) {
		policy.allowOpening(new OpenHandler<WoofResourceModel>() {
			@Override
			public void doOpen(OpenHandlerContext<WoofResourceModel> context) {

				// Obtain the changes
				WoofChanges changes = (WoofChanges) WoofResourceEditPart.this
						.getEditor().getModelChanges();

				// Refactor resource
				WoofResourceModel model = context.getModel();
				OperationUtil.execute(new RefactorResourceOperation(changes),
						model.getX(), model.getY(), context.getEditPart());
			}
		});
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<WoofResourceModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<WoofChanges, WoofResourceModel>() {
			@Override
			public String getInitialValue() {
				return WoofResourceEditPart.this.getCastedModel()
						.getResourcePath();
			}

			@Override
			public IFigure getLocationFigure() {
				return WoofResourceEditPart.this.getOfficeFloorFigure()
						.getResourcePathFigure();
			}

			@Override
			public Change<WoofResourceModel> createChange(WoofChanges changes,
					WoofResourceModel target, String newValue) {
				return changes.changeResourcePath(target, newValue);
			}
		});
	}

	@Override
	protected Class<WoofResourceEvent> getPropertyChangeEventType() {
		return WoofResourceEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofResourceEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_RESOURCE_NAME:
		case CHANGE_RESOURCE_PATH:
			this.getOfficeFloorFigure().setResourcePath(
					this.getCastedModel().getResourcePath());
			break;
		case ADD_WOOF_TEMPLATE_OUTPUT:
		case REMOVE_WOOF_TEMPLATE_OUTPUT:
		case ADD_WOOF_SECTION_OUTPUT:
		case REMOVE_WOOF_SECTION_OUTPUT:
		case ADD_WOOF_ACCESS_OUTPUT:
		case REMOVE_WOOF_ACCESS_OUTPUT:
		case ADD_WOOF_EXCEPTION:
		case REMOVE_WOOF_EXCEPTION:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * =========================== ResourceFigureContext =======================
	 */

	@Override
	public String getResourceName() {
		return this.getCastedModel().getWoofResourceName();
	}

	@Override
	public String getResourcePath() {
		return this.getCastedModel().getResourcePath();
	}

}