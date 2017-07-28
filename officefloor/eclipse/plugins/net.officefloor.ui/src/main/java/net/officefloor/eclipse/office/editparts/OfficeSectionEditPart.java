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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.skin.office.OfficeSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.office.OfficeSectionModel.OfficeSectionEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionEditPart
		extends AbstractOfficeFloorEditPart<OfficeSectionModel, OfficeSectionEvent, OfficeSectionFigure>
		implements OfficeSectionFigureContext {

	@Override
	protected OfficeSectionFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory().createOfficeSectionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOfficeSectionInputs());
		childModels.addAll(this.getCastedModel().getOfficeSectionOutputs());
		childModels.addAll(this.getCastedModel().getOfficeSectionObjects());
		EclipseUtil.addToList(childModels, this.getCastedModel().getOfficeSubSection());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(OfficeFloorDirectEditPolicy<OfficeSectionModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeChanges, OfficeSectionModel>() {
			@Override
			public String getInitialValue() {
				return OfficeSectionEditPart.this.getCastedModel().getOfficeSectionName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeSectionEditPart.this.getOfficeFloorFigure().getOfficeSectionNameFigure();
			}

			@Override
			public Change<OfficeSectionModel> createChange(OfficeChanges changes, OfficeSectionModel target,
					String newValue) {
				return changes.renameOfficeSection(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(OfficeFloorOpenEditPolicy<OfficeSectionModel> policy) {
		policy.allowOpening(new OpenHandler<OfficeSectionModel>() {
			@Override
			public void doOpen(OpenHandlerContext<OfficeSectionModel> context) {

				// Obtain the section details
				OfficeSectionModel section = context.getModel();
				String className = section.getSectionSourceClassName();
				String location = section.getSectionLocation();
				PropertyList properties = context.createPropertyList();
				for (PropertyModel property : section.getProperties()) {
					properties.addProperty(property.getName()).setValue(property.getValue());
				}

				// Open the section
				ExtensionUtil.openSectionSource(className, location, properties, context.getEditPart().getEditor());
			}
		});
	}

	@Override
	protected Class<OfficeSectionEvent> getPropertyChangeEventType() {
		return OfficeSectionEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeSectionEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_SECTION_NAME:
			this.getOfficeFloorFigure().setOfficeSectionName(this.getCastedModel().getOfficeSectionName());
			break;

		case ADD_OFFICE_SECTION_INPUT:
		case REMOVE_OFFICE_SECTION_INPUT:
		case ADD_OFFICE_SECTION_OUTPUT:
		case REMOVE_OFFICE_SECTION_OUTPUT:
		case ADD_OFFICE_SECTION_OBJECT:
		case REMOVE_OFFICE_SECTION_OBJECT:
		case CHANGE_OFFICE_SUB_SECTION:
			this.refreshChildren();
			break;

		case CHANGE_SECTION_SOURCE_CLASS_NAME:
		case CHANGE_SECTION_LOCATION:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

	/*
	 * ================== RoomFigureContext ===============================
	 */

	@Override
	public String getOfficeSectionName() {
		return this.getCastedModel().getOfficeSectionName();
	}

}