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
import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel.SectionManagedObjectDependencyEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ManagedObjectDependencyModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionManagedObjectDependencyEditPart
		extends
		AbstractOfficeFloorEditPart<SectionManagedObjectDependencyModel, SectionManagedObjectDependencyEvent, SectionManagedObjectDependencyFigure>
		implements SectionManagedObjectDependencyFigureContext {

	@Override
	protected SectionManagedObjectDependencyFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory()
				.createSectionManagedObjectDependencyFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getSectionManagedObject());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getExternalManagedObject());
	}

	@Override
	protected Class<SectionManagedObjectDependencyEvent> getPropertyChangeEventType() {
		return SectionManagedObjectDependencyEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			SectionManagedObjectDependencyEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_SECTION_MANAGED_OBJECT:
		case CHANGE_EXTERNAL_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;

		case CHANGE_SECTION_MANAGED_OBJECT_DEPENDENCY_NAME:
			this.getOfficeFloorFigure().setSectionManagedObjectDependencyName(
					this.getSectionManagedObjectDependencyName());
			break;

		case CHANGE_DEPENDENCY_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ==================== ManagedObjectDependencyFigureContext ============
	 */

	@Override
	public String getSectionManagedObjectDependencyName() {
		return this.getCastedModel().getSectionManagedObjectDependencyName();
	}

}