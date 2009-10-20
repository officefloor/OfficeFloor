/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.eclipse.skin.office.OfficeManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel.OfficeManagedObjectDependencyEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ManagedObjectDependencyModel}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeManagedObjectDependencyEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeManagedObjectDependencyModel, OfficeManagedObjectDependencyEvent, OfficeManagedObjectDependencyFigure>
		implements OfficeManagedObjectDependencyFigureContext {

	@Override
	protected OfficeManagedObjectDependencyFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeManagedObjectDependencyFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeManagedObject());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getExternalManagedObject());
	}

	@Override
	protected Class<OfficeManagedObjectDependencyEvent> getPropertyChangeEventType() {
		return OfficeManagedObjectDependencyEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeManagedObjectDependencyEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_MANAGED_OBJECT:
		case CHANGE_EXTERNAL_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ==================== ManagedObjectDependencyFigureContext ============
	 */

	@Override
	public String getOfficeManagedObjectDependencyName() {
		return this.getCastedModel().getOfficeManagedObjectDependencyName();
	}

}