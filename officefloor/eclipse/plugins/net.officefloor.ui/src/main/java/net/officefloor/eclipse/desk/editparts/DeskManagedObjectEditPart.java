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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskManagedObjectModel.DeskManagedObjectEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DeskManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskManagedObjectEditPart
		extends
		AbstractOfficeFloorEditPart<DeskManagedObjectModel, DeskManagedObjectEvent, DeskManagedObjectFigure>
		implements DeskManagedObjectFigureContext {

	/*
	 * =============== AbstractOfficeFloorEditPart ============================
	 */

	@Override
	protected DeskManagedObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createDeskManagedObjectFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getDeskManagedObjectDependencies());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getDeskManagedObjectSource());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getWorkTaskObjects());
		models.addAll(this.getCastedModel().getDependentDeskManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<DeskManagedObjectModel> policy) {
		policy
				.allowDirectEdit(new DirectEditAdapter<DeskChanges, DeskManagedObjectModel>() {
					@Override
					public String getInitialValue() {
						return DeskManagedObjectEditPart.this.getCastedModel()
								.getDeskManagedObjectName();
					}

					@Override
					public IFigure getLocationFigure() {
						return DeskManagedObjectEditPart.this
								.getOfficeFloorFigure()
								.getDeskManagedObjectNameFigure();
					}

					@Override
					public Change<DeskManagedObjectModel> createChange(
							DeskChanges changes, DeskManagedObjectModel target,
							String newValue) {
						return changes
								.renameDeskManagedObject(target, newValue);
					}
				});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<DeskManagedObjectModel> policy) {
		policy.allowOpening(new OpenHandler<DeskManagedObjectModel>() {
			@Override
			public void doOpen(
					OpenHandlerContext<DeskManagedObjectModel> context) {
				// Obtain the managed object source for the managed object
				DeskManagedObjectSourceModel managedObjectSource = null;
				DeskManagedObjectModel managedObject = context.getModel();
				if (managedObject != null) {
					DeskManagedObjectToDeskManagedObjectSourceModel conn = managedObject
							.getDeskManagedObjectSource();
					if (conn != null) {
						managedObjectSource = conn.getDeskManagedObjectSource();
					}
				}
				if (managedObjectSource == null) {
					// Must have connected managed object source
					context
							.getEditPart()
							.messageError(
									"Can not open managed object.\n"
											+ "\nPlease ensure the managed object is connected to a managed object source.");
					return; // can not open
				}

				// Open the managed object source
				DeskManagedObjectSourceEditPart.openManagedObjectSource(
						managedObjectSource, context);
			}
		});
	}

	@Override
	protected Class<DeskManagedObjectEvent> getPropertyChangeEventType() {
		return DeskManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(DeskManagedObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DESK_MANAGED_OBJECT_NAME:
			this.getOfficeFloorFigure().setDeskManagedObjectName(
					this.getCastedModel().getDeskManagedObjectName());
			break;
		case CHANGE_MANAGED_OBJECT_SCOPE:
			this.getOfficeFloorFigure().setManagedObjectScope(
					this.getManagedObjectScope());
			break;
		case ADD_DESK_MANAGED_OBJECT_DEPENDENCY:
		case REMOVE_DESK_MANAGED_OBJECT_DEPENDENCY:
			this.refreshChildren();
			break;
		case CHANGE_DESK_MANAGED_OBJECT_SOURCE:
			this.refreshSourceConnections();
			break;
		case ADD_WORK_TASK_OBJECT:
		case REMOVE_WORK_TASK_OBJECT:
		case ADD_DEPENDENT_DESK_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_DESK_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ================= DeskManagedObjectFigureContext ===============
	 */

	@Override
	public String getDeskManagedObjectName() {
		return this.getCastedModel().getDeskManagedObjectName();
	}

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		// Return the scope
		String scopeName = this.getCastedModel().getManagedObjectScope();
		if (DeskChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.PROCESS;
		} else if (DeskChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.THREAD;
		} else if (DeskChanges.WORK_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.WORK;
		} else {
			// Unknown scope
			return null;
		}
	}

}