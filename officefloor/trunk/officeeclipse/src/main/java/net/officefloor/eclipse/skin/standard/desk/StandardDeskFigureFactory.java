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

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.DeskFigureFactory;
import net.officefloor.eclipse.skin.desk.DeskTaskFigure;
import net.officefloor.eclipse.skin.desk.DeskTaskFigureContext;
import net.officefloor.eclipse.skin.desk.DeskTaskObjectFigureContext;
import net.officefloor.eclipse.skin.desk.DeskWorkFigure;
import net.officefloor.eclipse.skin.desk.DeskWorkFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalEscalationFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemEscalationFigure;
import net.officefloor.eclipse.skin.desk.FlowItemEscalationFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemFigure;
import net.officefloor.eclipse.skin.desk.FlowItemFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemOutputFigure;
import net.officefloor.eclipse.skin.desk.FlowItemOutputFigureContext;
import net.officefloor.eclipse.skin.standard.OfficeFloorFigureImpl;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Standard {@link DeskFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardDeskFigureFactory implements DeskFigureFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.desk.skin.DeskFigureFactory#createDeskWorkFigure
	 * (net.officefloor.eclipse.desk.skin.DeskWorkFigureContext)
	 */
	@Override
	public DeskWorkFigure createDeskWorkFigure(DeskWorkFigureContext context) {
		return new StandardDeskWorkFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskFigureFactory#createDeskTaskFigure
	 * (net.officefloor.eclipse.skin.desk.DeskTaskFigureContext)
	 */
	@Override
	public DeskTaskFigure createDeskTaskFigure(
			final DeskTaskFigureContext context) {
		return new StandardDeskTaskFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.desk.DeskFigureFactory#
	 * createDeskTaskObjectFigure
	 * (net.officefloor.eclipse.skin.desk.DeskTaskObjectFigureContext)
	 */
	@Override
	public net.officefloor.eclipse.skin.desk.DeskTaskObjectFigure createDeskTaskObjectFigure(
			final DeskTaskObjectFigureContext context) {
		return new StandardDeskTaskObjectFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.desk.DeskFigureFactory#
	 * createExternalEscalationFigure
	 * (net.officefloor.eclipse.skin.desk.ExternalEscalationFigureContext)
	 */
	@Override
	public OfficeFloorFigure createExternalEscalationFigure(
			ExternalEscalationFigureContext context) {

		Label figure = new Label(context.getExternalEscalationName());
		figure.setBackgroundColor(ColorConstants.orange);
		figure.setOpaque(true);
		figure.setBounds(new Rectangle(140, 30, 120, 20));

		// Return figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskFigureFactory#createExternalFlowFigure
	 * (net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext)
	 */
	@Override
	public OfficeFloorFigure createExternalFlowFigure(
			ExternalFlowFigureContext context) {

		Label figure = new Label(context.getExternalFlowName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setBounds(new Rectangle(140, 30, 120, 20));

		// Return figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.desk.DeskFigureFactory#
	 * createExternalManagedObjectFigure
	 * (net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext)
	 */
	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		return new StandardExternalManagedObjectFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskFigureFactory#createFlowItemFigure
	 * (net.officefloor.eclipse.skin.desk.FlowItemFigureContext)
	 */
	@Override
	public FlowItemFigure createFlowItemFigure(
			final FlowItemFigureContext context) {
		return new StandardFlowItemFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskFigureFactory#createFlowItemEscalation
	 * (net.officefloor.eclipse.skin.desk.FlowItemEscalationFigureContext)
	 */
	@Override
	public FlowItemEscalationFigure createFlowItemEscalation(
			FlowItemEscalationFigureContext context) {
		return new StandardFlowItemEscalationFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.desk.DeskFigureFactory#
	 * createFlowItemOutputFigure
	 * (net.officefloor.eclipse.skin.desk.FlowItemOutputFigureContext)
	 */
	@Override
	public FlowItemOutputFigure createFlowItemOutputFigure(
			FlowItemOutputFigureContext context) {
		return new StandardFlowItemOutputFigure(context);
	}

}
