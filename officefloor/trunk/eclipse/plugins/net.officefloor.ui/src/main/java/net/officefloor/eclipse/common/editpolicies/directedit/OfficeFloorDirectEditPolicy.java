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
package net.officefloor.eclipse.common.editpolicies.directedit;

import net.officefloor.eclipse.common.commands.ChangeCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

/**
 * {@link DirectEditPolicy} for directly editing a value for a {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDirectEditPolicy<M> extends DirectEditPolicy {

	/**
	 * Flag indicating if allow direct editing.
	 */
	private boolean isAllowEdit = false;

	/**
	 * {@link DirectEditInitialiser}.
	 */
	private DirectEditInitialiser initialiser;

	/**
	 * {@link DirectEditChangeFactory}.
	 */
	@SuppressWarnings("rawtypes")
	private DirectEditChangeFactory factory;

	/**
	 * Initialises this {@link OfficeFloorDirectEditPolicy} to allow directly
	 * editing the {@link Model} of the {@link EditPolicy} host.
	 * 
	 * @param initialiser
	 *            {@link DirectEditInitialiser}.
	 * @param factory
	 *            {@link DirectEditChangeFactory}.
	 */
	public <C> void allowDirectEdit(DirectEditInitialiser initialiser,
			DirectEditChangeFactory<C, M> factory) {
		this.initialiser = initialiser;
		this.factory = factory;
		this.isAllowEdit = true;
	}

	/**
	 * Initialise this {@link OfficeFloorDirectEditPolicy} to allow directly
	 * editing the {@link Model} of the {@link EditPolicy} host.
	 * 
	 * @param adapter
	 *            {@link DirectEditAdapter}.
	 */
	public <C> void allowDirectEdit(DirectEditAdapter<C, M> adapter) {
		this.allowDirectEdit(adapter, adapter);
	}

	/**
	 * <p>
	 * Does the direct edit and handles creating the necessary {@link Command}.
	 * <p>
	 * This is to be called in
	 * {@link EditPart#performRequest(org.eclipse.gef.Request)} on a
	 * {@link RequestConstants#REQ_DIRECT_EDIT}.
	 */
	public void doDirectEdit(GraphicalEditPart editPart) {

		// Do nothing if not allow to do direct edit
		if (!this.isAllowEdit) {
			return;
		}

		// Create the locator to position the direct edit
		CellEditorLocator locator = new CellEditorLocator() {

			@Override
			public void relocate(CellEditor celleditor) {

				// Obtain the location
				IFigure figure = OfficeFloorDirectEditPolicy.this.initialiser
						.getLocationFigure();
				Rectangle rect = figure.getBounds().getCopy();
				figure.translateToAbsolute(rect);

				// Obtain the preferred size
				Text text = (Text) celleditor.getControl();
				Point pref = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);

				// Position the cell editor text box
				text.setBounds(rect.x, rect.y, pref.x, pref.y);
			}
		};

		// Create the direct edit manager to handle direct editing
		DirectEditManager manager = new DirectEditManager(editPart,
				TextCellEditor.class, locator) {
			@Override
			protected void initCellEditor() {

				// Obtain the initial value
				String initialValue = OfficeFloorDirectEditPolicy.this.initialiser
						.getInitialValue();
				if (EclipseUtil.isBlank(initialValue)) {
					initialValue = ""; // ensure not null
				}

				// Load the initial value
				CellEditor editor = this.getCellEditor();
				editor.setValue(initialValue);
			}
		};

		// Show the manager which does the direct edit
		manager.show();
	}

	/*
	 * =================== DirectEditPolicy ==================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	protected Command getDirectEditCommand(DirectEditRequest request) {

		// Do nothing if not allow to do direct edit
		if (!this.isAllowEdit) {
			return null;
		}

		// Obtain the host edit part
		EditPart editPart = this.getHost();

		// Obtain the changes
		Object changes;
		if (editPart instanceof AbstractOfficeFloorEditPart) {
			AbstractOfficeFloorEditPart<?, ?, ?> officeFloorEditPart = (AbstractOfficeFloorEditPart<?, ?, ?>) editPart;
			changes = officeFloorEditPart.getEditor().getModelChanges();
		} else if (editPart instanceof AbstractOfficeFloorConnectionEditPart) {
			AbstractOfficeFloorConnectionEditPart<?, ?> officeFloorConnectionEditPart = (AbstractOfficeFloorConnectionEditPart<?, ?>) editPart;
			changes = officeFloorConnectionEditPart.getEditor()
					.getModelChanges();
		} else {
			MessageDialog.openError(null, "Error", "Unknown edit part type "
					+ editPart.getClass().getName());
			return null; // must obtain changes
		}

		// Obtain the model
		Object model = editPart.getModel();

		// Obtain the new value
		Text text = (Text) request.getCellEditor().getControl();
		String newValue = text.getText();

		// Create the change
		Change<?> change = this.factory.createChange(changes, model, newValue);
		if (change == null) {
			return null; // must have change
		}

		// Return the command to make the change
		return new ChangeCommand(change);
	}

	@Override
	protected void showCurrentEditValue(DirectEditRequest request) {
		// Do not show current value
	}

}