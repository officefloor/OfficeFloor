/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.conform.figures;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.model.conform.ExistingItemModel;
import net.officefloor.model.conform.ExistingItemToTargetItemModel;
import net.officefloor.model.conform.TargetItemModel;

import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.ToggleButton;
import org.eclipse.swt.SWT;

/**
 * {@link OfficeFloorFigure} for the {@link ExistingItemModel} or
 * {@link TargetItemModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TargetConformModelItemFigure extends AbstractOfficeFloorFigure {

	/**
	 * {@link LabelConnectorFigure} displaying the item name and connector.
	 */
	private final LabelConnectorFigure label;

	/**
	 * {@link ToggleButton} indicating whether to inherit configuration.
	 */
	private final ToggleButton inherit;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link TargetConformModelItemFigureContext}.
	 */
	public TargetConformModelItemFigure(
			final TargetConformModelItemFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(2);
		layout.horizontalSpacing = 2; // Provide some spacing for button
		figure.setLayoutManager(layout);

		// Create the connector figure
		String itemName = context.getTargetItemName();
		this.label = new LabelConnectorFigure(itemName,
				ConnectorDirection.WEST, ColorConstants.black);
		figure.add(this.label);
		layout.setConstraint(this.label, new GridData(SWT.RIGHT, SWT.CENTER,
				false, false));

		// Register the anchor for the connection
		this.registerConnectionAnchor(ExistingItemToTargetItemModel.class,
				this.label.getConnectionAnchor());

		// Add the inherit button
		this.inherit = new ToggleButton("Inherit");
		figure.add(this.inherit);
		layout.setConstraint(this.inherit, new GridData(SWT.RIGHT, SWT.CENTER,
				false, false));
		this.inherit.addChangeListener(new ChangeListener() {
			@Override
			public void handleStateChanged(ChangeEvent event) {
				context.setInherit(TargetConformModelItemFigure.this.inherit
						.isSelected());
			}
		});

		// Provide details of inheritance
		this.setInheritable(context.isInheritable());
		this.setInherit(context.isInherit());

		// Add layout for this figure
		context.setLayoutConstraint(figure, new GridData(SWT.RIGHT, SWT.CENTER,
				false, false));

		// Specify the figure
		this.setFigure(figure);
	}

	/**
	 * Specifies the new item name to display.
	 * 
	 * @param itemName
	 *            New item name to display.
	 */
	public void setItemName(String itemName) {
		this.label.getLabel().setText(itemName);
	}

	/**
	 * Specifies whether the item may inherit its configuration.
	 * 
	 * @param isInheritable
	 *            <code>true</code> if the item may inherit its configuration.
	 */
	public void setInheritable(boolean isInheritable) {
		this.inherit.setVisible(isInheritable);
	}

	/**
	 * Specifies whether to display inheriting.
	 * 
	 * @param isInherit
	 *            <code>true</code> if to display inheriting.
	 */
	public void setInherit(boolean isInherit) {

		// Flag whether to display inherited
		if (isInherit != this.inherit.isSelected()) {
			this.inherit.setSelected(isInherit);
		}

		// Specify whether connector visible for configuring
		this.label.setConnectorVisible(!isInherit);
	}

}