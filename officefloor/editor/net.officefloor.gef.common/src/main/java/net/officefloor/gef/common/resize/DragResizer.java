/*-
 * #%L
 * [bundle] OfficeFloor Common
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

package net.officefloor.gef.common.resize;

import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Allows drag resizing of a {@link Region}.
 * 
 * @author Daniel Sagenschneider
 */
public class DragResizer {

	/**
	 * Allows making the {@link Region} resizeable.
	 * 
	 * @param region
	 *            {@link Region}.
	 * @param orientation
	 *            Indicates whether resize horizontal or vertically.
	 */
	public static void makeResizable(Region region, Orientation orientation) {
		final DragResizer resizer = new DragResizer(region, orientation);
		region.setOnMouseMoved((event) -> resizer.mouseOver(event));
		region.setOnMousePressed((event) -> resizer.mousePressed(event));
		region.setOnMouseDragged((event) -> resizer.mouseDragged(event));
		region.setOnMouseReleased((event) -> resizer.mouseReleased(event));
	}

	/**
	 * Resize margin.
	 */
	private static final int RESIZE_MARGIN = 5;

	/**
	 * {@link Region} being resized;
	 */
	private final Region region;

	/**
	 * {@link Orientation}.
	 */
	private final Orientation orientation;

	/**
	 * Indicates currently dragging.
	 */
	private boolean dragging;

	/**
	 * Instantiate.
	 * 
	 * @param region
	 *            {@link Region} being resized.
	 * @param orientation
	 *            {@link Orientation}.
	 */
	private DragResizer(Region region, Orientation orientation) {
		this.region = region;
		this.orientation = orientation;
	}

	/**
	 * Indicates if in the drag zone.
	 * 
	 * @param event
	 *            {@link MouseEvent}.
	 * @return <code>true</code> if in drag zone.
	 */
	protected boolean isInDraggableZone(MouseEvent event) {
		switch (this.orientation) {
		case HORIZONTAL:
			return event.getX() > (region.getWidth() - RESIZE_MARGIN);
		default:
			// VERTICAL
			return event.getY() > (region.getHeight() - RESIZE_MARGIN);
		}
	}

	/**
	 * Handle mouse over.
	 * 
	 * @param event
	 *            {@link MouseEvent}.
	 */
	protected void mouseOver(MouseEvent event) {
		if (isInDraggableZone(event) || this.dragging) {
			this.region.setCursor(Cursor.S_RESIZE);
		} else {
			this.region.setCursor(Cursor.DEFAULT);
		}
	}

	/**
	 * Handle start dragging.
	 * 
	 * @param event
	 *            {@link MouseEvent}.
	 */
	protected void mousePressed(MouseEvent event) {

		// Ignore clicks outside drag margin
		if (!isInDraggableZone(event)) {
			return;
		}

		// Start dragging
		this.dragging = true;
	}

	/**
	 * Handle dragging to resize.
	 * 
	 * @param event
	 *            {@link MouseEvent}.
	 */
	protected void mouseDragged(MouseEvent event) {
		// Ensure dragging
		if (!dragging) {
			return;
		}

		// Update pref dimension based on drag
		switch (this.orientation) {
		case HORIZONTAL:
			this.region.setPrefWidth(event.getX());
			break;

		default:
			// VERTICAL
			this.region.setPrefHeight(event.getY());
			break;
		}
	}

	/**
	 * Handle mouse released.
	 * 
	 * @param event
	 *            {@link MouseEvent}.
	 */
	protected void mouseReleased(MouseEvent event) {
		this.dragging = false;
		this.region.setCursor(Cursor.DEFAULT);
	}

}
