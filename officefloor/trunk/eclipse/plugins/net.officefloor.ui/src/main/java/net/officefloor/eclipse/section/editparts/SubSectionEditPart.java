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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.section.SubSectionFigure;
import net.officefloor.eclipse.skin.section.SubSectionFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionModel.SubSectionEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SubSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionEditPart
		extends
		AbstractOfficeFloorEditPart<SubSectionModel, SubSectionEvent, SubSectionFigure>
		implements SubSectionFigureContext {

	@Override
	protected SubSectionFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory()
				.createSubSectionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getSubSectionInputs());
		childModels.addAll(this.getCastedModel().getSubSectionOutputs());
		childModels.addAll(this.getCastedModel().getSubSectionObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<SubSectionModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<SectionChanges, SubSectionModel>() {
			@Override
			public String getInitialValue() {
				return SubSectionEditPart.this.getCastedModel()
						.getSubSectionName();
			}

			@Override
			public IFigure getLocationFigure() {
				return SubSectionEditPart.this.getOfficeFloorFigure()
						.getSubSectionNameFigure();
			}

			@Override
			public Change<SubSectionModel> createChange(SectionChanges changes,
					SubSectionModel target, String newValue) {
				return changes.renameSubSection(target, newValue);
			}
		});
	}

	@Override
	protected Class<SubSectionEvent> getPropertyChangeEventType() {
		return SubSectionEvent.class;
	}

	@Override
	protected void handlePropertyChange(SubSectionEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_SUB_SECTION_NAME:
			this.getOfficeFloorFigure().setSubSectionName(
					this.getCastedModel().getSubSectionName());
			break;

		case ADD_SUB_SECTION_INPUT:
		case REMOVE_SUB_SECTION_INPUT:
		case ADD_SUB_SECTION_OUTPUT:
		case REMOVE_SUB_SECTION_OUTPUT:
		case ADD_SUB_SECTION_OBJECT:
		case REMOVE_SUB_SECTION_OBJECT:
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
	 * ================= SubRoomFigureContext =======================
	 */

	@Override
	public String getSubSectionName() {
		return this.getCastedModel().getSubSectionName();
	}

}