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
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.section.SubSectionOutputFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;
import net.officefloor.model.section.SubSectionOutputModel.SubSectionOutputEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link SubSectionOutputModel}.
 * 
 * @author Daniel
 */
public class SubSectionOutputEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<SubSectionOutputModel, OfficeFloorFigure>
		implements SubSectionOutputFigureContext {

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				// TODO Handle always only connected to one type

				if (target instanceof SubSectionInputModel) {
					// Create the flow connection
					SubSectionOutputToSubSectionInputModel conn = new SubSectionOutputToSubSectionInputModel();
					conn.setSubSectionOutput((SubSectionOutputModel) source);
					conn.setSubSectionInput((SubSectionInputModel) target);
					conn.connect();
					return conn;

				} else if (target instanceof ExternalFlowModel) {
					// Create the external flow connection
					SubSectionOutputToExternalFlowModel conn = new SubSectionOutputToExternalFlowModel();
					conn.setSubSectionOutput((SubSectionOutputModel) source);
					conn.setExternalFlow((ExternalFlowModel) target);
					conn.connect();
					return conn;

				} else {
					throw new IllegalArgumentException("Unknown target '"
							+ target.getClass().getName()
							+ "' for "
							+ SubSectionOutputEditPart.this.getClass()
									.getName());
				}
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(SubSectionInputModel.class);
		types.add(ExternalFlowModel.class);
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
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<SubSectionOutputEvent>(
				SubSectionOutputEvent.values()) {
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
		});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createSubSectionOutputFigure(this);
	}

	/*
	 * ==================== SubSectionOutputFigureContext ==================
	 */

	@Override
	public String getSubSectionOutputName() {
		return this.getCastedModel().getSubSectionOutputName();
	}

}