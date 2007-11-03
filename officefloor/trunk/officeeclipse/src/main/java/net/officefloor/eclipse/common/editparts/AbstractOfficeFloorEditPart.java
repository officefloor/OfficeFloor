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
package net.officefloor.eclipse.common.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.ProjectClassLoader;
import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.model.Model;
import net.officefloor.repository.ConfigurationContext;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * Abstract {@link org.eclipse.gef.EditPart} for the Office.
 * </p>
 * <p>
 * This provides the implementation of a {@link org.eclipse.gef.NodeEditPart}
 * but does not implement the interface. Subclasses may therefore choose to
 * implement the interface if it requires this functionality.
 * </p>
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorEditPart<M extends Model> extends
		AbstractGraphicalEditPart implements PropertyChangeListener {

	/**
	 * Editor containing this.
	 */
	private AbstractOfficeFloorEditor editor = null;

	/**
	 * Listing of the {@link PropertyChangeHandler} instances.
	 */
	protected final List<PropertyChangeHandler> propertyChangeHandlers = new LinkedList<PropertyChangeHandler>();

	/**
	 * Initiates the Edit Part.
	 */
	public AbstractOfficeFloorEditPart() {
		// Populate the property change handlers
		this.populatePropertyChangeHandlers(this.propertyChangeHandlers);
	}

	/**
	 * Specifies the {@link AbstractOfficeFloorEditor} that contains this
	 * {@link org.eclipse.gef.EditPart}.
	 * 
	 * @param editor
	 *            Editor containing this.
	 */
	public void setOfficeFloorEditor(AbstractOfficeFloorEditor editor) {
		this.editor = editor;
	}

	/**
	 * Obtains the Editor for this.
	 * 
	 * @return Editor for this.
	 */
	public AbstractOfficeFloorEditor getEditor() {
		return this.editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		// Disallow resizing
		this.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
				new NonResizableEditPolicy());

		// Allow delegating back to this EditPart
		this.installEditPolicy("OfficeFloor", new OfficeFloorEditPolicy());

		// Initialise
		this.init();
	}

	/**
	 * Sub classes may override this method to initialise various state.
	 */
	protected void init() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#setModel(java.lang.Object)
	 */
	public void setModel(Object model) {

		// Register with the model if capable
		if (model instanceof Model) {
			Model modelElement = (Model) model;
			modelElement.addPropertyChangeListener(this);
		}

		// Now register the model
		super.setModel(model);
	}

	/**
	 * Handles property changes by utilising registered
	 * {@link PropertyChangeHandler} instances.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// Handle property change
		for (PropertyChangeHandler handler : this.propertyChangeHandlers) {
			handler.propertyChange(evt);
		}
	}

	/**
	 * Populates the handlers to handle property changes.
	 * 
	 * @param handlers
	 *            List of {@link PropertyChangeHandler} to be populated.
	 */
	protected abstract void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List getModelChildren() {
		// Create the list of model children
		List<Object> models = new LinkedList<Object>();

		// Populate the children
		this.populateModelChildren(models);

		// Return the children
		return models;
	}

	/**
	 * Override to populdate the children of this model.
	 * 
	 * @param childModels
	 *            List to be populated with the children models.
	 */
	protected void populateModelChildren(List<Object> childModels) {
		// Defaultly no children
	}

	/**
	 * Refresh the visuals.
	 */
	protected void refreshVisuals() {
		// Specify location for the model
		Model model = this.getCastedModel();

		// Obtain the size of the figure
		Dimension figureSize = this.getFigure().getSize();

		// Refresh the view off the model
		this.getFigure().setBounds(
				new Rectangle(model.getX(), model.getY(), figureSize.width,
						figureSize.height));
	}

	/**
	 * Obtains the model casted to its specific type.
	 * 
	 * @return Model casted to its specific type.
	 */
	@SuppressWarnings( { "unchecked" })
	public M getCastedModel() {
		return (M) this.getModel();
	}

	/**
	 * Specifies the location for the figure.
	 * 
	 * @param location
	 *            Location for the figure.
	 */
	public void setLocation(Point location) {
		// Specify the constraints on the model
		Model model = this.getCastedModel();
		model.setX(location.x);
		model.setY(location.y);

		// Refresh the view
		this.refreshVisuals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
	 */
	public void performRequest(Request req) {
		// Obtain the command and execute if have
		Command command = this.getCommand(req);
		if (command != null) {
			// Execute the command
			this.getViewer().getEditDomain().getCommandStack().execute(command);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	public void activate() {
		if (!this.isActive()) {
			super.activate();

			// Start listening to model
			this.getCastedModel().addPropertyChangeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	public void deactivate() {
		if (this.isActive()) {
			// Stop listening to model
			this.getCastedModel().removePropertyChangeListener(this);

			super.deactivate();
		}
	}

	/**
	 * Provides an {@link EditPolicy} that delegates back to this
	 * {@link org.eclipse.gef.EditPart}.
	 */
	private class OfficeFloorEditPolicy extends AbstractEditPolicy {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.EditPolicy#getCommand(org.eclipse.gef.Request)
		 */
		public Command getCommand(Request request) {
			// Obtain request type
			Object type = request.getType();

			// Handle request by delegating back to Edit Part
			if (REQ_OPEN.equals(type)) {
				// Double Click on Edit Part
				return handleDoubleClick(request);
			}

			// Not to be handled
			return null;
		}

	}

	/**
	 * Override to handle the double click on the
	 * {@link org.eclipse.gef.EditPart}.
	 * 
	 * @param request
	 *            Request.
	 */
	protected Command handleDoubleClick(Request request) {
		// Defaultly not handled
		return null;
	}

	/**
	 * Creates the {@link BeanDialog} for the input bean.
	 * 
	 * @param bean
	 *            Bean.
	 * @param ignoreProperties
	 *            Properties to not be populated.
	 * @return {@link BeanDialog} for the bean.
	 */
	protected BeanDialog createBeanDialog(Object bean,
			String... ignoreProperties) {

		// Obtain the configuration context
		ConfigurationContext context = new FileConfigurationItem(this.editor
				.getEditorInput()).getContext();

		// Create a new Class Loader to ensure load the latest version of
		// classes
		ClassLoader classLoader = ProjectClassLoader.create(context);

		// Obtain the Shell for the dialog
		Shell editorShell = this.editor.getSite().getShell();

		// Return the bean dialog
		return new BeanDialog(editorShell, bean, classLoader, ignoreProperties);
	}

	/**
	 * Displays the message as an error {@link MessageDialog}.
	 * 
	 * @param message
	 *            Error message.
	 */
	protected void messageError(String message) {
		MessageDialog.openError(this.getEditor().getEditorSite().getShell(),
				"Office Floor", message);
	}

	/**
	 * Displays the {@link Throwable} error details as an error
	 * {@link MessageDialog}.
	 * 
	 * @param error
	 *            Error.
	 */
	protected void messageError(Throwable error) {
		// Obtain the stack trace
		StringWriter buffer = new StringWriter();
		error.printStackTrace(new PrintWriter(buffer));

		// Display the stack trace
		this.messageError(buffer.toString());
	}

	/**
	 * Displays the message as a warning {@link MessageDialog}.
	 * 
	 * @param message
	 *            Warning message
	 */
	protected void messageWarning(String message) {
		MessageDialog.openWarning(this.getEditor().getEditorSite().getShell(),
				"Office Floor", message);
	}
}
