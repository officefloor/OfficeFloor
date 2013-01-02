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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceInputDependencyFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceInputDependencyFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel.OfficeFloorManagedObjectSourceInputDependencyEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the
 * {@link OfficeFloorManagedObjectSourceInputDependencyModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceInputDependencyEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorManagedObjectSourceInputDependencyModel, OfficeFloorManagedObjectSourceInputDependencyEvent, OfficeFloorManagedObjectSourceInputDependencyFigure>
		implements OfficeFloorManagedObjectSourceInputDependencyFigureContext {

	@Override
	protected OfficeFloorManagedObjectSourceInputDependencyFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin
				.getSkin()
				.getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectSourceInputDependencyFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeFloorManagedObject());
	}

	@Override
	protected Class<OfficeFloorManagedObjectSourceInputDependencyEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectSourceInputDependencyEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorManagedObjectSourceInputDependencyEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY_NAME:
			this.getOfficeFloorFigure()
					.setOfficeFloorManagedObjectSourceInputDependencyName(
							this.getOfficeFloorManagedObjectSourceInputDependencyName());
			break;

		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;

		case CHANGE_DEPENDENCY_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ===== OfficeFloorManagedObjectSourceInputDependencyFigureContext =======
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceInputDependencyName() {
		return this.getCastedModel()
				.getOfficeFloorManagedObjectSourceInputDependencyName();
	}

}