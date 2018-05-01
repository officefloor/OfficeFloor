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
package net.officefloor.eclipse.editor.internal.behaviors;

import org.eclipse.gef.fx.nodes.InfiniteCanvas;
import org.eclipse.gef.mvc.fx.behaviors.FocusBehavior;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

/**
 * {@link FocusBehavior} for the palette.
 *
 * @author Daniel Sagenschneider
 */
public class PaletteFocusBehavior extends FocusBehavior {

	/**
	 * Default style (as can not style {@link InfiniteCanvas} via style sheets).
	 */
	public static final String DEFAULT_STYLE = "-fx-background-insets: 0; -fx-padding: 0; -fx-border-width: 0;";

	/*
	 * ============== FocusBehaviour ===================
	 */

	@Override
	protected void addViewerFocusedFeedback() {
		super.addViewerFocusedFeedback();

		// Style
		IViewer viewer = getHost().getRoot().getViewer();
		viewer.getCanvas().setStyle(DEFAULT_STYLE);
	}

	@Override
	protected void removeViewerFocusedFeedback() {
		super.removeViewerFocusedFeedback();

		// Style
		IViewer viewer = getHost().getRoot().getViewer();
		viewer.getCanvas().setStyle(DEFAULT_STYLE);
	}

}
