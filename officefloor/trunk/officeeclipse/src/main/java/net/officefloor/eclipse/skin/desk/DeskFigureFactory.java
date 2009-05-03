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
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;

import org.eclipse.draw2d.IFigure;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link DeskModel}.
 * 
 * @author Daniel
 */
public interface DeskFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link WorkModel}.
	 * 
	 * @param context
	 *            {@link WorkFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	WorkFigure createWorkFigure(WorkFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link WorkTaskModel}.
	 * 
	 * @param context
	 *            {@link WorkTaskFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	WorkTaskFigure createWorkTaskFigure(WorkTaskFigureContext context);

	/**
	 * Creates {@link OfficeFloorFigure} for the {@link WorkTaskObjectModel}.
	 * 
	 * @param context
	 *            {@link WorkTaskObjectFigureContext}.
	 * @return {@link WorkTaskObjectFigure}.
	 */
	WorkTaskObjectFigure createWorkTaskObjectFigure(
			WorkTaskObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
	 * 
	 * @param context
	 *            {@link ExternalFlowModel}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link TaskModel}.
	 * 
	 * @param context
	 *            {@link TaskFigureContext}.
	 * @return {@link TaskFigure}.
	 */
	TaskFigure createTaskFigure(TaskFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link TaskFlowModel}.
	 * 
	 * @param context
	 *            {@link TaskFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	TaskFlowFigure createTaskFlowFigure(TaskFlowFigureContext context);

	/**
	 * Creates {@link OfficeFloorFigure} for the {@link TaskEscalationModel}.
	 * 
	 * @param context
	 *            {@link TaskEscalationFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	TaskEscalationFigure createTaskEscalationFigure(
			TaskEscalationFigureContext context);

}