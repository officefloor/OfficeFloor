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
package net.officefloor.eclipse.desk.figure;

import net.officefloor.eclipse.common.figure.ListItemFigure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link Figure} for the {@link net.officefloor.model.desk.DeskTaskObjectModel}.
 * 
 * @author Daniel
 */
public class DeskTaskObjectFigure extends Figure {

	/**
	 * Initiate.
	 * 
	 * @param objectName
	 *            Name of object.
	 * @param figureForIsParameter
	 *            {@link IFigure} to indicate if a parameter.
	 */
	public DeskTaskObjectFigure(String objectName, IFigure figureForIsParameter) {
		this.setLayoutManager(new ToolbarLayout(true));

		// Tool tip being full type name
		this.setToolTip(new Label(objectName));

		// Display only short name
		this.add(new ListItemFigure(this.getShortTypeName(objectName)));
		this.add(figureForIsParameter);
	}

	/**
	 * Obtains the short type name from the input type.
	 * 
	 * @param typeName
	 *            Type name.
	 * @return Short type name.
	 */
	private String getShortTypeName(String typeName) {
		// Obtain index of '.'
		int dotIndex = typeName.lastIndexOf('.');
		if (dotIndex < 0) {
			// Already short name
			return typeName;
		} else {
			// Return calculated short name (+1 to ignore '.')
			return typeName.substring((dotIndex + 1));
		}
	}
}
