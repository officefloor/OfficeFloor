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
import net.officefloor.eclipse.skin.woof.TemplateFigure;
import net.officefloor.eclipse.skin.woof.TemplateFigureContext;
import net.officefloor.eclipse.woof.operations.RefactorTemplateOperation;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateModel.WoofTemplateEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateEditPart
		extends
		AbstractOfficeFloorEditPart<WoofTemplateModel, WoofTemplateEvent, TemplateFigure>
		implements TemplateFigureContext {

	@Override
	protected TemplateFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createTemplateFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOutputs());
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
			OfficeFloorOpenEditPolicy<WoofTemplateModel> policy) {
		policy.allowOpening(new OpenHandler<WoofTemplateModel>() {
			@Override
			public void doOpen(OpenHandlerContext<WoofTemplateModel> context) {

				// Obtain the changes
				WoofChanges changes = (WoofChanges) WoofTemplateEditPart.this
						.getEditor().getModelChanges();

				// Refactor template
				WoofTemplateModel model = context.getModel();
				OperationUtil.execute(new RefactorTemplateOperation(changes),
						model.getX(), model.getY(), context.getEditPart());
			}
		});
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<WoofTemplateModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<WoofChanges, WoofTemplateModel>() {
			@Override
			public String getInitialValue() {
				return WoofTemplateEditPart.this.getCastedModel().getUri();
			}

			@Override
			public IFigure getLocationFigure() {
				return WoofTemplateEditPart.this.getOfficeFloorFigure()
						.getTemplateDisplayFigure();
			}

			@Override
			public Change<WoofTemplateModel> createChange(WoofChanges changes,
					WoofTemplateModel target, String newValue) {
				return changes.changeTemplateUri(target, newValue);
			}
		});
	}

	@Override
	protected Class<WoofTemplateEvent> getPropertyChangeEventType() {
		return WoofTemplateEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofTemplateEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_TEMPLATE_NAME:
		case CHANGE_URI:
			this.getOfficeFloorFigure().setTemplateDisplayName(
					this.getTemplateDisplayName());
			break;

		case ADD_OUTPUT:
		case REMOVE_OUTPUT:
			this.refreshChildren();
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

		case CHANGE_IS_TEMPLATE_SECURE:
			this.getOfficeFloorFigure().setTemplateSecure(
					this.isTemplateSecure());
			break;

		case CHANGE_TEMPLATE_CLASS_NAME:
		case CHANGE_TEMPLATE_PATH:
		case CHANGE_SUPER_TEMPLATE:
		case ADD_LINK:
		case REMOVE_LINK:
		case ADD_REDIRECT:
		case REMOVE_REDIRECT:
		case CHANGE_IS_CONTINUE_RENDERING:
		case ADD_EXTENSION:
		case REMOVE_EXTENSION:
			// No visual change
			break;
		}
	}

	/*
	 * =========================== TemplateFigureContext =======================
	 */

	@Override
	public String getTemplateDisplayName() {

		// Obtain the display name
		String displayName;
		String templateUri = this.getCastedModel().getUri();
		if ("/".equals(templateUri)) {
			// Root template
			displayName = templateUri;

		} else {
			// Use simplified URI
			displayName = (templateUri.startsWith("/") ? templateUri
					.substring("/".length()) : templateUri);
		}

		// Provide the display name
		return displayName;
	}

	@Override
	public boolean isTemplateSecure() {
		return this.getCastedModel().getIsTemplateSecure();
	}

}