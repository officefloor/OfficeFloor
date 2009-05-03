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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.DeskFigureFactory;
import net.officefloor.eclipse.skin.desk.WorkTaskFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskFigureContext;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigureContext;
import net.officefloor.eclipse.skin.desk.WorkFigure;
import net.officefloor.eclipse.skin.desk.WorkFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigure;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.TaskEscalationFigure;
import net.officefloor.eclipse.skin.desk.TaskEscalationFigureContext;
import net.officefloor.eclipse.skin.desk.TaskFigure;
import net.officefloor.eclipse.skin.desk.TaskFigureContext;
import net.officefloor.eclipse.skin.desk.TaskFlowFigure;
import net.officefloor.eclipse.skin.desk.TaskFlowFigureContext;

/**
 * Standard {@link DeskFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardDeskFigureFactory implements DeskFigureFactory {

	/*
	 * ===================== DeskFigureFactory ============================
	 */

	@Override
	public WorkFigure createWorkFigure(WorkFigureContext context) {
		return new StandardWorkFigure(context);
	}

	@Override
	public WorkTaskFigure createWorkTaskFigure(
			final WorkTaskFigureContext context) {
		return new StandardWorkTaskFigure(context);
	}

	@Override
	public net.officefloor.eclipse.skin.desk.WorkTaskObjectFigure createWorkTaskObjectFigure(
			final WorkTaskObjectFigureContext context) {
		return new StandardWorkTaskObjectFigure(context);
	}

	@Override
	public ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context) {
		return new StandardExternalFlowFigure(context);
	}

	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		return new StandardExternalManagedObjectFigure(context);
	}

	@Override
	public TaskFigure createTaskFigure(
			final TaskFigureContext context) {
		return new StandardTaskFigure(context);
	}

	@Override
	public TaskEscalationFigure createTaskEscalationFigure(
			TaskEscalationFigureContext context) {
		return new StandardTaskEscalationFigure(context);
	}

	@Override
	public TaskFlowFigure createTaskFlowFigure(
			TaskFlowFigureContext context) {
		return new StandardTaskFlowFigure(context);
	}

}