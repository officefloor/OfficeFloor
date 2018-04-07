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

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;

import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.internal.handlers.CreateAdaptedConnectionOnDragHandler;
import net.officefloor.eclipse.editor.internal.models.ActiveConnectionSourceModel;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnector;

/**
 * {@link IContentPart} for the {@link AdaptedConnector}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnectorPart extends AbstractContentPart<GeometryNode<?>> {

	@Inject
	private ActiveConnectionSourceModel activeConnectionSource;

	/**
	 * Specifies this as the active {@link AdaptedConnectorPart} for the
	 * {@link CreateAdaptedConnectionOnDragHandler}.
	 */
	public void setActiveConnector(boolean isActive) {
		if (isActive) {
			this.activeConnectionSource.setActiveAdaptedChild(this.getContent().getParentAdaptedChild());
		} else {
			this.activeConnectionSource.setActiveAdaptedChild(null);
		}
	}

	/*
	 * ================= IContentPart ====================
	 */

	@Override
	public AdaptedConnector<?> getContent() {
		return (AdaptedConnector<?>) super.getContent();
	}

	@Override
	public void setContent(Object content) {
		if (content != null && !(content instanceof AdaptedConnector)) {
			throw new IllegalArgumentException("Only " + AdaptedConnector.class.getSimpleName() + " supported.");
		}
		super.setContent(content);

		// Listen in on changes to determine if can be a connector
		this.activeConnectionSource.getActiveAdaptedChild().addListener((change) -> {
			AdaptedChild<?> activeChild = this.activeConnectionSource.getActiveAdaptedChild().get();

			// Determine if clear active child
			if (activeChild == null) {
				this.getVisual().visibleProperty().set(true);
				return;
			}

			// Ensure have content
			if (this.getContent() == null) {
				return;
			}

			// Keep this child visible
			if (activeChild == this.getContent().getParentAdaptedChild()) {
				return;
			}

			// Determine if can be connected to from the active child
			boolean isAbleToConnect = activeChild.canConnect(this.getContent().getParentAdaptedChild());

			// Display based on whether can connect
			this.getVisual().visibleProperty().set(isAbleToConnect);
		});
	}

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected GeometryNode<?> doCreateVisual() {

		// Obtain the parent
		AdaptedChildPart<?, ?> parent = (AdaptedChildPart<?, ?>) this.getParent();

		// Obtain the node for the connector
		GeometryNode<?> node = parent.getAdaptedConnectorNode(this.getContent());

		// Add the children group name for CSS
		node.getStyleClass().add("connector");
		node.getStyleClass().add(this.getContent().getConnectionModelClass().getSimpleName());

		// Return the visual
		return node;
	}

	@Override
	protected void doRefreshVisual(GeometryNode<?> visual) {
	}

}