/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.section.SubSectionOutputFigureContext;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;
import net.officefloor.model.section.SubSectionOutputModel.SubSectionOutputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SubSectionOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionOutputEditPart
		extends
		AbstractOfficeFloorEditPart<SubSectionOutputModel, SubSectionOutputEvent, OfficeFloorFigure>
		implements SubSectionOutputFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory()
				.createSubSectionOutputFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		SubSectionOutputToSubSectionInputModel sourceInput = this
				.getCastedModel().getSubSectionInput();
		if (sourceInput != null) {
			models.add(sourceInput);
		}
		SubSectionOutputToExternalFlowModel sourceExternal = this
				.getCastedModel().getExternalFlow();
		if (sourceExternal != null) {
			models.add(sourceExternal);
		}
	}

	@Override
	protected Class<SubSectionOutputEvent> getPropertyChangeEventType() {
		return SubSectionOutputEvent.class;
	}

	@Override
	protected void handlePropertyChange(SubSectionOutputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_EXTERNAL_FLOW:
		case CHANGE_SUB_SECTION_INPUT:
			SubSectionOutputEditPart.this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ==================== SubSectionOutputFigureContext ==================
	 */

	@Override
	public String getSubSectionOutputName() {
		return this.getCastedModel().getSubSectionOutputName();
	}

}