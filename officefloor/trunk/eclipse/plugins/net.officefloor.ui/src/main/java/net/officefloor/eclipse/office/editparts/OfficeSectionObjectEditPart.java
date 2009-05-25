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

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectModel.OfficeSectionObjectEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeSectionObjectModel}.
 * 
 * @author Daniel
 */
public class OfficeSectionObjectEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeSectionObjectModel, OfficeSectionObjectEvent, OfficeFloorFigure>
		implements OfficeSectionObjectFigureContext {

	/*
	 * ================== AbstractOfficeFloorEditPart =======================
	 */

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeSectionObjectFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getExternalManagedObject());
	}

	@Override
	protected Class<OfficeSectionObjectEvent> getPropertyChangeEventType() {
		return OfficeSectionObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeSectionObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_EXTERNAL_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ================== OfficeSectionObjectFigureContext ==================
	 */

	@Override
	public String getOfficeSectionObjectName() {
		return this.getCastedModel().getOfficeSectionObjectName();
	}

}