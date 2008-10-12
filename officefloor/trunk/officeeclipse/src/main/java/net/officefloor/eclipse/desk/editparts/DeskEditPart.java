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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.CommandFactoryUtil;
import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.DeskWorkCreateDialog;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.ButtonEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.eclipse.common.wrap.OfficeFloorWrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.eclipse.desk.commands.AddExternalManagedObjectCommandFactory;
import net.officefloor.eclipse.desk.figure.SectionFigure;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.DeskModel.DeskEvent;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.geometry.Point;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.DeskModel}.
 * 
 * @author Daniel
 */
public class DeskEditPart extends AbstractOfficeFloorDiagramEditPart<DeskModel> {

	/**
	 * Listing of {@link net.officefloor.model.desk.ExternalManagedObjectModel}
	 * instances.
	 */
	private WrappingModel<DeskModel> externalManagedObjects;

	/**
	 * Listing of {@link net.officefloor.model.desk.DeskWorkModel} instances.
	 */
	private WrappingModel<DeskModel> works;

	/**
	 * Listing of {@link net.officefloor.model.desk.ExternalFlowModel}
	 * instances.
	 */
	private WrappingModel<DeskModel> externalFlowItems;

	/**
	 * Listing of {@link ExternalEscalationModel} instances.
	 */
	private WrappingModel<DeskModel> externalEscalations;

	/**
	 * Obtains the unique {@link FlowItemModel} id for the input
	 * {@link FlowItemModel}.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @return Unique {@link FlowItemModel} id.
	 */
	public String getUniqueFlowItemId(FlowItemModel flowItem) {

		// Create the set of existing flow items
		Map<String, FlowItemModel> existingFlowItems = new HashMap<String, FlowItemModel>();
		for (FlowItemModel existingFlowItem : this.getCastedModel()
				.getFlowItems()) {
			existingFlowItems.put(existingFlowItem.getId(), existingFlowItem);
		}

		// Obtain the unique id
		String suffix = "";
		int index = 0;
		for (;;) {
			// Try for uniqueness
			String uniqueIdAttempt = flowItem.getTaskName() + suffix;

			// Determine if unique (not registered or same flow item)
			FlowItemModel existingFlowItem = existingFlowItems
					.get(uniqueIdAttempt);
			if ((existingFlowItem == null) || (existingFlowItem == flowItem)) {
				return uniqueIdAttempt;
			}

			// Setup for next attempt
			index++;
			suffix = String.valueOf(index);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * init()
	 */
	@SuppressWarnings("unchecked")
	protected void init() {

		// Button to add External Managed Objects
		final ButtonEditPart extMoButton = new ButtonEditPart("Add Ext MO") {
			protected void handleButtonClick() {
				// Add the external managed object
				CommandFactoryUtil.execute(DeskEditPart.this,
						new AddExternalManagedObjectCommandFactory(
								DeskEditPart.this));
			}
		};

		// External Managed Objects
		WrappingEditPart extMoEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(DeskEditPart.this.getCastedModel()
						.getExternalManagedObjects());
				children.add(extMoButton);
			}
		};
		extMoEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Managed Objects")));
		this.externalManagedObjects = new WrappingModel<DeskModel>(this
				.getCastedModel(), extMoEditPart, new Point(10, 10));

		// Button to add Work
		final ButtonEditPart workButton = new ButtonEditPart("Add Work") {
			protected void handleButtonClick() {
				try {
					// Add the Desk Work
					AbstractOfficeFloorEditor editor = DeskEditPart.this
							.getEditor();
					IProject project = ProjectConfigurationContext
							.getProject(editor.getEditorInput());
					DeskWorkModel deskWork = new DeskWorkCreateDialog(editor
							.getSite().getShell(), project).createDeskWork();
					if (deskWork == null) {
						// Cancelled creation of work
						return;
					}

					// Add the work
					DeskEditPart.this.getCastedModel().addWork(deskWork);

				} catch (Exception ex) {
					DeskEditPart.this.messageError(ex);
				}
			}
		};

		// Work
		WrappingEditPart workEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(DeskEditPart.this.getCastedModel().getWorks());
				children.add(workButton);
			}
		};
		workEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Work")));
		this.works = new WrappingModel<DeskModel>(this.getCastedModel(),
				workEditPart, new Point(110, 10));

		// Button to add External Flows
		final ButtonEditPart extFlowButton = new ButtonEditPart("Add Ext Flow") {
			protected void handleButtonClick() {
				// Add the populated External Flow
				ExternalFlowModel flow = new ExternalFlowModel();
				BeanDialog dialog = DeskEditPart.this.createBeanDialog(flow,
						"X", "Y");
				if (dialog.populate()) {
					DeskEditPart.this.getCastedModel().addExternalFlow(flow);
				}
			}
		};

		// External flow items
		WrappingEditPart flowItemsEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(DeskEditPart.this.getCastedModel()
						.getExternalFlows());
				children.add(extFlowButton);
			}
		};
		flowItemsEditPart.setFigure(new FreeformWrapperFigure(
				new SectionFigure("Flow Items")));
		this.externalFlowItems = new WrappingModel<DeskModel>(this
				.getCastedModel(), flowItemsEditPart, new Point(510, 10));

		// Button to add External Escalation
		final ButtonEditPart extEscalationButton = new ButtonEditPart(
				"Add Ext Escalation") {
			@Override
			protected void handleButtonClick() {
				// Add the populated External Escalation
				ExternalEscalationModel escalation = new ExternalEscalationModel();
				BeanDialog dialog = DeskEditPart.this.createBeanDialog(
						escalation, "Escalation Type", "X", "Y");
				if (dialog.populate()) {
					DeskEditPart.this.getCastedModel().addExternalEscalation(
							escalation);
				}
			}
		};

		// External escalations
		WrappingEditPart escalationsEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List<Object> children) {
				children.addAll(DeskEditPart.this.getCastedModel()
						.getExternalEscalations());
				children.add(extEscalationButton);
			}
		};
		escalationsEditPart.setFigure(new FreeformWrapperFigure(
				new SectionFigure("Escalations")));
		this.externalEscalations = new WrappingModel<DeskModel>(this
				.getCastedModel(), escalationsEditPart, new Point(510, 300));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #createLayoutEditPolicy()
	 */
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new DeskLayoutEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #populateChildren(java.util.List)
	 */
	protected void populateChildren(List<Object> childModels) {
		// Add the static children
		childModels.add(this.externalManagedObjects);
		childModels.add(this.works);
		childModels.add(this.externalFlowItems);
		childModels.add(this.externalEscalations);

		// Add the flow items
		childModels.addAll(this.getCastedModel().getFlowItems());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<DeskEvent>(DeskEvent.values()) {
			protected void handlePropertyChange(DeskEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
					DeskEditPart.this.externalManagedObjects.getEditPart()
							.refresh();
					break;
				case ADD_WORK:
				case REMOVE_WORK:
					DeskEditPart.this.works.getEditPart().refresh();
					break;
				case ADD_EXTERNAL_FLOW:
				case REMOVE_EXTERNAL_FLOW:
					DeskEditPart.this.externalFlowItems.getEditPart().refresh();
					break;
				case ADD_EXTERNAL_ESCALATION:
				case REMOVE_EXTERNAL_ESCALATION:
					DeskEditPart.this.externalEscalations.getEditPart()
							.refresh();
					break;
				case ADD_FLOW_ITEM:
				case REMOVE_FLOW_ITEM:
					DeskEditPart.this.refresh();
					break;
				}
			}
		});
	}

}

/**
 * {@link org.eclipse.gef.editpolicies.LayoutEditPolicy} for the
 * {@link net.officefloor.model.desk.DeskModel}.
 */
class DeskLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<DeskModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy
	 * #createCreateComand(P, java.lang.Object,
	 * org.eclipse.draw2d.geometry.Point)
	 */
	protected CreateCommand<?, ?> createCreateComand(DeskModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}