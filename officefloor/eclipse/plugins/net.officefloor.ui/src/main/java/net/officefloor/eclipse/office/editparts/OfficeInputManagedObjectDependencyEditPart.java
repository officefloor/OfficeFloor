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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel.OfficeInputManagedObjectDependencyEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeInputManagedObjectDependencyModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeInputManagedObjectDependencyEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeInputManagedObjectDependencyModel, OfficeInputManagedObjectDependencyEvent, OfficeInputManagedObjectDependencyFigure>
		implements OfficeInputManagedObjectDependencyFigureContext {

	@Override
	protected OfficeInputManagedObjectDependencyFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeInputManagedObjectDependencyFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getOfficeManagedObject());
		EclipseUtil.addToList(models, this.getCastedModel().getExternalManagedObject());
	}

	@Override
	protected Class<OfficeInputManagedObjectDependencyEvent> getPropertyChangeEventType() {
		return OfficeInputManagedObjectDependencyEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeInputManagedObjectDependencyEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_INPUT_MANAGED_OBJECT_DEPENDENCY_NAME:
			this.getOfficeFloorFigure()
					.setOfficeInputManagedObjectDependencyName(
							this.getOfficeInputManagedObjectDependencyName());
			break;

		case CHANGE_EXTERNAL_MANAGED_OBJECT:
		case CHANGE_OFFICE_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;

		case CHANGE_DEPENDENCY_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ============ OfficeInputManagedObjectDependencyFigureContext ============
	 */

	@Override
	public String getOfficeInputManagedObjectDependencyName() {
		return this.getCastedModel()
				.getOfficeInputManagedObjectDependencyName();
	}

}