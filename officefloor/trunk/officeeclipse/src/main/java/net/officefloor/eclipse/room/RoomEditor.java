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
package net.officefloor.eclipse.room;

import java.util.Map;

import net.officefloor.desk.DeskLoader;
import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.commands.TagFactory;
import net.officefloor.eclipse.common.editparts.FigureFactory;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.eclipse.room.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.room.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.room.editparts.RoomEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomInputFlowEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomManagedObjectEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomOutputFlowEditPart;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.ManagedObjectToExternalManagedObjectModel;
import net.officefloor.model.room.OutputFlowToExternalFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;
import net.officefloor.room.RoomLoader;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteGroup;

/**
 * Editor for the {@link net.officefloor.model.room.RoomModel}.
 * 
 * @author Daniel
 */
public class RoomEditor extends AbstractOfficeFloorEditor<RoomModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#populateEditPartTypes(java.util.Map)
	 */
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {
		// Entities
		map.put(RoomModel.class, RoomEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(ExternalFlowModel.class, ExternalFlowEditPart.class);
		map.put(SubRoomModel.class, SubRoomEditPart.class);
		map.put(SubRoomInputFlowModel.class, SubRoomInputFlowEditPart.class);
		map.put(SubRoomManagedObjectModel.class,
				SubRoomManagedObjectEditPart.class);
		map.put(SubRoomOutputFlowModel.class, SubRoomOutputFlowEditPart.class);

		// Connections
		map.put(ManagedObjectToExternalManagedObjectModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(OutputFlowToExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(OutputFlowToInputFlowModel.class,
				OfficeFloorConnectionEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#isDragTarget()
	 */
	protected boolean isDragTarget() {
		// Disallow as drag target
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#retrieveModel(net.officefloor.repository.ConfigurationItem)
	 */
	protected RoomModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		// Return the loaded Room
		return this.getRoomLoader().loadRoom(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#storeModel(T,
	 *      net.officefloor.repository.ConfigurationItem)
	 */
	protected void storeModel(RoomModel model, ConfigurationItem configuration)
			throws Exception {
		// Store the Room
		this.getRoomLoader().storeRoom(model, configuration);
	}

	/**
	 * Obtain the {@link RoomLoader}.
	 * 
	 * @return {@link RoomLoader}.
	 */
	private RoomLoader getRoomLoader() {
		return new RoomLoader(new ModelRepository());
	}

	/**
	 * Initiate the specialised
	 * {@link net.officefloor.eclipse.common.editparts.FigureFactory} instances
	 * for the model types.
	 */
	static {
		// Managed object
		OfficeFloorConnectionEditPart.registerFigureFactory(
				ManagedObjectToExternalManagedObjectModel.class,
				new FigureFactory<ManagedObjectToExternalManagedObjectModel>() {
					public IFigure createFigure(
							ManagedObjectToExternalManagedObjectModel model) {
						PolylineConnection figure = new PolylineConnection();
						figure.setForegroundColor(ColorConstants.darkGreen);
						return figure;
					}
				});

		// Create the Figure Factory for flow links
		FigureFactory<Object> linkFigureFactory = new FigureFactory<Object>() {
			public IFigure createFigure(Object model) {

				// Obtain the link type
				String linkType;
				if (model instanceof OutputFlowToInputFlowModel) {
					linkType = ((OutputFlowToInputFlowModel) model)
							.getLinkType();
				} else if (model instanceof OutputFlowToExternalFlowModel) {
					linkType = ((OutputFlowToExternalFlowModel) model)
							.getLinkType();
				} else {
					// Unknown model type
					throw new OfficeFloorPluginFailure("Unknown model type: "
							+ model.getClass().getName());
				}

				// Create link
				if (DeskLoader.SEQUENTIAL_LINK_TYPE.equals(linkType)) {
					PolylineConnection figure = new PolylineConnection();
					figure.setTargetDecoration(new PolygonDecoration());
					return figure;

				} else if (DeskLoader.PARALLEL_LINK_TYPE.equals(linkType)) {
					PolylineConnection figure = new PolylineConnection();
					figure.setTargetDecoration(new PolygonDecoration());
					figure.setSourceDecoration(new PolygonDecoration());
					return figure;

				} else if (DeskLoader.ASYNCHRONOUS_LINK_TYPE.equals(linkType)) {
					PolylineConnection figure = new PolylineConnection();
					figure.setTargetDecoration(new PolygonDecoration());
					figure.setLineStyle(Graphics.LINE_DASH);
					return figure;

				} else {
					PolylineConnection figure = new PolylineConnection();
					figure.setForegroundColor(ColorConstants.red);
					return figure;
				}
			}
		};

		// Room Output to Room Input
		OfficeFloorConnectionEditPart.registerFigureFactory(
				OutputFlowToInputFlowModel.class, linkFigureFactory);

		// Room Output to External Flow Item
		OfficeFloorConnectionEditPart.registerFigureFactory(
				OutputFlowToExternalFlowModel.class, linkFigureFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#initialisePaletteRoot()
	 */
	protected void initialisePaletteRoot() {
		// Add the link group
		PaletteGroup linkGroup = new PaletteGroup("Links");
		linkGroup.add(new ConnectionCreationToolEntry("Sequential",
				"sequential", new TagFactory(DeskLoader.SEQUENTIAL_LINK_TYPE),
				null, null));
		linkGroup.add(new ConnectionCreationToolEntry("Parallel", "parallel",
				new TagFactory(DeskLoader.PARALLEL_LINK_TYPE), null, null));
		linkGroup.add(new ConnectionCreationToolEntry("Asynchronous",
				"asynchronous", new TagFactory(
						DeskLoader.ASYNCHRONOUS_LINK_TYPE), null, null));
		this.paletteRoot.add(linkGroup);
	}

}
