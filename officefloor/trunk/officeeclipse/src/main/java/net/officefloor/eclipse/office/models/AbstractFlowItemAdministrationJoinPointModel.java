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

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.FlowItemModel;

/**
 * Links the {@link DutyModel} to the {@link FlowItemModel} administration join
 * point.
 * 
 * @author Daniel
 */
public abstract class AbstractFlowItemAdministrationJoinPointModel<C extends ConnectionModel>
		extends AbstractModel {

	/**
	 * {@link FlowItemModel}.
	 */
	private final FlowItemModel flowItem;

	/**
	 * Initiate.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 */
	public AbstractFlowItemAdministrationJoinPointModel(FlowItemModel flowItem) {
		this.flowItem = flowItem;
	}

	/**
	 * Obtains the {@link FlowItemModel}.
	 * 
	 * @return {@link FlowItemModel}.
	 */
	public FlowItemModel getFlowItem() {
		return this.flowItem;
	}

	/**
	 * Triggers a refresh on the connections.
	 */
	public void triggerRefreshConnections() {
		this.changeField("old", "new",
				FlowItemAdministrationJoinPointEvent.CHANGE_DUTIES);
	}

	/**
	 * Obtains the {@link ConnectionModel} instances to the {@link DutyModel}.
	 * 
	 * @return {@link ConnectionModel} instances to the {@link DutyModel}.
	 */
	public abstract List<C> getDutyConnections();

	/**
	 * Creates the {@link ConnectionModel} from the {@link FlowItemModel} to the
	 * {@link DutyModel}.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @param duty
	 *            {@link DutyModel}.
	 * @return {@link ConnectionModel} between {@link FlowItemModel} to the
	 *         {@link DutyModel}.
	 */
	public abstract ConnectionModel createDutyConnection(FlowItemModel flowItem,
			DutyModel duty);

}
