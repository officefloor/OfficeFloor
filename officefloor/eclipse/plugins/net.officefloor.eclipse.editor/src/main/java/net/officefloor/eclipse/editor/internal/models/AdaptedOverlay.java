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
package net.officefloor.eclipse.editor.internal.models;

import org.eclipse.gef.geometry.planar.Point;

import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.OverlayVisualContext;
import net.officefloor.eclipse.editor.OverlayVisualFactory;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.AbstractModel;

/**
 * Overlay within the editor.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedOverlay extends AbstractModel implements AdaptedModel<AdaptedOverlay>, OverlayVisualContext {

	/**
	 * Overlay parent {@link Pane}.
	 */
	private final Pane overlayParent = new Pane();

	/**
	 * {@link OverlayVisualFactory}.
	 */
	private final OverlayVisualFactory visualFactory;

	/**
	 * {@link OfficeFloorContentPartFactory}.
	 */
	private final OfficeFloorContentPartFactory<?, ?> contentPartFactory;

	/**
	 * Instantiate.
	 * 
	 * @param location
	 *            Location.
	 * @param visualFactory
	 *            {@link OverlayVisualFactory}.
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	public AdaptedOverlay(Point location, OverlayVisualFactory visualFactory,
			OfficeFloorContentPartFactory<?, ?> contentPartFactory) {
		this.visualFactory = visualFactory;
		this.contentPartFactory = contentPartFactory;

		// Specify location
		this.setX((int) location.x);
		this.setY((int) location.y);
	}

	/**
	 * Obtains the {@link OverlayVisualFactory}.
	 * 
	 * @return {@link OverlayVisualFactory}.
	 */
	public OverlayVisualFactory getOverlayVisualFactory() {
		return this.visualFactory;
	}

	/*
	 * ============= OverlayVisualContext ==============
	 */

	@Override
	public Pane getOverlayParent() {
		return this.overlayParent;
	}

	@Override
	public void close() {
		this.contentPartFactory.removeOverlay(this);
	}

	/*
	 * ============== AdaptedModel ====================
	 */

	@Override
	public AdaptedOverlay getModel() {
		return this;
	}

}