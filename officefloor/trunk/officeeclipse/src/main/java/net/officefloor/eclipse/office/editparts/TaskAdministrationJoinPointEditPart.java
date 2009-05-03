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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.office.models.AbstractTaskAdministrationJoinPointModel;
import net.officefloor.eclipse.office.models.TaskAdministrationJoinPointEvent;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.OfficeTaskModel;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for {@link AbstractTaskAdministrationJoinPointModel}.
 * 
 * @author Daniel
 */
public class TaskAdministrationJoinPointEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<AbstractTaskAdministrationJoinPointModel<? extends ConnectionModel>, OfficeFloorFigure> {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createTaskAdministrationJoinPointFigure();
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers
				.add(new PropertyChangeHandler<TaskAdministrationJoinPointEvent>(
						TaskAdministrationJoinPointEvent.values()) {
					@Override
					protected void handlePropertyChange(
							TaskAdministrationJoinPointEvent property,
							PropertyChangeEvent evt) {
						switch (property) {
						case CHANGE_DUTIES:
							TaskAdministrationJoinPointEditPart.this
									.refreshSourceConnections();
							break;
						}
					}
				});
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			@Override
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				// Obtain the types
				AbstractTaskAdministrationJoinPointModel<?> joinPoint = (AbstractTaskAdministrationJoinPointModel<?>) source;
				OfficeTaskModel task = joinPoint.getTask();
				DutyModel duty = (DutyModel) target;

				// Create the connection to the duty
				ConnectionModel connection = TaskAdministrationJoinPointEditPart.this
						.getCastedModel().createDutyConnection(task, duty);

				// Connect and return the connection
				connection.connect();
				return connection;
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(DutyModel.class);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getDutyConnections());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Never a target
	}

}