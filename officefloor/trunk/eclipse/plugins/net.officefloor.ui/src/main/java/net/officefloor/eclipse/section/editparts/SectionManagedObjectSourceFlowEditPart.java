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
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel.SectionManagedObjectSourceFlowEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SectionManagedObjectSourceFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionManagedObjectSourceFlowEditPart
		extends
		AbstractOfficeFloorEditPart<SectionManagedObjectSourceFlowModel, SectionManagedObjectSourceFlowEvent, SectionManagedObjectSourceFlowFigure>
		implements SectionManagedObjectSourceFlowFigureContext {

	@Override
	protected SectionManagedObjectSourceFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory()
				.createSectionManagedObjectSourceFlowFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getSubSectionInput());
		EclipseUtil.addToList(models, this.getCastedModel().getExternalFlow());
	}

	@Override
	protected Class<SectionManagedObjectSourceFlowEvent> getPropertyChangeEventType() {
		return SectionManagedObjectSourceFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			SectionManagedObjectSourceFlowEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_SECTION_MANAGED_OBJECT_SOURCE_FLOW_NAME:
			this.getOfficeFloorFigure().setSectionManagedObjectSourceFlowName(
					this.getSectionManagedObjectSourceFlowName());
			break;

		case CHANGE_SUB_SECTION_INPUT:
		case CHANGE_EXTERNAL_FLOW:
			SectionManagedObjectSourceFlowEditPart.this
					.refreshSourceConnections();
			break;

		case CHANGE_ARGUMENT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ============== SectionManagedObjectSourceFlowFigureContext ==============
	 */

	@Override
	public String getSectionManagedObjectSourceFlowName() {
		return this.getCastedModel().getSectionManagedObjectSourceFlowName();
	}

}