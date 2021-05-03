/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.gef.editor.internal.policies;

import org.eclipse.gef.fx.nodes.InfiniteCanvas;
import org.eclipse.gef.mvc.fx.policies.ViewportPolicy;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import javafx.geometry.Bounds;

public class ContentRestrictedChangeViewportPolicy extends ViewportPolicy {

	@Override
	protected void locallyExecuteOperation() {
		// determine current translation
		double tx = getChangeViewportOperation().getNewHorizontalScrollOffset();
		double ty = getChangeViewportOperation().getNewVerticalScrollOffset();

		// determine direction of change
		double dx = tx - getChangeViewportOperation().getInitialHorizontalScrollOffset();
		double dy = ty - getChangeViewportOperation().getInitialVerticalScrollOffset();

		// determine scrollable bounds
		InfiniteCanvasViewer viewer = (InfiniteCanvasViewer) getHost().getRoot().getViewer();
		InfiniteCanvas canvas = viewer.getCanvas();
		Bounds scrollableBounds = canvas.getScrollableBounds();

		// compute content restricted translation range
		// XXX: Scrolling is implemented by translating the scene graph node
		// that holds all children. Therefore, scrolling to the right is
		// actually moving this node to the left, and vice versa.
		double contentRestrictedMinTranslateX = -scrollableBounds.getMinX();
		double contentRestrictedMinTranslateY = -scrollableBounds.getMinY();
		// XXX: The size of the canvas is the visible area which has to be
		// subtracted from the scrollable bounds to yield the translation range.
		double contentRestrictedMaxTranslateX = -(scrollableBounds.getMaxX() - canvas.getWidth());
		double contentRestrictedMaxTranslateY = -(scrollableBounds.getMaxY() - canvas.getHeight());

		// sort content restricted bounds numerically
		if (contentRestrictedMinTranslateX > contentRestrictedMaxTranslateX) {
			double tmp = contentRestrictedMinTranslateX;
			contentRestrictedMinTranslateX = contentRestrictedMaxTranslateX;
			contentRestrictedMaxTranslateX = tmp;
		}
		if (contentRestrictedMinTranslateY > contentRestrictedMaxTranslateY) {
			double tmp = contentRestrictedMinTranslateY;
			contentRestrictedMinTranslateY = contentRestrictedMaxTranslateY;
			contentRestrictedMaxTranslateY = tmp;
		}

		// do not allow translation when the corresponding scrollbar is
		// invisible, and otherwise restrict the translation values to the
		// scrollable bounds
		if (!canvas.getHorizontalScrollBar().isVisible()) {
			tx = getChangeViewportOperation().getInitialHorizontalScrollOffset();
		} else if (tx < contentRestrictedMinTranslateX || tx > contentRestrictedMaxTranslateX) {
			// restrict to minimum/maximum depending on direction
			if (dx < 0) {
				tx = contentRestrictedMinTranslateX;
			} else {
				tx = contentRestrictedMaxTranslateX;
			}
		}
		if (!canvas.getVerticalScrollBar().isVisible()) {
			ty = getChangeViewportOperation().getInitialVerticalScrollOffset();
		} else if (ty < contentRestrictedMinTranslateY || ty > contentRestrictedMaxTranslateY) {
			// restrict to minimum/maximum depending on direction
			if (dy < 0) {
				ty = contentRestrictedMinTranslateY;
			} else {
				ty = contentRestrictedMaxTranslateY;
			}
		}

		// change operation values
		getChangeViewportOperation().setNewHorizontalScrollOffset(tx);
		getChangeViewportOperation().setNewVerticalScrollOffset(ty);

		// execute
		super.locallyExecuteOperation();
	}

}
