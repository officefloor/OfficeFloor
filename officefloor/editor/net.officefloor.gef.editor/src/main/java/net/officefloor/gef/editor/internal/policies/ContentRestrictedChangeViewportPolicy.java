/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
