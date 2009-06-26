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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel.OfficeFloorManagedObjectSourceEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorManagedObjectSourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorManagedObjectSourceModel, OfficeFloorManagedObjectSourceEvent, OfficeFloorManagedObjectSourceFigure>
		implements OfficeFloorManagedObjectSourceFigureContext {

	@Override
	protected OfficeFloorManagedObjectSourceFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectSourceFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceFlows());
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceTeams());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil
				.addToList(models, this.getCastedModel().getManagingOffice());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeFloorManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeFloorManagedObjectSourceModel> policy) {
		policy
				.allowDirectEdit(new DirectEditAdapter<OfficeFloorChanges, OfficeFloorManagedObjectSourceModel>() {
					@Override
					public String getInitialValue() {
						return OfficeFloorManagedObjectSourceEditPart.this
								.getCastedModel()
								.getOfficeFloorManagedObjectSourceName();
					}

					@Override
					public IFigure getLocationFigure() {
						return OfficeFloorManagedObjectSourceEditPart.this
								.getOfficeFloorFigure()
								.getOfficeFloorManagedObjectSourceNameFigure();
					}

					@Override
					public Change<OfficeFloorManagedObjectSourceModel> createChange(
							OfficeFloorChanges changes,
							OfficeFloorManagedObjectSourceModel target,
							String newValue) {
						return changes.renameOfficeFloorManagedObjectSource(
								target, newValue);
					}
				});
	}

	@Override
	protected Class<OfficeFloorManagedObjectSourceEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectSourceEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorManagedObjectSourceEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_NAME:
			this.getOfficeFloorFigure().setOfficeFloorManagedObjectName(
					this.getCastedModel()
							.getOfficeFloorManagedObjectSourceName());
			break;
		case CHANGE_MANAGING_OFFICE:
			this.refreshSourceConnections();
			break;
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
			this.refreshChildren();
			break;
		}
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.getCastedModel().getOfficeFloorManagedObjectSourceName();
	}

}