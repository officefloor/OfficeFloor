/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.configurer.internal.resize;

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
	 */
	public static void makeResizable(Region region) {
		final DragResizer resizer = new DragResizer(region);
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
	 * Indicates currently dragging.
	 */
	private boolean dragging;

	/**
	 * Instantiate.
	 * 
	 * @param region
	 *            {@link Region} being resized.
	 */
	private DragResizer(Region region) {
		this.region = region;
	}

	/**
	 * Indicates if in the drag zone.
	 * 
	 * @param event
	 *            {@link MouseEvent}.
	 * @return <code>true</code> if in drag zone.
	 */
	protected boolean isInDraggableZone(MouseEvent event) {
		return event.getY() > (region.getHeight() - RESIZE_MARGIN);
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

		// Update pref height based on drag
		this.region.setPrefHeight(event.getY());
	}

	protected void mouseReleased(MouseEvent event) {
		this.dragging = false;
		this.region.setCursor(Cursor.DEFAULT);
	}

}