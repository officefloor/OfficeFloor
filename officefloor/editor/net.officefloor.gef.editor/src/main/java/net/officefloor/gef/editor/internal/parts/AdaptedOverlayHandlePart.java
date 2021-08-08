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

import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.parts.AbstractHandlePart;
import org.eclipse.gef.mvc.fx.parts.IHandlePart;

import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;
import net.officefloor.gef.common.resize.DragResizer;
import net.officefloor.gef.editor.OverlayVisualContext;
import net.officefloor.gef.editor.OverlayVisualFactory;

/**
 * {@link IHandlePart} for the overlay.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedOverlayHandlePart extends AbstractHandlePart<Pane> implements OverlayVisualContext {

	/**
	 * Location.
	 */
	private final Point location;

	/**
	 * {@link OverlayVisualFactory}.
	 */
	private final OverlayVisualFactory visualFactory;

	/**
	 * Overlay parent {@link Pane}.
	 */
	private final Pane overlayParent = new Pane();

	/**
	 * Indicates if fixed width overlay.
	 */
	private boolean isFixedWidth = false;

	/**
	 * Indicates if fixed height overlay.
	 */
	private boolean isFixedHeight = false;

	/**
	 * Instantiate.
	 * 
	 * @param location      Location.
	 * @param visualFactory {@link OverlayVisualContext}.
	 */
	public AdaptedOverlayHandlePart(Point location, OverlayVisualFactory visualFactory) {
		this.location = location;
		this.visualFactory = visualFactory;
	}

	/*
	 * =============== AbstractHandlePart ===================
	 */

	@Override
	protected Pane doCreateVisual() {

		// Configure the overlay parent
		this.visualFactory.loadOverlay(this);

		// Specify location of overlay
		overlayParent.setLayoutX(this.location.x);
		overlayParent.setLayoutY(this.location.y);

		// Provide resizing
		if (!this.isFixedWidth) {
			DragResizer.makeResizable(overlayParent, Orientation.HORIZONTAL);
		}
		if (!this.isFixedHeight) {
			DragResizer.makeResizable(overlayParent, Orientation.VERTICAL);
		}

		// Return the overlay parent
		return overlayParent;
	}

	@Override
	protected void doRefreshVisual(Pane visual) {
		// Nothing to refresh
	}

	/*
	 * ============= OverlayVisualContext ==============
	 */

	@Override
	public Pane getOverlayParent() {
		return this.overlayParent;
	}

	@Override
	public void setFixedWidth(boolean isFixedWith) {
		this.isFixedWidth = isFixedWith;
	}

	@Override
	public void setFixedHeight(boolean isFixedHeight) {
		this.isFixedHeight = isFixedHeight;
	}

	@Override
	public void close() {
		this.getRoot().removeChild(this);
	}

}
