/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.MovePositionalModelCommand;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * Abstract {@link EditPart} for the Office.
 * <p>
 * This provides the implementation of a {@link NodeEditPart} but does not
 * implement the interface. Subclasses may therefore choose to implement the
 * interface if it requires this functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorEditPart<M extends Model, E extends Enum<E>, F extends OfficeFloorFigure>
		extends AbstractGraphicalEditPart implements PropertyChangeListener,
		NodeEditPart {

	/**
	 * Editor containing this.
	 */
	private AbstractOfficeFloorEditor<?, ?> editor = null;

	/**
	 * {@link OfficeFloorFigure} for this {@link EditPart}.
	 */
	private F officeFloorFigure = null;

	/**
	 * {@link OfficeFloorOpenEditPolicy}.
	 */
	private OfficeFloorOpenEditPolicy<M> officeFloorOpenEditPolicy;

	/**
	 * {@link OfficeFloorDirectEditPolicy}.
	 */
	private OfficeFloorDirectEditPolicy<M> officeFloorDirectEditPolicy;

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

	/**
	 * <p>
	 * Specifies the location for the figure.
	 * <p>
	 * This is utilised by {@link MovePositionalModelCommand}.
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
	 * Creates the {@link BeanDialog} for the input bean.
	 * 
	 * @param bean
	 *            Bean.
	 * @param ignoreProperties
	 *            Properties to not be populated.
	 * @return {@link BeanDialog} for the bean.
	 */
	public BeanDialog createBeanDialog(Object bean, String... ignoreProperties) {

		// Create a new Class Loader to ensure latest version of classes
		ClassLoader classLoader = ProjectClassLoader.create(this.editor);

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
		this.getEditor().messageError(message);
	}

	/**
	 * Displays the {@link Throwable} error details as an error
	 * {@link MessageDialog}.
	 * 
	 * @param error
	 *            Error.
	 */
	public void messageError(Throwable error) {
		this.getEditor().messageError(error);
	}

	/**
	 * Displays the message and its cause as an error {@link MessageDialog}.
	 * 
	 * @param message
	 *            Error message.
	 * @param cause
	 *            Cause of error.
	 */
	public void messageError(String message, Throwable cause) {
		this.getEditor().messageError(message, cause);
	}

	/**
	 * Displays the {@link IStatus} error.
	 * 
	 * @param status
	 *            {@link IStatus} error.
	 */
	public void messageError(IStatus status) {
		this.getEditor().messageError(status);
	}

	/**
	 * Displays the message as a warning {@link MessageDialog}.
	 * 
	 * @param message
	 *            Warning message
	 */
	public void messageWarning(String message) {
		this.getEditor().messageWarning(message);
	}

	/*
	 * ================== PropertyChangeListener ==============================
	 */

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		// Obtain the event name
		String eventName = evt.getPropertyName();

		// Obtain the event property
		E event = null;
		E[] events = this.getPropertyChangeEventType().getEnumConstants();
		for (E currentEvent : events) {
			if (currentEvent.name().equals(eventName)) {
				event = currentEvent;
			}
		}

		// Handle the property
		if (event != null) {
			this.handlePropertyChange(event, evt);
		}
	}

	/**
	 * Obtains the {@link Enum} type for the property change events.
	 * 
	 * @return {@link Enum} type for the property change events.
	 */
	protected abstract Class<E> getPropertyChangeEventType();

	/**
	 * Handles the {@link PropertyChangeEvent}.
	 * 
	 * @param property
	 *            {@link Enum} property change event.
	 * @param evt
	 *            {@link PropertyChangeEvent}.
	 */
	protected abstract void handlePropertyChange(E property,
			PropertyChangeEvent evt);

	/*
	 * ================== AbstractGraphicalEditPart ========================
	 */

	@Override
	public RootEditPart getRoot() {
		// Overridden to prevent NPE issues.
		// NPE occurs when undo'ing delete operations involving connections.
		return this.getEditor().getRootEditPart();
	}

	@Override
	protected void createEditPolicies() {

		// Allow direct editing (if configured)
		this.officeFloorDirectEditPolicy = new OfficeFloorDirectEditPolicy<M>();
		this.populateOfficeFloorDirectEditPolicy(this.officeFloorDirectEditPolicy);
		this.installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
				this.officeFloorDirectEditPolicy);

		// Allow open
		this.officeFloorOpenEditPolicy = new OfficeFloorOpenEditPolicy<M>();
		this.populateOfficeFloorOpenEditPolicy(this.officeFloorOpenEditPolicy);
		this.installEditPolicy("OfficeFloorOpen",
				this.officeFloorOpenEditPolicy);

		// Install the graphical node edit policy
		this.installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, this.getEditor()
				.createGraphicalEditPolicy());

		// Initialise
		this.init();
	}

	/**
	 * Sub classes may override to populate the
	 * {@link OfficeFloorDirectEditPolicy}.
	 * 
	 * @param policy
	 *            {@link OfficeFloorDirectEditPolicy} to populate.
	 */
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<M> policy) {
		// Default, do nothing
	}

	/**
	 * Sub classes may override to populate the
	 * {@link OfficeFloorOpenEditPolicy}.
	 * 
	 * @param policy
	 *            {@link OfficeFloorOpenEditPolicy} to populate.
	 */
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<M> policy) {
		// Default, do nothing
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
	 * <p>
	 * Override to indicate that {@link IFigure} needs a
	 * {@link FreeformWrapperFigure}.
	 * <p>
	 * By default this will return <code>true</code> only if the parent
	 * {@link EditPart} is a {@link AbstractOfficeFloorDiagramEditPart}.
	 * Typically this default behaviour will cover most scenarios.
	 * 
	 * @return <code>true</code> if {@link IFigure} needs a
	 *         {@link FreeformWrapperFigure}.
	 */
	protected boolean isFreeformFigure() {

		// Obtain the parent edit part
		EditPart parent = this.getParent();
		boolean isFreeForm = (parent instanceof AbstractOfficeFloorDiagramEditPart<?>);
		return isFreeForm;
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
		// By default no children
	}

	@Override
	protected List<?> getModelSourceConnections() {
		// Create list of connections
		List<Object> connections = new LinkedList<Object>();

		// Populate the Source Connection Models
		this.populateConnectionSourceModels(connections);

		// Return the source connection models
		return connections;
	}

	/**
	 * Populates the Models that are sources of connections.
	 * 
	 * @param models
	 *            List to be populated with Models that are the sources of
	 *            connections.
	 */
	protected void populateConnectionSourceModels(List<Object> models) {
		// By default no sources
	}

	@Override
	protected List<?> getModelTargetConnections() {
		// Create list of connections
		List<Object> connections = new LinkedList<Object>();

		// Populate the Target Connection Models
		this.populateConnectionTargetModels(connections);

		// Return the source connection models
		return connections;
	}

	/**
	 * Populates the Models that are targets of connections.
	 * 
	 * @param models
	 *            List to be populated with Models that are the targets of
	 *            connections.
	 */
	protected void populateConnectionTargetModels(List<Object> models) {
		// By default no targets
	}

	/**
	 * Refresh the visuals.
	 */
	protected void refreshVisuals() {

		// Obtain model for location to refresh
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
	@SuppressWarnings({ "unchecked" })
	public M getCastedModel() {
		return (M) this.getModel();
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

		// Determine if specifically handle request
		if (RequestConstants.REQ_DIRECT_EDIT.equals(req.getType())) {
			// Direct edit
			this.officeFloorDirectEditPolicy.doDirectEdit(this);
			return; // edit policy will handle creating commands
		} else if (RequestConstants.REQ_OPEN.equals(req.getType())) {
			// Open model
			this.officeFloorOpenEditPolicy.doOpen(this);
			return; // edit policy will handle creating commands
		}

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

	/*
	 * ================== NodeEditPart ==============================
	 */

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		// Obtain the type of connection
		Class<?> connectionModelType = connection.getModel().getClass();
		ConnectionAnchor anchor = this.getOfficeFloorFigure()
				.getSourceConnectionAnchor(connectionModelType);
		if (anchor != null) {
			return anchor;
		}

		// No anchor so provide around figure
		return new ChopboxAnchor(this.getFigure());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(this.getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		// Obtain the type of connection
		Class<?> connectionModelType = connection.getModel().getClass();
		ConnectionAnchor anchor = this.getOfficeFloorFigure()
				.getTargetConnectionAnchor(connectionModelType);
		if (anchor != null) {
			return anchor;
		}

		// No anchor so provide around figure
		return new ChopboxAnchor(this.getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new ChopboxAnchor(this.getFigure());
	}

}