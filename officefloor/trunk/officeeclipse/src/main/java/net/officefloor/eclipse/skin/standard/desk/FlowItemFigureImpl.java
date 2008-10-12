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

import org.eclipse.draw2d.CheckBox;
import org.eclipse.draw2d.IFigure;

import net.officefloor.eclipse.skin.desk.FlowItemFigure;
import net.officefloor.eclipse.skin.standard.OfficeFloorFigureImpl;

/**
 * {@link FlowItemFigure} implementation.
 * 
 * @author Daniel
 */
public class FlowItemFigureImpl extends OfficeFloorFigureImpl implements
		FlowItemFigure {

	/**
	 * Is public {@link CheckBox}.
	 */
	private final CheckBox checkBox;

	/**
	 * Initiate.
	 * 
	 * @param figure
	 *            {@link IFigure}.
	 * @param checkBox
	 *            Is public {@link CheckBox}.
	 */
	public FlowItemFigureImpl(IFigure figure, CheckBox checkBox) {
		super(figure);
		this.checkBox = checkBox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.FlowItemFigure#setIsPublic(boolean)
	 */
	@Override
	public void setIsPublic(boolean isPublic) {
		this.checkBox.setSelected(isPublic);
	}

}
