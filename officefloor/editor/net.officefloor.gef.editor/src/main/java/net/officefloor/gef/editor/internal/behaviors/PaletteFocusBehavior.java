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

package net.officefloor.gef.editor.internal.behaviors;

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
