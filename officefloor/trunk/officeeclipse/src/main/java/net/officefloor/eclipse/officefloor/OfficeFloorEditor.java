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
package net.officefloor.eclipse.officefloor;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.CommandFactory;
import net.officefloor.eclipse.common.editparts.FigureFactory;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectDependencyEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectHandlerEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectHandlerInstanceEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectHandlerLinkProcessEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectSourceEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectTaskEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectTaskFlowEditPart;
import net.officefloor.eclipse.officefloor.editparts.ManagedObjectTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.TeamEditPart;
import net.officefloor.model.officefloor.ManagedObjectDependencyModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectSourceToOfficeFloorOfficeModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel;
import net.officefloor.model.officefloor.ManagedObjectTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTeamModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeManagedObjectToManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.OfficeTeamToTeamModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.officefloor.OfficeFloorLoader;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;

/**
 * Editor for the {@link net.officefloor.model.officefloor.OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorEditor extends
		AbstractOfficeFloorEditor<OfficeFloorModel> {

	/**
	 * Initialise the specialised {@link FigureFactory} for the model types.
	 */
	static {
		// Managing Office of Managed Object
		OfficeFloorConnectionEditPart
				.registerFigureFactory(
						ManagedObjectSourceToOfficeFloorOfficeModel.class,
						new FigureFactory<ManagedObjectSourceToOfficeFloorOfficeModel>() {
							@Override
							public IFigure createFigure(
									ManagedObjectSourceToOfficeFloorOfficeModel model) {
								PolylineConnection figure = new PolylineConnection();
								figure
										.setForegroundColor(ColorConstants.lightGray);
								return figure;
							}
						});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#populateEditPartTypes(java.util.Map)
	 */
	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(OfficeFloorModel.class, OfficeFloorEditPart.class);
		map.put(ManagedObjectSourceModel.class,
				ManagedObjectSourceEditPart.class);
		map.put(TeamModel.class, TeamEditPart.class);
		map.put(OfficeFloorOfficeModel.class, OfficeEditPart.class);
		map.put(OfficeTeamModel.class, OfficeTeamEditPart.class);
		map.put(OfficeManagedObjectModel.class,
				OfficeManagedObjectEditPart.class);
		map.put(ManagedObjectDependencyModel.class,
				ManagedObjectDependencyEditPart.class);
		map.put(ManagedObjectHandlerModel.class,
				ManagedObjectHandlerEditPart.class);
		map.put(ManagedObjectHandlerInstanceModel.class,
				ManagedObjectHandlerInstanceEditPart.class);
		map.put(ManagedObjectHandlerLinkProcessModel.class,
				ManagedObjectHandlerLinkProcessEditPart.class);
		map.put(ManagedObjectTaskModel.class, ManagedObjectTaskEditPart.class);
		map.put(ManagedObjectTaskFlowModel.class,
				ManagedObjectTaskFlowEditPart.class);
		map.put(ManagedObjectTeamModel.class, ManagedObjectTeamEditPart.class);

		// Connections
		map.put(OfficeTeamToTeamModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(OfficeManagedObjectToManagedObjectSourceModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(ManagedObjectSourceToOfficeFloorOfficeModel.class,
				OfficeFloorConnectionEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#isDragTarget()
	 */
	@Override
	protected boolean isDragTarget() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#retrieveModel(net.officefloor.repository.ConfigurationItem)
	 */
	@Override
	protected OfficeFloorModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return this.getOfficeFloorLoader().loadOfficeFloor(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#storeModel(T,
	 *      net.officefloor.repository.ConfigurationItem)
	 */
	@Override
	protected void storeModel(OfficeFloorModel model,
			ConfigurationItem configuration) throws Exception {
		this.getOfficeFloorLoader().storeOfficeFloor(model, configuration);
	}

	/**
	 * Obtains the {@link OfficeFloorLoader}.
	 * 
	 * @return {@link OfficeFloorLoader}.
	 */
	private OfficeFloorLoader getOfficeFloorLoader() {
		return new OfficeFloorLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#populateCommandFactories(java.util.List)
	 */
	@Override
	protected void populateCommandFactories(
			List<CommandFactory<OfficeFloorModel>> list) {
		// No commands yet
	}
}
