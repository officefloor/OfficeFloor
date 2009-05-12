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
package net.officefloor.eclipse.office.editparts;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectToExternalManagedObjectFigureContext;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the
 * {@link OfficeSectionObjectToExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class OfficeSectionObjectToExternalManagedObjectEditPart
		extends
		AbstractOfficeFloorConnectionEditPart<OfficeSectionObjectToExternalManagedObjectModel>
		implements OfficeSectionObjectToExternalManagedObjectFigureContext {

	/*
	 * ============ AbstractOfficeFloorConnectionEditPart =====================
	 */

	@Override
	protected void decorateFigure(PolylineConnection figure) {
		OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.decorateOfficeSectionObjectToExternalManagedObjectFigure(
						figure, this);
	}

}