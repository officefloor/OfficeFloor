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
package net.officefloor.eclipse.skin.standard.officefloor;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MidpointLocator;
import org.eclipse.draw2d.PolylineConnection;

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link OfficeFloorManagedObjectSourceToDeployedOfficeFigure} implementation.
 * 
 * @author Daniel
 */
public class StandardOfficeFloorManagedObjectSourceToDeployedOfficeFigure
		implements OfficeFloorManagedObjectSourceToDeployedOfficeFigure {

	/**
	 * {@link Label} for the {@link ProcessState} bound name.
	 */
	private final Label processBoundName;

	/**
	 * Initiate.
	 * 
	 * @param figure
	 *            {@link PolylineConnection} to decorate.
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext}
	 */
	public StandardOfficeFloorManagedObjectSourceToDeployedOfficeFigure(
			PolylineConnection figure,
			OfficeFloorManagedObjectSourceToDeployedOfficeFigureContext context) {
		this.processBoundName = new Label();
		figure.add(this.processBoundName, new MidpointLocator(figure, 0));

		// Specify the process bound name
		this.setProcessBoundManagedObjectName(context
				.getProcessBoundManagedObjectName());
	}

	/*
	 * ========= OfficeFloorManagedObjectSourceToDeployedOfficeFigure ==========
	 */

	@Override
	public void setProcessBoundManagedObjectName(
			String processBoundManagedObjectName) {
		if (EclipseUtil.isBlank(processBoundManagedObjectName)) {
			this.processBoundName.setText("<not bound>");
		} else {
			this.processBoundName.setText(processBoundManagedObjectName);
		}
	}

	@Override
	public IFigure getProcessBoundManagedObjectNameFigure() {
		return this.processBoundName;
	}

}