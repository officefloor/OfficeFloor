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
package net.officefloor.eclipse.office.models;

import java.util.List;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.FlowItemToPreAdministratorDutyModel;

/**
 * Pre {@link AbstractFlowItemAdministrationJoinPointModel}.
 * 
 * @author Daniel
 */
public class PreFlowItemAdministrationJointPointModel
		extends
		AbstractFlowItemAdministrationJoinPointModel<FlowItemToPreAdministratorDutyModel> {

	/**
	 * Initiate.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 */
	public PreFlowItemAdministrationJointPointModel(FlowItemModel flowItem) {
		super(flowItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.office.models.
	 * AbstractFlowItemAdministrationJoinPointModel
	 * #createDutyConnection(net.officefloor.model.office.FlowItemModel,
	 * net.officefloor.model.office.DutyModel)
	 */
	@Override
	public ConnectionModel createDutyConnection(FlowItemModel flowItem,
			DutyModel duty) {
		FlowItemToPreAdministratorDutyModel connection = new FlowItemToPreAdministratorDutyModel();
		connection.setFlowItem(flowItem);
		connection.setDuty(duty);
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.office.models.
	 * AbstractFlowItemAdministrationJoinPointModel#getDutyConnections()
	 */
	@Override
	public List<FlowItemToPreAdministratorDutyModel> getDutyConnections() {
		return this.getFlowItem().getPreAdminDutys();
	}

}
