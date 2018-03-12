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
package net.officefloor.eclipse.woof;

import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.GhostImageFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;

/**
 * Customisation of the {@link NonResizableEditPolicy} for WoOF.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofNonResizableEditPolicy extends NonResizableEditPolicy {

	/*
	 * ================ NonResizableEditPolicy ======================
	 */

	@Override
	protected IFigure createDragSourceFeedbackFigure() {

		// Provide ghost figure
		IFigure figure = this.getHostFigure();
		GhostImageFigure ghostFigure = new GhostImageFigure(figure, 100, new RGB(31, 31, 31));
		ghostFigure.validate();
		this.addFeedback(ghostFigure);

		// Return the figure
		return ghostFigure;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void createMoveHandle(List handles) {

		// Create and register the handle
		MoveHandle handle = new MoveHandle((GraphicalEditPart) this.getHost());
		this.configureHandle(handle);
		handles.add(handle);

		// Provide no border
		handle.setBorder(null);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void createDragHandle(List handles, int direction) {

		// Create and register the handle
		ResizeHandle handle = new ResizeHandle((GraphicalEditPart) this.getHost(), direction);
		this.configureHandle(handle);
		handles.add(handle);
	}

	/**
	 * Configures the {@link Handle}.
	 * 
	 * @param handle
	 *            {@link AbstractHandle}.
	 */
	private void configureHandle(AbstractHandle handle) {

		// Obtain specific details regarding dragging/selection
		DragTracker tracker;
		Cursor cursor;
		if (isDragAllowed()) {
			tracker = this.getDragTracker();
			cursor = Cursors.SIZEALL;
		} else {
			tracker = this.getSelectTracker();
			cursor = SharedCursors.ARROW;
		}

		// Load details to handle
		handle.setDragTracker(tracker);
		handle.setCursor(cursor);
	}

}