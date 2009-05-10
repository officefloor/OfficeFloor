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
package net.officefloor.eclipse.skin.section;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.section.ExternalFlowModel;

import org.eclipse.draw2d.IFigure;

/**
 * {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
 * 
 * @author Daniel
 */
public interface ExternalFlowFigure extends OfficeFloorFigure {

	/**
	 * Indicates a change in the {@link ExternalFlowModel} name.
	 * 
	 * @param externalFlowName
	 *            Name to display for the {@link ExternalFlowModel}.
	 */
	void setExternalFlowName(String externalFlowName);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the {@link ExternalFlowModel} name.
	 * <p>
	 * This is to allow placement of the editor in changing the {@link ExternalFlowModel}
	 * name.
	 * 
	 * @return {@link IFigure} containing the {@link ExternalFlowModel} name.
	 */
	IFigure getExternalFlowNameFigure();

}