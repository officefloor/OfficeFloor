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

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;
import net.officefloor.model.repository.ConfigurationContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

/**
 * <p>
 * Abstract {@link EditPart} for the Office.
 * <p>
 * This provides the implementation of a {@link NodeEditPart} but does not
 * implement the interface. Subclasses may therefore choose to implement the
 * interface if it requires this functionality.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorEditPart<M extends Model, F extends OfficeFloorFigure>
		extends AbstractGraphicalEditPart implements PropertyChangeListener {

	/**
	 * Editor containing this.
	 */
	private AbstractOfficeFloorEditor<?, ?> editor = null;

	/**
	 * Listing of the {@link PropertyChangeHandler} instances.
	 */
	protected final List<PropertyChangeHandler<?>> propertyChangeHandlers = new LinkedList<PropertyChangeHandler<?>>();

	/**
	 * {@link OfficeFloorFigure} for this {@link EditPart}.
	 */
	private F officeFloorFigure = null;

	/**
	 * Initiates the Edit Part.
	 */
	public AbstractOfficeFloorEditPart() {
		// Populate the property change handlers
		this.populatePropertyChangeHandlers(this.propertyChangeHandlers);
	}

	/**
	 * Specifies the {@link AbstractOfficeFloorEditor} that contains this
	 * {@link EditPart}.
	 * 
	 * @param editor
	 *            Editor containing this.
	 */
	public void setOfficeFloorEditor(AbstractOfficeFloorEditor<?, ?> editor) {
		this.editor = editor;
	}

	/**
	 * Obtains the Editor for this.
	 * 
	 * @return Editor for this.
	 */
	public AbstractOfficeFloorEditor<?, ?> getEditor() {
		return this.editor;
	}

	/*
	 * ================== AbstractGraphicalEditPart ========================
	 */

	@Override
	public RootEditPart getRoot() {
		// Sometimes parent may not be set
		if (this.getParent() != null) {
			// Parent available, so follow to get root
			return super.getRoot();
		} else if (this.editor != null) {
			// Return from editor
			return ((EditPart) this.editor.getRootEditPart()).getRoot();
		} else {
			// Can not obtain root
			return null;
		}
	}

	@Override
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

	@Override
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
		for (PropertyChangeHandler<?> handler : this.propertyChangeHandlers) {
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
			List<PropertyChangeHandler<?>> handlers);

	@Override
	protected IFigure createFigure() {

		// Obtain the figure
		IFigure figure = this.getOfficeFloorFigure().getFigure();
		if (this.isFreeformFigure()) {
			// Wrap with free form wrapper
			figure = new FreeformWrapperFigure((Figure) figure);
		}

		// Return the figure
		return figure;
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeFloorFigure} for this {@link EditPart}.
	 * <p>
	 * This will lazy create the {@link OfficeFloorFigure}.
	 * 
	 * @return {@link OfficeFloorFigure}.
	 */
	public F getOfficeFloorFigure() {
		if (this.officeFloorFigure == null) {
			this.officeFloorFigure = this.createOfficeFloorFigure();
		}
		return this.officeFloorFigure;
	}

	/**
	 * Override to indicate that {@link IFigure} needs a
	 * {@link FreeformWrapperFigure}.
	 * 
	 * @return <code>true</code> if {@link IFigure} needs a
	 *         {@link FreeformWrapperFigure}.
	 */
	protected boolean isFreeformFigure() {
		return false;
	}

	/**
	 * Creates the {@link OfficeFloorFigure} for this {@link EditPart}.
	 * 
	 * @return {@link OfficeFloorFigure}.
	 */
	protected abstract F createOfficeFloorFigure();

	@Override
	public IFigure getContentPane() {
		// Return the content pane of the Office Floor Figure
		OfficeFloorFigure officeFloorFigure = this.getOfficeFloorFigure();
		IFigure contentPane = officeFloorFigure.getContentPane();

		// Use top level figure if no content pane provided
		return (contentPane == null ? this.getFigure() : contentPane);
	}

	@Override
	protected List<?> getModelChildren() {
		// Create the list of model children
		List<Object> models = new LinkedList<Object>();

		// Populate the children
		this.populateModelChildren(models);

		// Return the children
		return models;
	}

	/**
	 * Override to populate the children of this model.
	 * 
	 * @param childModels
	 *            List to be populated with the children models.
	 */
	protected void populateModelChildren(List<Object> childModels) {
		// By Default no children
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

	/**
	 * Executes the input {@link Command}.
	 * 
	 * @param command
	 *            {@link Command}.
	 */
	protected void executeCommand(OfficeFloorCommand command) {
		if (command != null) {
			this.getViewer().getEditDomain().getCommandStack().execute(command);
		}
	}

	@Override
	public void performRequest(Request req) {
		// Obtain the command and execute if have
		Command command = this.getCommand(req);

		// Execute the command
		if (command != null) {
			this.getViewer().getEditDomain().getCommandStack().execute(command);
		}
	}

	@Override
	public void activate() {
		if (!this.isActive()) {
			super.activate();

			// Start listening to model
			this.getCastedModel().addPropertyChangeListener(this);
		}
	}

	@Override
	public void deactivate() {
		if (this.isActive()) {
			// Stop listening to model
			this.getCastedModel().removePropertyChangeListener(this);

			super.deactivate();
		}
	}

	/**
	 * Provides an {@link EditPolicy} that delegates back to this
	 * {@link EditPart}.
	 */
	private class OfficeFloorEditPolicy extends AbstractEditPolicy {

		/*
		 * ==================== EditPolicy ===========================
		 */

		@Override
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
	 * Override to handle the double click on the {@link EditPart}.
	 * 
	 * @param request
	 *            Request.
	 */
	protected Command handleDoubleClick(Request request) {
		// By default not handled
		return null;
	}

	/**
	 * Opens the {@link IFile} that corresponds to the input class path location
	 * of the {@link IProject} containing this {@link IEditorInput}.
	 * 
	 * @param classpathFilePath
	 *            Path of the file on the class path to open.
	 * @param editorId
	 *            Id of the {@link IEditorPart}.
	 */
	public void openClasspathFile(String classpathFilePath, String editorId) {
		try {
			// Obtain the URL with full path
			ProjectClassLoader projectClassLoader = ProjectClassLoader
					.create(this.getEditor());
			URL url = projectClassLoader.getResource(classpathFilePath);
			if (url == null) {
				// Can not find item to open
				MessageDialog.openWarning(this.getEditor().getEditorSite()
						.getShell(), "Open", "Can not find '"
						+ classpathFilePath + "'");
				return;
			}

			// Obtain the file to open
			String urlFilePath = url.getFile();
			IPath path = new Path(urlFilePath);
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocation(path);
			if (files.length != 1) {
				// Can not find file
				MessageDialog.openWarning(this.getEditor().getEditorSite()
						.getShell(), "Open", "Can not find '"
						+ classpathFilePath + "' at [" + urlFilePath + "]");
				return;
			}
			IFile file = files[0];

			// Open the file
			IDE.openEditor(this.getEditor().getEditorSite().getPage(), file,
					editorId);

		} catch (Throwable ex) {
			// Failed to open file
			MessageDialog.openInformation(this.getEditor().getEditorSite()
					.getShell(), "Open", "Failed to open '" + classpathFilePath
					+ "': " + ex.getMessage());
		}
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
	public BeanDialog createBeanDialog(Object bean, String... ignoreProperties) {

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
	public void messageError(String message) {
		this.messageError(new Status(IStatus.ERROR,
				OfficeFloorPlugin.PLUGIN_ID, message));
	}

	/**
	 * Displays the {@link Throwable} error details as an error
	 * {@link MessageDialog}.
	 * 
	 * @param error
	 *            Error.
	 */
	public void messageError(Throwable error) {
		this.messageError(new Status(IStatus.ERROR,
				OfficeFloorPlugin.PLUGIN_ID, error.getMessage(), error));
	}

	/**
	 * Displays the {@link IStatus} error.
	 * 
	 * @param status
	 *            {@link IStatus} error.
	 */
	public void messageError(IStatus status) {
		this.messageStatus(status, "Error");
	}

	/**
	 * Displays the message as a warning {@link MessageDialog}.
	 * 
	 * @param message
	 *            Warning message
	 */
	public void messageWarning(String message) {
		this.messageStatus(new Status(IStatus.WARNING,
				OfficeFloorPlugin.PLUGIN_ID, message), "Warning");
	}

	/**
	 * Displays a {@link Dialog} for the {@link IStatus}.
	 * 
	 * @param status
	 *            {@link IStatus}.
	 * @param title
	 *            Title for {@link Dialog}.
	 */
	private void messageStatus(IStatus status, String title) {
		// Display the error
		ErrorDialog.openError(this.getEditor().getEditorSite().getShell(),
				title, null, status);
	}

}