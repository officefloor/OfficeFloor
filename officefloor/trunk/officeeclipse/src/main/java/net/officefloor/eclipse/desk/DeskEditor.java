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
package net.officefloor.eclipse.desk;

import java.util.List;
import java.util.Map;

import net.officefloor.LoaderContext;
import net.officefloor.desk.DeskLoader;
import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.CommandFactory;
import net.officefloor.eclipse.common.commands.TagFactory;
import net.officefloor.eclipse.common.editparts.FigureFactory;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.eclipse.desk.commands.RefreshWorkCommand;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskTaskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskTaskObjectEditPart;
import net.officefloor.eclipse.desk.editparts.DeskWorkEditPart;
import net.officefloor.eclipse.desk.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.desk.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.FlowItemEditPart;
import net.officefloor.eclipse.desk.editparts.FlowItemEscalationEditPart;
import net.officefloor.eclipse.desk.editparts.FlowItemOutputEditPart;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.DeskWorkToFlowItemModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.desk.FlowItemOutputToExternalFlowModel;
import net.officefloor.model.desk.FlowItemOutputToFlowItemModel;
import net.officefloor.model.desk.FlowItemToNextExternalFlowModel;
import net.officefloor.model.desk.FlowItemToNextFlowItemModel;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteGroup;

/**
 * Editor for the {@link net.officefloor.model.desk.DeskModel}.
 * 
 * @author Daniel
 */
public class DeskEditor extends AbstractOfficeFloorEditor<DeskModel> {

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
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#retrieveModel(net.officefloor.model.repository.ConfigurationItem)
	 */
	protected DeskModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		// Return the loaded Desk
		return this.getDeskLoader(configuration).loadDesk(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#storeModel(T,
	 *      net.officefloor.model.repository.ConfigurationItem)
	 */
	protected void storeModel(DeskModel model, ConfigurationItem configuration)
			throws Exception {
		// Store the Desk
		this.getDeskLoader(configuration).storeDesk(model, configuration);
	}

	/**
	 * Obtains the {@link DeskLoader}.
	 * 
	 * @return {@link DeskLoader}.
	 */
	private DeskLoader getDeskLoader(ConfigurationItem configurationItem) {
		return new DeskLoader(new LoaderContext(ProjectClassLoader
				.create(configurationItem.getContext())), new ModelRepository());
	}

	/**
	 * Initiate the specialised
	 * {@link net.officefloor.eclipse.common.editparts.FigureFactory} instances
	 * for the model types.
	 */
	static {
		// Initial flow of work
		OfficeFloorConnectionEditPart.registerFigureFactory(
				DeskWorkToFlowItemModel.class,
				new FigureFactory<DeskWorkToFlowItemModel>() {
					public IFigure createFigure(DeskWorkToFlowItemModel model) {
						PolylineConnection figure = new PolylineConnection();
						figure.setForegroundColor(ColorConstants.lightBlue);
						return figure;
					}
				});

		// Managed object
		OfficeFloorConnectionEditPart
				.registerFigureFactory(
						DeskTaskObjectToExternalManagedObjectModel.class,
						new FigureFactory<DeskTaskObjectToExternalManagedObjectModel>() {
							public IFigure createFigure(
									DeskTaskObjectToExternalManagedObjectModel model) {
								PolylineConnection figure = new PolylineConnection();
								figure
										.setForegroundColor(ColorConstants.darkGreen);
								return figure;
							}
						});

		// Task to Flow Item
		OfficeFloorConnectionEditPart.registerFigureFactory(
				DeskTaskToFlowItemModel.class,
				new FigureFactory<DeskTaskToFlowItemModel>() {
					public IFigure createFigure(DeskTaskToFlowItemModel model) {
						PolylineConnection figure = new PolylineConnection();
						figure.setForegroundColor(ColorConstants.lightGray);
						figure.setLineStyle(Graphics.LINE_DASH);
						return figure;
					}
				});

		// Create the Figure Factory for flow links
		FigureFactory<Object> linkFigureFactory = new FigureFactory<Object>() {
			public IFigure createFigure(Object model) {

				// Obtain the link type
				String linkType;
				if (model instanceof FlowItemOutputToFlowItemModel) {
					linkType = ((FlowItemOutputToFlowItemModel) model)
							.getLinkType();
				} else if (model instanceof FlowItemOutputToExternalFlowModel) {
					linkType = ((FlowItemOutputToExternalFlowModel) model)
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

		// Flow Item Output to Flow Item
		OfficeFloorConnectionEditPart.registerFigureFactory(
				FlowItemOutputToFlowItemModel.class, linkFigureFactory);

		// Flow Item Output to External Flow Item
		OfficeFloorConnectionEditPart.registerFigureFactory(
				FlowItemOutputToExternalFlowModel.class, linkFigureFactory);

		// Escalation handling
		OfficeFloorConnectionEditPart.registerFigureFactory(
				FlowItemEscalationToFlowItemModel.class,
				new FigureFactory<FlowItemEscalationToFlowItemModel>() {
					public IFigure createFigure(
							FlowItemEscalationToFlowItemModel model) {
						PolylineConnection figure = new PolylineConnection();
						figure.setTargetDecoration(new PolygonDecoration());
						figure.setForegroundColor(ColorConstants.lightGray);
						return figure;
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#populateEditPartTypes(java.util.Map)
	 */
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(DeskModel.class, DeskEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(DeskWorkModel.class, DeskWorkEditPart.class);
		map.put(DeskTaskModel.class, DeskTaskEditPart.class);
		map.put(DeskTaskObjectModel.class, DeskTaskObjectEditPart.class);
		map.put(FlowItemModel.class, FlowItemEditPart.class);
		map.put(FlowItemOutputModel.class, FlowItemOutputEditPart.class);
		map
				.put(FlowItemEscalationModel.class,
						FlowItemEscalationEditPart.class);
		map.put(ExternalFlowModel.class, ExternalFlowEditPart.class);

		// Connections
		map.put(DeskTaskObjectToExternalManagedObjectModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(FlowItemOutputToFlowItemModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(FlowItemOutputToExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(DeskTaskToFlowItemModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(DeskWorkToFlowItemModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(FlowItemToNextFlowItemModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(FlowItemToNextExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(FlowItemEscalationToFlowItemModel.class,
				OfficeFloorConnectionEditPart.class);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.AbstractOfficeFloorEditor#populateCommandFactories(java.util.List)
	 */
	@Override
	protected void populateCommandFactories(List<CommandFactory<DeskModel>> list) {
		list.add(new RefreshWorkCommand("Refresh Work",
				ProjectConfigurationContext.getProject(this.getEditorInput())));
	}

}
