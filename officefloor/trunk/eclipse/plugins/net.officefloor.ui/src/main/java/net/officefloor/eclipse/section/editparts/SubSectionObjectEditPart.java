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
import net.officefloor.eclipse.skin.section.SubSectionObjectFigure;
import net.officefloor.eclipse.skin.section.SubSectionObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectModel.SubSectionObjectEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SubSectionObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionObjectEditPart
		extends
		AbstractOfficeFloorEditPart<SubSectionObjectModel, SubSectionObjectEvent, SubSectionObjectFigure>
		implements SubSectionObjectFigureContext {

	@Override
	protected SubSectionObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory()
				.createSubSectionObjectFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getExternalManagedObject());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getSectionManagedObject());
	}

	@Override
	protected Class<SubSectionObjectEvent> getPropertyChangeEventType() {
		return SubSectionObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(SubSectionObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_SUB_SECTION_OBJECT_NAME:
			this.getOfficeFloorFigure().setSubSectionObjectName(
					this.getSubSectionObjectName());
			break;

		case CHANGE_EXTERNAL_MANAGED_OBJECT:
		case CHANGE_SECTION_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;

		case CHANGE_OBJECT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ==================== SubSectionObjectFigureContext ===============
	 */

	@Override
	public String getSubSectionObjectName() {
		return this.getCastedModel().getSubSectionObjectName();
	}

}