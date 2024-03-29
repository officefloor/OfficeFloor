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

package net.officefloor.gef.editor.internal.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.geometry.planar.BezierCurve;
import org.eclipse.gef.mvc.fx.parts.AbstractHandlePart;
import org.eclipse.gef.mvc.fx.parts.CircleSegmentHandlePart;
import org.eclipse.gef.mvc.fx.parts.DefaultSelectionHandlePartFactory;
import org.eclipse.gef.mvc.fx.parts.IHandlePart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Provider;

import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class OfficeFloorSelectionHandlePartFactory extends DefaultSelectionHandlePartFactory {

	@Override
	protected List<IHandlePart<? extends Node>> createSingleSelectionHandlePartsForRectangularOutline(
			IVisualPart<? extends Node> target, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {

		// Determine if parent
		if (target instanceof AdaptedParentPart) {
			return Arrays.asList(new StyleClassHandlePart(target));
		}

		// Fall back to default implementation
		return super.createSingleSelectionHandlePartsForRectangularOutline(target, contextMap, segmentsProvider);
	}

	@Override
	protected List<IHandlePart<? extends Node>> createSingleSelectionHandlePartsForCurve(
			IVisualPart<? extends Node> target, Map<Object, Object> contextMap,
			Provider<BezierCurve[]> segmentsProvider) {

		// Determine if can remove connection
		if (target instanceof AdaptedConnectionPart) {
			AdaptedConnectionPart<?, ?, ?> connectionPart = (AdaptedConnectionPart<?, ?, ?>) target;
			if (!connectionPart.getContent().canRemove()) {
				// Not able to remove connection, so don't show handles
				return Collections.emptyList();
			}
		}

		// Fall back to default implementation
		List<IHandlePart<? extends Node>> allHandleParts = super.createSingleSelectionHandlePartsForCurve(target,
				contextMap, segmentsProvider);

		// Include only the circle handle parts (for moving the connection)
		List<IHandlePart<? extends Node>> circleHandleParts = new ArrayList<>(2);
		for (IHandlePart<? extends Node> handlePart : allHandleParts) {
			if (handlePart instanceof CircleSegmentHandlePart) {
				circleHandleParts.add(handlePart);
			}
		}

		// Return only the circle handle parts
		return circleHandleParts;
	}

	/**
	 * {@link IHandlePart} to specify {@link Styleable} class.
	 */
	private class StyleClassHandlePart extends AbstractHandlePart<Node> {

		/**
		 * {@link IVisualPart}.
		 */
		private final IVisualPart<? extends Node> visualPart;

		/**
		 * Instantiate.
		 * 
		 * @param visualPart
		 *            {@link IVisualPart}.
		 */
		public StyleClassHandlePart(IVisualPart<? extends Node> visualPart) {
			this.visualPart = visualPart;
		}

		/**
		 * ============== IHandlePart ====================
		 */

		@Override
		protected void doAttachToAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {

			// Undertake default behaviour
			super.doAttachToAnchorageVisual(anchorage, role);

			// Apply CSS style
			this.visualPart.getVisual().getStyleClass().add("selected");
		}

		@Override
		protected void doDetachFromAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {

			// Remove CSS style
			this.visualPart.getVisual().getStyleClass().remove("selected");

			// Ensure default behaviour occurs
			super.doDetachFromAnchorageVisual(anchorage, role);
		}

		/*
		 * ================ IVisualPart ==================
		 */

		@Override
		protected Node doCreateVisual() {
			Pane pane = new Pane();
			pane.visibleProperty().set(false);
			return pane;
		}

		@Override
		protected void doRefreshVisual(Node visual) {
			// Nothing to update
		}

		@Override
		protected void unregisterFromVisualPartMap(IViewer viewer, Node visual) {
			// AdaptedConnectorPart registered multiple times to same node (so handle)
			Map<Node, IVisualPart<? extends Node>> registry = viewer.getVisualPartMap();
			if (registry.get(visual) == this) {
				registry.remove(visual);
			}
		}
	}

}
