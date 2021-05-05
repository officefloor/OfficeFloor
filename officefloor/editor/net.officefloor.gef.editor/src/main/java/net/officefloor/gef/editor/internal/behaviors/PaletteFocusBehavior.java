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
