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
import java.util.List;

import net.officefloor.eclipse.common.dialog.OfficeTaskDialog;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.officefloor.LinkProcessToOfficeTaskModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeTaskModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel.ManagedObjectHandlerLinkProcessEvent;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link ManagedObjectHandlerLinkProcessModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectHandlerLinkProcessEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<ManagedObjectHandlerLinkProcessModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers
				.add(new PropertyChangeHandler<ManagedObjectHandlerLinkProcessEvent>(
						ManagedObjectHandlerLinkProcessEvent.values()) {
					@Override
					protected void handlePropertyChange(
							ManagedObjectHandlerLinkProcessEvent property,
							PropertyChangeEvent evt) {
						switch (property) {
						case CHANGE_OFFICE_TASK:
							ManagedObjectHandlerLinkProcessEditPart.this
									.refreshSourceConnections();
							break;
						}
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		// Create the figure
		IFigure figure = new Label(this.getCastedModel().getLinkProcessId());
		figure.setForegroundColor(ColorConstants.red);

		// Return the figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			@Override
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				try {

					// Obtain the office task
					OfficeTaskModel task;
					if (target instanceof OfficeTaskModel) {
						// Office task specified
						task = (OfficeTaskModel) target;

					} else if (target instanceof OfficeFloorOfficeModel) {
						// Obtain the task from the office
						OfficeFloorOfficeModel office = (OfficeFloorOfficeModel) target;

						// Obtain the project
						IProject project = ProjectConfigurationContext
								.getProject(ManagedObjectHandlerLinkProcessEditPart.this
										.getEditor().getEditorInput());

						// Obtain the task of the office to link
						OfficeTaskDialog dialog = new OfficeTaskDialog(
								ManagedObjectHandlerLinkProcessEditPart.this
										.getEditor().getEditorSite().getShell(),
								office, project);
						task = dialog.createOfficeTask();
						if (task == null) {
							// No task selected, so do not connect
							return null;
						}

						// Add the task to the office, so may connect
						office.addTask(task);

					} else {
						// Unknown type selected, so do not connect
						return null;
					}

					// Create the connection to the task
					LinkProcessToOfficeTaskModel conn = new LinkProcessToOfficeTaskModel();
					conn
							.setLinkProcess((ManagedObjectHandlerLinkProcessModel) source);
					conn.setOfficeTask(task);
					conn.connect();

					// Return the connection
					return conn;

				} catch (Exception ex) {
					// Indicate error and do no create connection
					ManagedObjectHandlerLinkProcessEditPart.this
							.messageError(ex);
					return null;
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#populateConnectionTargetTypes(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(OfficeTaskModel.class);
		types.add(OfficeFloorOfficeModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		LinkProcessToOfficeTaskModel conn = this.getCastedModel()
				.getOfficeTask();
		if (conn != null) {
			models.add(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Never a target
	}

}
