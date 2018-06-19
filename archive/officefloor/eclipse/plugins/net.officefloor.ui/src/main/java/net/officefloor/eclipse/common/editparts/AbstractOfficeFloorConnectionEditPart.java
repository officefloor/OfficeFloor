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

import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.model.ConnectionModel;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

/**
 * Abstract Office Floor {@link AbstractConnectionEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorConnectionEditPart<M extends ConnectionModel, E extends Enum<E>>
		extends AbstractConnectionEditPart implements PropertyChangeListener {

	/**
	 * Editor containing this.
	 */
	private AbstractOfficeFloorEditor<?, ?> editor = null;

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

	/*
	 * ================ PropertyChangeListener =============================
	 */

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		// Only handle property if event class provided
		Class<E> eventClass = this.getPropertyChangeEventType();
		if (eventClass == null) {
			return;
		}

		// Obtain the event name
		String eventName = evt.getPropertyName();

		// Obtain the event property
		E event = null;
		E[] events = eventClass.getEnumConstants();
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
	 * <p>
	 * Obtains the {@link Enum} type for the property change events.
	 * <p>
	 * By default this returns <code>null</code> to indicate not handle property
	 * changes. Overriding to return a value will result in
	 * {@link #handlePropertyChange(Enum, PropertyChangeEvent)} being invoked
	 * for the property changes.
	 * 
	 * @return {@link Enum} type for the property change events.
	 */
	protected Class<E> getPropertyChangeEventType() {
		return null;
	}

	/**
	 * <p>
	 * Handles the {@link PropertyChangeEvent}.
	 * <p>
	 * By default this does nothing. Override to handle property changes
	 * (remembering to provide return value from
	 * {@link #getPropertyChangeEventType()}).
	 * 
	 * @param property
	 *            {@link Enum} property change event.
	 * @param evt
	 *            {@link PropertyChangeEvent}.
	 * 
	 * @see #getPropertyChangeEventType()
	 */
	protected void handlePropertyChange(E property, PropertyChangeEvent evt) {
		// Do nothing by default
	}

	/*
	 * =============== AbstractConnectionEditPart =============================
	 */

	@Override
	protected IFigure createFigure() {

		// Create the default connection figure
		PolylineConnection connFigure = (PolylineConnection) super
				.createFigure();
		this.decorateFigure(connFigure);

		// Return the figure
		return connFigure;
	}

	/**
	 * Invoked to decorate the {@link Figure} for the {@link ConnectionModel}.
	 * 
	 * @param figure
	 *            {@link PolylineConnection}.
	 */
	protected abstract void decorateFigure(PolylineConnection figure);

	@Override
	protected void createEditPolicies() {
		// Provide connection policy
		this.installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());

		// Allow direct editing (if configured)
		this.officeFloorDirectEditPolicy = new OfficeFloorDirectEditPolicy<M>();
		this
				.populateOfficeFloorDirectEditPolicy(this.officeFloorDirectEditPolicy);
		this.installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
				this.officeFloorDirectEditPolicy);
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

	@Override
	public void performRequest(Request req) {

		// Determine if direct edit
		if (RequestConstants.REQ_DIRECT_EDIT.equals(req.getType())) {
			this.officeFloorDirectEditPolicy.doDirectEdit(this);
			return; // edit policy will handle creating commands
		}

		// Provide default behaviour
		super.performRequest(req);
	}

	/**
	 * Obtains the specific Model.
	 * 
	 * @return Specific Model.
	 */
	@SuppressWarnings( { "unchecked" })
	public M getCastedModel() {
		return (M) this.getModel();
	}

	@Override
	public void setModel(Object model) {

		// Register with the model if capable
		if (model instanceof ConnectionModel) {
			ConnectionModel modelElement = (ConnectionModel) model;
			modelElement.addPropertyChangeListener(this);
		}

		// Now register the model
		super.setModel(model);
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

}