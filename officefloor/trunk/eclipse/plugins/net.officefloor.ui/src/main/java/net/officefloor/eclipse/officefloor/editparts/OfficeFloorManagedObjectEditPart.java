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
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel.OfficeFloorManagedObjectEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorManagedObjectModel, OfficeFloorManagedObjectEvent, OfficeFloorManagedObjectFigure>
		implements OfficeFloorManagedObjectFigureContext {

	/*
	 * =============== AbstractOfficeFloorEditPart ============================
	 */

	@Override
	protected OfficeFloorManagedObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectDependencies());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeFloorManagedObjectSource());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getDeployedOfficeObjects());
		models.addAll(this.getCastedModel()
				.getDependentOfficeFloorManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeFloorManagedObjectModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeFloorChanges, OfficeFloorManagedObjectModel>() {
			@Override
			public String getInitialValue() {
				return OfficeFloorManagedObjectEditPart.this.getCastedModel()
						.getOfficeFloorManagedObjectName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeFloorManagedObjectEditPart.this
						.getOfficeFloorFigure()
						.getOfficeFloorManagedObjectNameFigure();
			}

			@Override
			public Change<OfficeFloorManagedObjectModel> createChange(
					OfficeFloorChanges changes,
					OfficeFloorManagedObjectModel target, String newValue) {
				return changes.renameOfficeFloorManagedObject(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<OfficeFloorManagedObjectModel> policy) {
		policy.allowOpening(new OpenHandler<OfficeFloorManagedObjectModel>() {
			@Override
			public void doOpen(
					OpenHandlerContext<OfficeFloorManagedObjectModel> context) {

				// Obtain the managed object source for the managed object
				OfficeFloorManagedObjectSourceModel managedObjectSource = null;
				OfficeFloorManagedObjectModel managedObject = context
						.getModel();
				if (managedObject != null) {
					OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel conn = managedObject
							.getOfficeFloorManagedObjectSource();
					if (conn != null) {
						managedObjectSource = conn
								.getOfficeFloorManagedObjectSource();
					}
				}
				if (managedObjectSource == null) {
					// Must have connected managed object source
					context.getEditPart()
							.messageError(
									"Can not open managed object.\n"
											+ "\nPlease ensure the managed object is connected to a managed object source.");
					return; // can not open
				}

				// Open the managed object source for the managed object
				OfficeFloorManagedObjectSourceEditPart.openManagedObjectSource(
						managedObjectSource, context);
			}
		});
	}

	@Override
	protected Class<OfficeFloorManagedObjectEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeFloorManagedObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_NAME:
			this.getOfficeFloorFigure().setOfficeFloorManagedObjectName(
					this.getCastedModel().getOfficeFloorManagedObjectName());
			break;

		case CHANGE_MANAGED_OBJECT_SCOPE:
			this.getOfficeFloorFigure().setManagedObjectScope(
					this.getManagedObjectScope());
			break;

		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_DEPENDENCY:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_DEPENDENCY:
			this.refreshChildren();
			break;

		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
			this.refreshSourceConnections();
			break;

		case ADD_DEPLOYED_OFFICE_OBJECT:
		case REMOVE_DEPLOYED_OFFICE_OBJECT:
		case ADD_DEPENDENT_OFFICE_FLOOR_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_OFFICE_FLOOR_MANAGED_OBJECT:
		case ADD_DEPENDENT_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT:
		case REMOVE_DEPENDENT_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ================= OfficeFloorManagedObjectFigureContext ===============
	 */

	@Override
	public String getOfficeFloorManagedObjectName() {
		return this.getCastedModel().getOfficeFloorManagedObjectName();
	}

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		// Return the scope
		String scopeName = this.getCastedModel().getManagedObjectScope();
		if (OfficeFloorChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.PROCESS;
		} else if (OfficeFloorChanges.THREAD_MANAGED_OBJECT_SCOPE
				.equals(scopeName)) {
			return ManagedObjectScope.THREAD;
		} else if (OfficeFloorChanges.WORK_MANAGED_OBJECT_SCOPE
				.equals(scopeName)) {
			return ManagedObjectScope.WORK;
		} else {
			// Unknown scope
			return null;
		}
	}

}