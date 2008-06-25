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

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.eclipse.common.figure.WrappingFigure;
import net.officefloor.eclipse.common.wrap.OfficeFloorNodeWrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.eclipse.office.models.FlowToAdminDutyWrappingModel;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.FlowItemToTeamModel;
import net.officefloor.model.office.FlowItemModel.FlowItemEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.office.FlowItemModel}.
 * 
 * @author Daniel
 */
public class FlowItemEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<FlowItemModel> {

	/**
	 * Pre-admin {@link EditPart}.
	 */
	private OfficeFloorNodeWrappingEditPart preAdminEditPart;

	/**
	 * Pre-admin {@link Model}.
	 */
	private WrappingModel<FlowItemModel> preAdminModel;

	/**
	 * Pre-admin {@link EditPart}.
	 */
	private OfficeFloorNodeWrappingEditPart postAdminEditPart;

	/**
	 * Post-admin {@link Model}.
	 */
	private WrappingModel<FlowItemModel> postAdminModel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#init()
	 */
	@Override
	protected void init() {
		// Create the pre admin model
		this.preAdminEditPart = new OfficeFloorNodeWrappingEditPart() {
			@Override
			protected void populateConnectionTargetModels(List<Object> models) {
				models.addAll(FlowItemEditPart.this.getCastedModel()
						.getPreAdminDutys());
			}
		};
		this.preAdminEditPart.setFigure(new ListItemFigure("PRE"));
		this.preAdminModel = new FlowToAdminDutyWrappingModel(true, this
				.getCastedModel(), this.preAdminEditPart);

		// Create the post admin model
		this.postAdminEditPart = new OfficeFloorNodeWrappingEditPart() {
			@Override
			protected void populateConnectionTargetModels(List<Object> models) {
				models.addAll(FlowItemEditPart.this.getCastedModel()
						.getPostAdminDutys());
			}
		};
		this.postAdminEditPart.setFigure(new ListItemFigure("POST"));
		this.postAdminModel = new FlowToAdminDutyWrappingModel(false, this
				.getCastedModel(), this.postAdminEditPart);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		// Add static children
		childModels.add(this.preAdminModel);
		childModels.add(this.postAdminModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<FlowItemEvent>(FlowItemEvent
				.values()) {
			@Override
			protected void handlePropertyChange(FlowItemEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_PRE_ADMIN_DUTY:
				case REMOVE_PRE_ADMIN_DUTY:
					FlowItemEditPart.this.preAdminEditPart.refreshTargetConnections();
					break;
				case ADD_POST_ADMIN_DUTY:
				case REMOVE_POST_ADMIN_DUTY:
					FlowItemEditPart.this.postAdminEditPart.refreshTargetConnections();
					break;
				case CHANGE_TEAM:
					FlowItemEditPart.this.refreshSourceConnections();
					break;
				case ADD_DUTY_FLOW:
				case REMOVE_DUTY_FLOW:
					FlowItemEditPart.this.refreshTargetConnections();
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
		WrappingFigure figure = new WrappingFigure(new ListFigure());
		figure.addDecorate(new ListItemFigure(this.getCastedModel().getId()));
		figure.setLayoutManager(new ToolbarLayout(true));
		figure.addChildContainerFigure();

		// Return the figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		FlowItemToTeamModel team = this.getCastedModel().getTeam();
		if (team != null) {
			models.add(team);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getDutyFlows());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				FlowItemToTeamModel conn = new FlowItemToTeamModel();
				conn.setFlowItem((FlowItemModel) source);
				conn.setTeam((ExternalTeamModel) target);
				conn.connect();
				return conn;
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
		types.add(ExternalTeamModel.class);
	}

}
