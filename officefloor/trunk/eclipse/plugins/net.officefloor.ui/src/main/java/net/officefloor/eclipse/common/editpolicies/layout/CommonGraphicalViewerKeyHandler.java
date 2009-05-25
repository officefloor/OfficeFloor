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
package net.officefloor.eclipse.common.editpolicies.layout;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * <p>
 * Common key handler for Office Floor Editors.
 * </p>
 * <p>
 * Adds delete handling.
 * </p>
 * 
 * @author Daniel
 */
public class CommonGraphicalViewerKeyHandler extends GraphicalViewerKeyHandler {

	/**
	 * Initiate with viewer to listen on.
	 * 
	 * @param viewer
	 *            Viewer to listen on.
	 */
	public CommonGraphicalViewerKeyHandler(GraphicalViewer viewer) {
		super(viewer);
	}

	/*
	 * ==================== GraphicalViewerKeyHandler =========================
	 */

	@Override
	public boolean keyPressed(KeyEvent event) {

		// Check if event is delete
		if ((SWT.DEL == event.keyCode) || ('\b' == event.character)) {
			// Obtain the selected edit parts
			List<?> selectedEditParts = this.getViewer().getSelectedEditParts();
			if (selectedEditParts.size() > 0) {
				// Request deletion of the edit parts
				EditPart[] editParts = selectedEditParts
						.toArray(new EditPart[0]);
				this.getViewer().getContents().performRequest(
						new DeleteRequest(editParts));
			}
		}

		// Allow inherited handling
		return super.keyPressed(event);
	}

}