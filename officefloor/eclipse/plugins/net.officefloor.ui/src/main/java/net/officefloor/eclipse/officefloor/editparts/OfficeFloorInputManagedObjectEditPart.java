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
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorInputManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorInputManagedObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel.OfficeFloorInputManagedObjectEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorInputManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorInputManagedObjectEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorInputManagedObjectModel, OfficeFloorInputManagedObjectEvent, OfficeFloorInputManagedObjectFigure>
		implements OfficeFloorInputManagedObjectFigureContext {

	/*
	 * =============== AbstractOfficeFloorEditPart ============================
	 */

	@Override
	protected OfficeFloorInputManagedObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorInputManagedObjectFigure(this);
	}

	@Override
	protected Class<OfficeFloorInputManagedObjectEvent> getPropertyChangeEventType() {
		return OfficeFloorInputManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorInputManagedObjectEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_FLOOR_INPUT_MANAGED_OBJECT_NAME:
			this.getOfficeFloorFigure().setOfficeFloorInputManagedObjectName(
					this.getCastedModel()
							.getOfficeFloorInputManagedObjectName());
			break;

		case ADD_DEPLOYED_OFFICE_OBJECT:
		case REMOVE_DEPLOYED_OFFICE_OBJECT:
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
		case ADD_DEPENDENT_OFFICE_FLOOR_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_OFFICE_FLOOR_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;

		case CHANGE_BOUND_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
			this.refreshSourceConnections();
			break;

		case CHANGE_OBJECT_TYPE:
			// Non visual change
			break;
		}
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getBoundOfficeFloorManagedObjectSource());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSources());
		models.addAll(this.getCastedModel().getDeployedOfficeObjects());
		models.addAll(this.getCastedModel()
				.getDependentOfficeFloorManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeFloorInputManagedObjectModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeFloorChanges, OfficeFloorInputManagedObjectModel>() {
			@Override
			public String getInitialValue() {
				return OfficeFloorInputManagedObjectEditPart.this
						.getCastedModel()
						.getOfficeFloorInputManagedObjectName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeFloorInputManagedObjectEditPart.this
						.getOfficeFloorFigure()
						.getOfficeFloorInputManagedObjectNameFigure();
			}

			@Override
			public Change<OfficeFloorInputManagedObjectModel> createChange(
					OfficeFloorChanges changes,
					OfficeFloorInputManagedObjectModel target, String newValue) {
				return changes.renameOfficeFloorInputManagedObject(target,
						newValue);
			}
		});
	}

	/*
	 * ============== OfficeFloorInputManagedObjectFigureContext ===============
	 */

	@Override
	public String getOfficeFloorInputManagedObjectName() {
		return this.getCastedModel().getOfficeFloorInputManagedObjectName();
	}

}