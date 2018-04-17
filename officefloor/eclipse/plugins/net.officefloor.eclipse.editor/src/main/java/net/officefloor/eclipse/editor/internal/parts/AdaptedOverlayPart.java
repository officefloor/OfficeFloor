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
package net.officefloor.eclipse.editor.internal.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.mvc.fx.parts.IContentPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.internal.models.AdaptedOverlay;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the {@link AdaptedOverlay}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedOverlayPart extends AbstractAdaptedPart<AdaptedOverlay, AdaptedOverlay, Pane> {

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected Pane doCreateVisual() {

		// Obtain the overlay parent
		Pane overlayParent = this.getContent().getOverlayParent();

		// Configure the overlay parent
		this.getContent().getOverlayVisualFactory().loadOverlay(this.getContent());

		// Specify location of overlay
		Model overlay = this.getContent();
		overlayParent.setLayoutX(overlay.getX());
		overlayParent.setLayoutY(overlay.getY());

		// Return the overlay parent
		return overlayParent;
	}

	@Override
	protected void doRefreshVisual(Pane visual) {
		// Nothing to refresh
	}

}