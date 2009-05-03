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
package net.officefloor.eclipse.room.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigure;
import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionInputModel.SubSectionInputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SubSectionInputModel}.
 * 
 * @author Daniel
 */
// TODO rename to SubSectionInputEditPart
public class SubRoomInputFlowEditPart
		extends
		AbstractOfficeFloorNodeEditPart<SubSectionInputModel, SubRoomInputFlowFigure>
		implements SubRoomInputFlowFigureContext {

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Not a source
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getSubSectionOutputs());
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<SubSectionInputEvent>(
				SubSectionInputEvent.values()) {
			@Override
			protected void handlePropertyChange(SubSectionInputEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_IS_PUBLIC:
					SubRoomInputFlowEditPart.this.getOfficeFloorFigure()
							.setIsPublic(
									SubRoomInputFlowEditPart.this
											.getCastedModel().getIsPublic());
					break;
				case ADD_SUB_SECTION_OUTPUT:
				case REMOVE_SUB_SECTION_OUTPUT:
					SubRoomInputFlowEditPart.this.refreshTargetConnections();
					break;
				}
			}
		});
	}

	@Override
	protected SubRoomInputFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createSubRoomInputFlowFigure(this);
	}

	/*
	 * ================= SubRoomInputFlowFigureContext =====================
	 */

	@Override
	public String getSubRoomInputFlowName() {
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
				SubRoomInputFlowEditPart.this.getCastedModel().setIsPublic(
						isPublic);
			}

			@Override
			protected void undoCommand() {
				SubRoomInputFlowEditPart.this.getCastedModel().setIsPublic(
						currentIsPublic);
			}
		});
	}

}