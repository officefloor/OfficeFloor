/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel.OfficeFloorManagedObjectSourceToDeployedOfficeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the
 * {@link OfficeFloorManagedObjectSourceToDeployedOfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorManagedObjectSourceToDeployedOfficeEditPart
		extends
		AbstractOfficeFloorConnectionEditPart<OfficeFloorManagedObjectSourceToDeployedOfficeModel, OfficeFloorManagedObjectSourceToDeployedOfficeEvent>
		implements OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext {

	/**
	 * {@link OfficeFloorManagedObjectSourceToDeployedOfficeFigure}.
	 */
	private OfficeFloorManagedObjectSourceToDeployedOfficeFigure decoratedFigure;

	/*
	 * =============== AbstractOfficeFloorConnectionEditPart ===================
	 */

	@Override
	protected void decorateFigure(PolylineConnection figure) {
		this.decoratedFigure = OfficeFloorPlugin.getSkin()
				.getOfficeFloorFigureFactory()
				.decorateOfficeFloorManagedObjectSourceToDeployedOfficeFigure(
						figure, this);
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeFloorManagedObjectSourceToDeployedOfficeModel> policy) {
		policy
				.allowDirectEdit(new DirectEditAdapter<OfficeFloorChanges, OfficeFloorManagedObjectSourceToDeployedOfficeModel>() {
					@Override
					public String getInitialValue() {
						return OfficeFloorManagedObjectSourceToDeployedOfficeEditPart.this
								.getCastedModel()
								.getProcessBoundManagedObjectName();
					}

					@Override
					public IFigure getLocationFigure() {
						return OfficeFloorManagedObjectSourceToDeployedOfficeEditPart.this.decoratedFigure
								.getProcessBoundManagedObjectNameFigure();
					}

					@Override
					public Change<OfficeFloorManagedObjectSourceToDeployedOfficeModel> createChange(
							OfficeFloorChanges changes,
							OfficeFloorManagedObjectSourceToDeployedOfficeModel target,
							String newValue) {
						return changes.setProcessBoundManagedObjectName(target,
								newValue);
					}
				});
	}

	@Override
	protected Class<OfficeFloorManagedObjectSourceToDeployedOfficeEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectSourceToDeployedOfficeEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorManagedObjectSourceToDeployedOfficeEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_PROCESS_BOUND_MANAGED_OBJECT_NAME:
			this.decoratedFigure.setProcessBoundManagedObjectName(this
					.getCastedModel().getProcessBoundManagedObjectName());
			break;
		}
	}

	/*
	 * ===== OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext =====
	 */

	@Override
	public String getProcessBoundManagedObjectName() {
		return this.getCastedModel().getProcessBoundManagedObjectName();
	}

}