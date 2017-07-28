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
package net.officefloor.eclipse.skin.standard.figure;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.graphics.Color;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} {@link Figure}.
 * 
 * @author Daniel Sagenschneider
 */
public class EllipseFigure extends Ellipse {

	/**
	 * {@link Label} for the name.
	 */
	private final Label name;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param colour
	 *            {@link Color} of the {@link Figure}.
	 */
	public EllipseFigure(String name, Color colour) {

		// Ellipse
		this.setBackgroundColor(colour);
		this.setOpaque(true);
		this.setLayoutManager(new FlowLayout());

		// Provide name
		this.name = new Label(name);
		this.name.setLayoutManager(new FlowLayout());
		this.name.setBorder(new MarginBorder(5));
		this.add(this.name);
	}

	/**
	 * Obtains the {@link Label} containing the {@link ManagedObject} name.
	 * 
	 * @return {@link Label} containing the {@link ManagedObject} name.
	 */
	public Label getLabel() {
		return this.name;
	}

	/**
	 * Specifies the name.
	 * 
	 * @param name
	 *            Name.
	 */
	public void setName(String name) {
		this.name.setText(name);
	}

}