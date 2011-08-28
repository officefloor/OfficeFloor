/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.desk.DeskManagedObjectDependencyModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyModel.DeskManagedObjectDependencyEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ManagedObjectDependencyModel}.
 *
 * @author Daniel Sagenschneider
 */
public class DeskManagedObjectDependencyEditPart
		extends
		AbstractOfficeFloorEditPart<DeskManagedObjectDependencyModel, DeskManagedObjectDependencyEvent, DeskManagedObjectDependencyFigure>
		implements DeskManagedObjectDependencyFigureContext {

	@Override
	protected DeskManagedObjectDependencyFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createDeskManagedObjectDependencyFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getDeskManagedObject());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getExternalManagedObject());
	}

	@Override
	protected Class<DeskManagedObjectDependencyEvent> getPropertyChangeEventType() {
		return DeskManagedObjectDependencyEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			DeskManagedObjectDependencyEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DESK_MANAGED_OBJECT:
		case CHANGE_EXTERNAL_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ==================== ManagedObjectDependencyFigureContext ============
	 */

	@Override
	public String getDeskManagedObjectDependencyName() {
		return this.getCastedModel().getDeskManagedObjectDependencyName();
	}

}