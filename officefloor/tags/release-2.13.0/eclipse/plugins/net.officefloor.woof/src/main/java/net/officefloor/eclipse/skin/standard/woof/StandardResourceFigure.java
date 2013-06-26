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
package net.officefloor.eclipse.skin.standard.woof;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.graphics.Color;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.EllipseFigure;
import net.officefloor.eclipse.skin.woof.ResourceFigure;
import net.officefloor.eclipse.skin.woof.ResourceFigureContext;

/**
 * Standard {@link ResourceFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardResourceFigure extends AbstractOfficeFloorFigure implements
		ResourceFigure {

	/**
	 * {@link ResourceFigureContext}.
	 */
	private final ResourceFigureContext context;

	/**
	 * Name.
	 */
	private final Label name;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ResourceFigureContext}.
	 */
	public StandardResourceFigure(ResourceFigureContext context) {
		this.context = context;
		EllipseFigure figure = new EllipseFigure(this.getDisplayName(),
				new Color(null, 229, 229, 229));
		figure.setOutline(false);
		this.name = figure.getLabel();
		this.setFigure(figure);
	}

	/**
	 * Obtains the display name.
	 * 
	 * @return Display name.
	 */
	private String getDisplayName() {

		// Determine if resource path
		String resourceName = this.context.getResourceName();
		boolean isPath = (resourceName.equals(this.context.getResourcePath()));

		// Return based on whether resource path
		return (isPath ? "" : "[") + resourceName + (isPath ? "" : "]");
	}

	/*
	 * ======================= ResourceFigure ===============================
	 */

	@Override
	public void setResourcePath(String resourcePath) {
		this.name.setText(this.getDisplayName());
	}

	@Override
	public IFigure getResourcePathFigure() {
		return this.name;
	}

}