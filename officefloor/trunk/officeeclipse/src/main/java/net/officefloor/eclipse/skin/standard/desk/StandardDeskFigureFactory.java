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
import net.officefloor.eclipse.skin.desk.DeskTaskFigure;
import net.officefloor.eclipse.skin.desk.DeskTaskFigureContext;
import net.officefloor.eclipse.skin.desk.DeskTaskObjectFigureContext;
import net.officefloor.eclipse.skin.desk.DeskWorkFigure;
import net.officefloor.eclipse.skin.desk.DeskWorkFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigure;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemEscalationFigure;
import net.officefloor.eclipse.skin.desk.FlowItemEscalationFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemFigure;
import net.officefloor.eclipse.skin.desk.FlowItemFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemOutputFigure;
import net.officefloor.eclipse.skin.desk.FlowItemOutputFigureContext;

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
	public DeskWorkFigure createDeskWorkFigure(DeskWorkFigureContext context) {
		return new StandardDeskWorkFigure(context);
	}

	@Override
	public DeskTaskFigure createDeskTaskFigure(
			final DeskTaskFigureContext context) {
		return new StandardDeskTaskFigure(context);
	}

	@Override
	public net.officefloor.eclipse.skin.desk.DeskTaskObjectFigure createDeskTaskObjectFigure(
			final DeskTaskObjectFigureContext context) {
		return new StandardDeskTaskObjectFigure(context);
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
	public FlowItemFigure createFlowItemFigure(
			final FlowItemFigureContext context) {
		return new StandardFlowItemFigure(context);
	}

	@Override
	public FlowItemEscalationFigure createFlowItemEscalation(
			FlowItemEscalationFigureContext context) {
		return new StandardFlowItemEscalationFigure(context);
	}

	@Override
	public FlowItemOutputFigure createFlowItemOutputFigure(
			FlowItemOutputFigureContext context) {
		return new StandardFlowItemOutputFigure(context);
	}

}