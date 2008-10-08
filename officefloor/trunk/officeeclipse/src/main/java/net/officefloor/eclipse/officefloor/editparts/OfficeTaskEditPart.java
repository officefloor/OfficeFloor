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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeTaskFigureContext;
import net.officefloor.model.officefloor.OfficeTaskModel;
import net.officefloor.model.officefloor.OfficeTaskModel.OfficeTaskEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeTaskModel}.
 * 
 * @author Daniel
 */
public class OfficeTaskEditPart extends
		AbstractOfficeFloorNodeEditPart<OfficeTaskModel> implements
		OfficeTaskFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeTaskEvent>(OfficeTaskEvent
				.values()) {
			@Override
			protected void handlePropertyChange(OfficeTaskEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_LINK_PROCESS:
				case REMOVE_LINK_PROCESS:
				case ADD_TASK_FLOW:
				case REMOVE_TASK_FLOW:
					OfficeTaskEditPart.this.refreshTargetConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeTaskFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Never a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getLinkProcesses());
		models.addAll(this.getCastedModel().getTaskFlows());
	}

	/*
	 * ====================== OfficeTaskFigureContext ====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.OfficeTaskFigureContext#getWorkName
	 * ()
	 */
	@Override
	public String getWorkName() {
		return this.getCastedModel().getWorkName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.OfficeTaskFigureContext#getTaskName
	 * ()
	 */
	@Override
	public String getTaskName() {
		return this.getCastedModel().getTaskName();
	}

}
