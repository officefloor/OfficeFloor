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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectDependencyFigureContext;
import net.officefloor.model.officefloor.ManagedObjectDependencyModel;
import net.officefloor.model.officefloor.ManagedObjectDependencyModel.ManagedObjectDependencyEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ManagedObjectDependencyModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectDependencyEditPart
		extends
		AbstractOfficeFloorEditPart<ManagedObjectDependencyModel, OfficeFloorFigure>
		implements ManagedObjectDependencyFigureContext {

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
		handlers.add(new PropertyChangeHandler<ManagedObjectDependencyEvent>(
				ManagedObjectDependencyEvent.values()) {
			@Override
			protected void handlePropertyChange(
					ManagedObjectDependencyEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				// TODO provide connection handling
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
				.createManagedObjectDependencyFigure(this);
	}

	/*
	 * ==================== ManagedObjectDependencyFigureContext ============
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.ManagedObjectDependencyFigureContext
	 * #getManagedObjectDependencyName()
	 */
	@Override
	public String getManagedObjectDependencyName() {
		return this.getCastedModel().getDependencyKey();
	}

}
