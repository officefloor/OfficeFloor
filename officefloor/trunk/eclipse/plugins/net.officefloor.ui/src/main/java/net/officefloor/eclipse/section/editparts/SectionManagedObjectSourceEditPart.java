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
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel.SectionManagedObjectSourceEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SectionManagedObjectSourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorEditPart<SectionManagedObjectSourceModel, SectionManagedObjectSourceEvent, SectionManagedObjectSourceFigure>
		implements SectionManagedObjectSourceFigureContext {

	@Override
	protected SectionManagedObjectSourceFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory()
				.createSectionManagedObjectSourceFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getSectionManagedObjectSourceFlows());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getSectionManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<SectionManagedObjectSourceModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<SectionChanges, SectionManagedObjectSourceModel>() {
			@Override
			public String getInitialValue() {
				return SectionManagedObjectSourceEditPart.this.getCastedModel()
						.getSectionManagedObjectSourceName();
			}

			@Override
			public IFigure getLocationFigure() {
				return SectionManagedObjectSourceEditPart.this
						.getOfficeFloorFigure()
						.getSectionManagedObjectSourceNameFigure();
			}

			@Override
			public Change<SectionManagedObjectSourceModel> createChange(
					SectionChanges changes,
					SectionManagedObjectSourceModel target, String newValue) {
				return changes.renameSectionManagedObjectSource(target,
						newValue);
			}
		});
	}

	@Override
	protected Class<SectionManagedObjectSourceEvent> getPropertyChangeEventType() {
		return SectionManagedObjectSourceEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			SectionManagedObjectSourceEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_SECTION_MANAGED_OBJECT_SOURCE_NAME:
			this.getOfficeFloorFigure().setSectionManagedObjectName(
					this.getCastedModel().getSectionManagedObjectSourceName());
			break;

		case ADD_SECTION_MANAGED_OBJECT:
		case REMOVE_SECTION_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;

		case ADD_SECTION_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_SECTION_MANAGED_OBJECT_SOURCE_FLOW:
			this.refreshChildren();
			break;

		case CHANGE_MANAGED_OBJECT_SOURCE_CLASS_NAME:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
		case CHANGE_TIMEOUT:
		case CHANGE_OBJECT_TYPE:
			// Non visual changes
			break;
		}
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	@Override
	public String getSectionManagedObjectSourceName() {
		return this.getCastedModel().getSectionManagedObjectSourceName();
	}

}