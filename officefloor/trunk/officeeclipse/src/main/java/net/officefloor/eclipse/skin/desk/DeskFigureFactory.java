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
package net.officefloor.eclipse.skin.desk;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemModel;

import org.eclipse.draw2d.IFigure;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link DeskModel}.
 * 
 * @author Daniel
 */
public interface DeskFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link DeskWorkModel}.
	 * 
	 * @param context
	 *            {@link DeskWorkFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createDeskWorkFigure(DeskWorkFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link DeskTaskModel}.
	 * 
	 * @param context
	 *            {@link DeskTaskFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createDeskTaskFigure(DeskTaskFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link DeskTaskObjectModel}
	 * .
	 * 
	 * @param context
	 *            {@link DeskTaskObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createDeskTaskObjectFigure(
			DeskTaskObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalEscalationFigureContext}.
	 * 
	 * @param context
	 *            {@link ExternalEscalationFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createExternalEscalationFigure(
			ExternalEscalationFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
	 * 
	 * @param context
	 *            {@link ExternalFlowModel}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createExternalFlowFigure(ExternalFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link FlowItemModel}.
	 * 
	 * @param context
	 *            {@link FlowItemFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createFlowItemFigure(FlowItemFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link FlowItemEscalationModel}.
	 * 
	 * @param context
	 *            {@link FlowItemEscalationFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createFlowItemEscalation(
			FlowItemEscalationFigureContext context);

	/**
	 * @param flowItemOutputEditPart
	 * @return
	 */
	OfficeFloorFigure createFlowItemOutputFigure(
			FlowItemOutputFigureContext context);
}
