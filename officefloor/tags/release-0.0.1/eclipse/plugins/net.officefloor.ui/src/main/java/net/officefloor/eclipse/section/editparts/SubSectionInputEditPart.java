/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.section.SubSectionInputFigure;
import net.officefloor.eclipse.skin.section.SubSectionInputFigureContext;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionInputModel.SubSectionInputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SubSectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionInputEditPart
		extends
		AbstractOfficeFloorEditPart<SubSectionInputModel, SubSectionInputEvent, SubSectionInputFigure>
		implements SubSectionInputFigureContext {

	@Override
	protected SubSectionInputFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory()
				.createSubSectionInputFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getSubSectionOutputs());
	}

	@Override
	protected Class<SubSectionInputEvent> getPropertyChangeEventType() {
		return SubSectionInputEvent.class;
	}

	@Override
	protected void handlePropertyChange(SubSectionInputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_IS_PUBLIC:
			SubSectionInputEditPart.this.getOfficeFloorFigure()
					.setIsPublic(
							SubSectionInputEditPart.this.getCastedModel()
									.getIsPublic());
			break;
		case ADD_SUB_SECTION_OUTPUT:
		case REMOVE_SUB_SECTION_OUTPUT:
			SubSectionInputEditPart.this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ================= SubSectionInputFigureContext =====================
	 */

	@Override
	public String getSubSectionInputName() {
		return this.getCastedModel().getSubSectionInputName();
	}

	@Override
	public boolean isPublic() {
		return this.getCastedModel().getIsPublic();
	}

	@Override
	public void setIsPublic(final boolean isPublic) {

		// Maintain current is public
		final boolean currentIsPublic = this.getCastedModel().getIsPublic();

		// Make change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				SubSectionInputEditPart.this.getCastedModel().setIsPublic(
						isPublic);
			}

			@Override
			protected void undoCommand() {
				SubSectionInputEditPart.this.getCastedModel().setIsPublic(
						currentIsPublic);
			}
		});
	}

}