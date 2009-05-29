/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.drag;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Provides a {@link org.eclipse.ui.views.navigator.LocalSelectionTransfer} drag
 * target implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class LocalSelectionTransferDragTargetListener extends
		AbstractTransferDropTargetListener {

	/**
	 * Initiate with {@link EditPartViewer} to drop target.
	 * 
	 * @param viewer
	 *            {@link EditPartViewer} to drop target.
	 */
	public LocalSelectionTransferDragTargetListener(EditPartViewer viewer) {
		super(viewer, LocalSelectionTransfer.getInstance());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.dnd.AbstractTransferDropTargetListener#createTargetRequest()
	 */
	protected Request createTargetRequest() {
		CreateRequest request = new CreateRequest();

		// Obtain the transfer
		LocalSelectionTransfer selectionTransfer = (LocalSelectionTransfer) this
				.getTransfer();

		// Obtain the selection
		ISelection selection = selectionTransfer.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		// Obtain the first element being selected
		Object firstSelected = structuredSelection.getFirstElement();

		// Load the model
		if (firstSelected instanceof EditPart) {
			EditPart editPart = (EditPart) firstSelected;

			// Obtain the model from the edit part
			Object model = editPart.getModel();

			// Load to creation request
			request
					.setFactory(new LocalSelectionTransferCreationFactory(model));

			// Return the request
			return request;
		} else {
			// Not an edit part therefore can not drag in
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.dnd.AbstractTransferDropTargetListener#updateTargetRequest()
	 */
	protected void updateTargetRequest() {
		((CreateRequest) this.getTargetRequest()).setLocation(this
				.getDropLocation());
	}

}

/**
 * {@link org.eclipse.gef.requests.CreationFactory} implementation for this drop
 * target listener.
 */
class LocalSelectionTransferCreationFactory implements CreationFactory {

	/**
	 * New object to be returned from this factory.
	 */
	protected final Object newObject;

	/**
	 * Initiates with the new object to be returned from this factory.
	 * 
	 * @param newObject
	 *            New object.
	 */
	public LocalSelectionTransferCreationFactory(Object newObject) {
		// Store state
		this.newObject = newObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
	 */
	public Object getNewObject() {
		return this.newObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
	 */
	public Object getObjectType() {
		return this.newObject.getClass();
	}

}