/*******************************************************************************
 * Copyright (c) 2016 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.parts;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.geometry.planar.BezierCurve;
import org.eclipse.gef.mvc.fx.parts.AbstractHandlePart;
import org.eclipse.gef.mvc.fx.parts.DefaultSelectionHandlePartFactory;
import org.eclipse.gef.mvc.fx.parts.IHandlePart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

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

	}

}