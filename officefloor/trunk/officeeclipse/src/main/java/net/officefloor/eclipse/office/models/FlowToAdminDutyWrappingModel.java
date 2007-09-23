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

import net.officefloor.eclipse.common.wrap.WrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.model.office.FlowItemModel;

/**
 * {@link net.officefloor.eclipse.common.wrap.WrappingModel} to provide specific
 * type for connections.
 * 
 * @author Daniel
 */
public class FlowToAdminDutyWrappingModel extends WrappingModel<FlowItemModel> {

	/**
	 * Flag indicating if pre-admin (otherwise post-admin).
	 */
	private final boolean isPre;

	/**
	 * Initiate.
	 * 
	 * @param isPre
	 *            Flag indicating if pre-admin (otherwise post-admin).
	 * @param model
	 *            {@link FlowItemModel}.
	 * @param editPart
	 *            {@link WrappingEditPart}.
	 */
	public FlowToAdminDutyWrappingModel(boolean isPre, FlowItemModel model,
			WrappingEditPart editPart) {
		super(model, editPart);
		this.isPre = isPre;
	}

	/**
	 * Obtains the {@link FlowItemModel}.
	 * 
	 * @return {@link FlowItemModel}.
	 */
	public FlowItemModel getFlowItem() {
		return this.getWrappedModel();
	}

	/**
	 * Indicates if pre-admin.
	 * 
	 * @return <code>true</code> if pre-admin.
	 */
	public boolean isPreAdmin() {
		return this.isPre;
	}

	/**
	 * Indicates if post-admin.
	 * 
	 * @return <code>true</code> if post-admin.
	 */
	public boolean isPostAdmin() {
		return !this.isPre;
	}
}
