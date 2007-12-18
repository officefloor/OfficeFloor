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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.ManagedObjectSourceCreateDialog;
import net.officefloor.eclipse.common.dialog.TeamCreateDialog;
import net.officefloor.eclipse.common.dialog.input.ClasspathResourceSelectionPropertyInput;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.ButtonEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.eclipse.common.wrap.OfficeFloorWrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.eclipse.desk.figure.SectionFigure;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.model.officefloor.OfficeFloorModel.OfficeFloorEvent;
import net.officefloor.officefloor.OfficeFloorLoader;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.geometry.Point;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.officefloor.OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorEditPart extends
		AbstractOfficeFloorDiagramEditPart<OfficeFloorModel> {

	/**
	 * Adds the {@link net.officefloor.model.officefloor.OfficeFloorOfficeModel}.
	 */
	private WrappingModel<OfficeFloorModel> addOffice;

	/**
	 * Listing of {@link TeamModel} instances.
	 */
	private WrappingModel<OfficeFloorModel> teams;

	/**
	 * Listing of
	 * {@link net.officefloor.model.officefloor.ManagedObjectSourceModel}
	 * instances.
	 */
	private WrappingModel<OfficeFloorModel> managedObjectSources;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#init()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void init() {

		// Button to add an Office
		final ButtonEditPart addOfficeButton = new ButtonEditPart("Add Office") {
			@Override
			protected void handleButtonClick() {
				// Add the Office
				OfficeFloorOfficeModel officeBean = new OfficeFloorOfficeModel();
				BeanDialog dialog = OfficeFloorEditPart.this.createBeanDialog(
						officeBean, "X", "Y");
				dialog
						.registerPropertyInputBuilder("Id",
								new ClasspathResourceSelectionPropertyInput(
										OfficeFloorEditPart.this.getEditor(),
										"office"));
				if (dialog.populate()) {
					try {
						// Obtain the office configuration
						ProjectClassLoader classLoader = ProjectClassLoader
								.create(OfficeFloorEditPart.this.getEditor());
						ConfigurationItem officeConfigItem = classLoader
								.findConfigurationItem(officeBean.getId());
						if (officeConfigItem == null) {
							OfficeFloorEditPart.this
									.messageError("Could not find Office at '"
											+ officeBean.getId() + "'");
							return;
						}

						// Load the Office
						OfficeFloorLoader officeFloorLoader = new OfficeFloorLoader();
						OfficeFloorOfficeModel office = officeFloorLoader
								.loadOfficeFloorOffice(officeConfigItem);
						
						// Specify name of office
						office.setName(officeBean.getName());

						// Set initial location of the office
						office.setX(200);
						office.setY(200);

						// Add the Office
						OfficeFloorEditPart.this.getCastedModel().addOffice(
								office);

					} catch (Exception ex) {
						OfficeFloorEditPart.this.messageError(ex);
					}
				}
			}
		};

		// Add Office
		WrappingEditPart addOfficeEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.add(addOfficeButton);
			}
		};
		addOfficeEditPart.setFigure(new FreeformWrapperFigure(
				new SectionFigure("Add Office")));
		this.addOffice = new WrappingModel<OfficeFloorModel>(this
				.getCastedModel(), addOfficeEditPart, new Point(10, 10));

		// Button to add a Team
		final ButtonEditPart teamButton = new ButtonEditPart("Add Team") {
			@Override
			protected void handleButtonClick() {
				try {
					// Create the Team
					AbstractOfficeFloorEditor editor = OfficeFloorEditPart.this
							.getEditor();
					IProject project = ProjectConfigurationContext
							.getProject(editor.getEditorInput());
					TeamCreateDialog dialog = new TeamCreateDialog(editor
							.getSite().getShell(), project);
					TeamModel team = dialog.createTeam();

					// Add team if created
					if (team != null) {
						OfficeFloorEditPart.this.getCastedModel().addTeam(team);
					}
				} catch (Exception ex) {
					OfficeFloorEditPart.this.messageError(ex);
				}
			}
		};

		// Teams
		WrappingEditPart teamEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(OfficeFloorEditPart.this.getCastedModel()
						.getTeams());
				children.add(teamButton);
			}
		};
		teamEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Add Team")));
		this.teams = new WrappingModel<OfficeFloorModel>(this.getCastedModel(),
				teamEditPart, new Point(10, 80));

		// Button to add a Managed Object Source
		final ButtonEditPart mosButton = new ButtonEditPart("Add MO") {
			@Override
			protected void handleButtonClick() {
				try {
					// Create the Managed Object Source
					AbstractOfficeFloorEditor editor = OfficeFloorEditPart.this
							.getEditor();
					IProject project = ProjectConfigurationContext
							.getProject(editor.getEditorInput());
					ManagedObjectSourceCreateDialog dialog = new ManagedObjectSourceCreateDialog(
							editor.getSite().getShell(), project);
					ManagedObjectSourceModel managedObjectSource = dialog
							.createManagedObjectSource();

					// Add managed object source if created
					if (managedObjectSource != null) {
						OfficeFloorEditPart.this.getCastedModel()
								.addManagedObjectSource(managedObjectSource);
					}
				} catch (Exception ex) {
					OfficeFloorEditPart.this.messageError(ex);
				}
			}
		};

		// Managed Object Sources
		WrappingEditPart mosEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(OfficeFloorEditPart.this.getCastedModel()
						.getManagedObjectSources());
				children.add(mosButton);
			}
		};
		mosEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Add MO")));
		this.managedObjectSources = new WrappingModel<OfficeFloorModel>(this
				.getCastedModel(), mosEditPart, new Point(500, 10));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart#createLayoutEditPolicy()
	 */
	@Override
	protected OfficeFloorLayoutEditPolicy createLayoutEditPolicy() {
		return new OfficeFloorOfficeFloorLayoutEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart#populateChildren(java.util.List)
	 */
	@Override
	protected void populateChildren(List<Object> childModels) {
		// Add the Static children
		childModels.add(this.addOffice);
		childModels.add(this.managedObjectSources);
		childModels.add(this.teams);

		// Add dynamic children
		childModels.addAll(this.getCastedModel().getOffices());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeFloorEvent>(
				OfficeFloorEvent.values()) {
			@Override
			protected void handlePropertyChange(OfficeFloorEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_MANAGED_OBJECT_SOURCE:
				case REMOVE_MANAGED_OBJECT_SOURCE:
					OfficeFloorEditPart.this.managedObjectSources.getEditPart()
							.refresh();
					break;
				case ADD_TEAM:
				case REMOVE_TEAM:
					OfficeFloorEditPart.this.teams.getEditPart().refresh();
					break;
				case ADD_OFFICE:
				case REMOVE_OFFICE:
					OfficeFloorEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

}

/**
 * {@link org.eclipse.gef.editpolicies.LayoutEditPolicy} for the
 * {@link net.officefloor.model.officefloor.OfficeFloorModel}.
 */
class OfficeFloorOfficeFloorLayoutEditPolicy extends
		OfficeFloorLayoutEditPolicy<OfficeFloorModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy#createCreateComand(P,
	 *      java.lang.Object, org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	protected CreateCommand createCreateComand(OfficeFloorModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}