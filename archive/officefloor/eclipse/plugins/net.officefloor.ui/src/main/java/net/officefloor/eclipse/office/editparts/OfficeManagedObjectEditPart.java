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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel.OfficeManagedObjectEvent;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;

/**
 * {@link EditPart} for the {@link OfficeManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagedObjectEditPart extends
		AbstractOfficeFloorEditPart<OfficeManagedObjectModel, OfficeManagedObjectEvent, OfficeManagedObjectFigure>
		implements OfficeManagedObjectFigureContext {

	/*
	 * =============== AbstractOfficeFloorEditPart ============================
	 */

	@Override
	protected OfficeManagedObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory().createOfficeManagedObjectFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOfficeManagedObjectDependencies());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getOfficeManagedObjectSource());
		models.addAll(this.getCastedModel().getAdministrations());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeSectionObjects());
		models.addAll(this.getCastedModel().getDependentOfficeManagedObjects());
		models.addAll(this.getCastedModel().getDependentOfficeInputManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(OfficeFloorDirectEditPolicy<OfficeManagedObjectModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeChanges, OfficeManagedObjectModel>() {
			@Override
			public String getInitialValue() {
				return OfficeManagedObjectEditPart.this.getCastedModel().getOfficeManagedObjectName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeManagedObjectEditPart.this.getOfficeFloorFigure().getOfficeManagedObjectNameFigure();
			}

			@Override
			public Change<OfficeManagedObjectModel> createChange(OfficeChanges changes, OfficeManagedObjectModel target,
					String newValue) {
				return changes.renameOfficeManagedObject(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(OfficeFloorOpenEditPolicy<OfficeManagedObjectModel> policy) {
		policy.allowOpening(new OpenHandler<OfficeManagedObjectModel>() {
			@Override
			public void doOpen(OpenHandlerContext<OfficeManagedObjectModel> context) {

				// Obtain the managed object source for the managed object
				OfficeManagedObjectSourceModel managedObjectSource = null;
				OfficeManagedObjectModel managedObject = context.getModel();
				if (managedObject != null) {
					OfficeManagedObjectToOfficeManagedObjectSourceModel conn = managedObject
							.getOfficeManagedObjectSource();
					if (conn != null) {
						managedObjectSource = conn.getOfficeManagedObjectSource();
					}
				}
				if (managedObjectSource == null) {
					// Must have connected managed object source
					context.getEditPart().messageError("Can not open managed object.\n"
							+ "\nPlease ensure the managed object is connected to a managed object source.");
					return; // can not open
				}

				// Open the managed object source
				OfficeManagedObjectSourceEditPart.openManagedObjectSource(managedObjectSource, context);
			}
		});
	}

	@Override
	protected Class<OfficeManagedObjectEvent> getPropertyChangeEventType() {
		return OfficeManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeManagedObjectEvent property, PropertyChangeEvent evt) {
		switch (property) {

		case CHANGE_OFFICE_MANAGED_OBJECT_NAME:
			this.getOfficeFloorFigure().setOfficeManagedObjectName(this.getCastedModel().getOfficeManagedObjectName());
			break;

		case CHANGE_MANAGED_OBJECT_SCOPE:
			this.getOfficeFloorFigure().setManagedObjectScope(this.getManagedObjectScope());
			break;

		case ADD_OFFICE_MANAGED_OBJECT_DEPENDENCY:
		case REMOVE_OFFICE_MANAGED_OBJECT_DEPENDENCY:
			this.refreshChildren();
			break;

		case CHANGE_OFFICE_MANAGED_OBJECT_SOURCE:
		case ADD_ADMINISTRATION:
		case REMOVE_ADMINISTRATION:
			this.refreshSourceConnections();
			break;

		case ADD_OFFICE_SECTION_OBJECT:
		case REMOVE_OFFICE_SECTION_OBJECT:
		case ADD_DEPENDENT_OFFICE_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_OFFICE_MANAGED_OBJECT:
		case ADD_DEPENDENT_OFFICE_INPUT_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_OFFICE_INPUT_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ================= OfficeManagedObjectFigureContext ===============
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.getCastedModel().getOfficeManagedObjectName();
	}

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		// Return the scope
		String scopeName = this.getCastedModel().getManagedObjectScope();
		if (OfficeChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.PROCESS;
		} else if (OfficeChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.THREAD;
		} else if (OfficeChanges.FUNCTION_MANAGED_OBJECT_SCOPE.equals(scopeName)) {
			return ManagedObjectScope.FUNCTION;
		} else {
			// Unknown scope
			return null;
		}
	}

}