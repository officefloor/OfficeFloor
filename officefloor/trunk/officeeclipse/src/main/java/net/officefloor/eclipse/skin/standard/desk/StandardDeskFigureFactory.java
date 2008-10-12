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

import net.officefloor.eclipse.common.editparts.ButtonEditPart;
import net.officefloor.eclipse.common.editparts.CheckBoxEditPart;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.desk.figure.DeskTaskFigure;
import net.officefloor.eclipse.desk.figure.DeskTaskObjectFigure;
import net.officefloor.eclipse.desk.figure.DeskWorkFigure;
import net.officefloor.eclipse.desk.figure.FlowItemFigure;
import net.officefloor.eclipse.desk.figure.FlowItemOutputFigure;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.DeskFigureFactory;
import net.officefloor.eclipse.skin.desk.DeskTaskFigureContext;
import net.officefloor.eclipse.skin.desk.DeskTaskObjectFigureContext;
import net.officefloor.eclipse.skin.desk.DeskWorkFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalEscalationFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemEscalationFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemFigureContext;
import net.officefloor.eclipse.skin.desk.FlowItemOutputFigureContext;
import net.officefloor.eclipse.skin.standard.OfficeFloorFigureImpl;

import org.eclipse.draw2d.CheckBox;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FlowLayout;
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
	public OfficeFloorFigure createDeskWorkFigure(DeskWorkFigureContext context) {
		return new OfficeFloorFigureImpl(new DeskWorkFigure(context
				.getWorkName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskFigureFactory#createDeskTaskFigure
	 * (net.officefloor.eclipse.skin.desk.DeskTaskFigureContext)
	 */
	@Override
	public OfficeFloorFigure createDeskTaskFigure(
			final DeskTaskFigureContext context) {
		// Button to add as flow item
		final ButtonEditPart addAsFlowItem = new ButtonEditPart("Create") {
			protected void handleButtonClick() {
				context.createAsNewFlowItem();
			}
		};

		// Return the Desk Task figure
		return new OfficeFloorFigureImpl(new DeskTaskFigure(context
				.getTaskName(), addAsFlowItem.getFigure()));
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

		// Create the check box to indicate if a parameter
		CheckBoxEditPart parameterCheckBox = new CheckBoxEditPart(context
				.isParameter()) {
			protected void checkBoxStateChanged(boolean isChecked) {
				// Specify if parameter
				context.setIsParameter(isChecked);
			}
		};

		// Create and return the figure
		return new DeskTaskObjectFigureImpl(new DeskTaskObjectFigure(context
				.getObjectType(), parameterCheckBox.getFigure()),
				(CheckBox) parameterCheckBox.getFigure());
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
		figure.setBackgroundColor(ColorConstants.lightGray);
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
	public OfficeFloorFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {

		Label figure = new Label(context.getExternalManagedObjectName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setLayoutManager(new FlowLayout(true));

		// Return figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskFigureFactory#createFlowItemFigure
	 * (net.officefloor.eclipse.skin.desk.FlowItemFigureContext)
	 */
	@Override
	public net.officefloor.eclipse.skin.desk.FlowItemFigure createFlowItemFigure(
			final FlowItemFigureContext context) {

		// Create the check box to indicate if public
		CheckBoxEditPart publicCheckBox = new CheckBoxEditPart(context
				.isPublic()) {
			protected void checkBoxStateChanged(boolean isChecked) {
				// Specify if public
				context.setIsPublic(isChecked);
			}
		};

		// Create the figure
		FlowItemFigure figure = new FlowItemFigure(context.getFlowItemName(),
				publicCheckBox.getFigure());

		// Return the figure (useable as a freeform figure)
		return new FlowItemFigureImpl(new FreeformWrapperFigure(figure),
				(CheckBox) publicCheckBox.getFigure());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskFigureFactory#createFlowItemEscalation
	 * (net.officefloor.eclipse.skin.desk.FlowItemEscalationFigureContext)
	 */
	@Override
	public OfficeFloorFigure createFlowItemEscalation(
			FlowItemEscalationFigureContext context) {

		// Obtain simple name of escalation
		String escalationType = context.getEscalationType();
		String simpleType = escalationType;
		if (simpleType.indexOf('.') > 0) {
			simpleType = simpleType.substring(simpleType.lastIndexOf('.') + 1);
		}

		// Create the figure
		FlowItemOutputFigure figure = new FlowItemOutputFigure(simpleType);
		figure.setToolTip(new Label(escalationType));

		// Return the figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.desk.DeskFigureFactory#
	 * createFlowItemOutputFigure
	 * (net.officefloor.eclipse.skin.desk.FlowItemOutputFigureContext)
	 */
	@Override
	public OfficeFloorFigure createFlowItemOutputFigure(
			FlowItemOutputFigureContext context) {
		// Return the figure
		return new OfficeFloorFigureImpl(new FlowItemOutputFigure(context
				.getFlowItemOutputName()));
	}

}
