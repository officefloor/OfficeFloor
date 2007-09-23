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
package net.officefloor.eclipse.common.figure;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;

/**
 * {@link org.eclipse.draw2d.Figure} for item within a list.
 * 
 * @author Daniel
 */
public class ListItemFigure extends Label {

	/**
	 * Initiate.
	 * 
	 * @param text
	 *            Text for the item.
	 */
	public ListItemFigure(String text) {
		super(text);
		
		// Align text to the left
		this.setLabelAlignment(PositionConstants.LEFT);
	}

}
