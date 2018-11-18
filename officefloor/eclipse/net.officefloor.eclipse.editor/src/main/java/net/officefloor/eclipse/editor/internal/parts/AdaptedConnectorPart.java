/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor.internal.parts;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import net.officefloor.eclipse.editor.AdaptedConnector;
import net.officefloor.eclipse.editor.AdaptedConnectorRole;
import net.officefloor.eclipse.editor.AdaptedPotentialConnection;
import net.officefloor.eclipse.editor.internal.handlers.CreateAdaptedConnectionOnDragHandler;
import net.officefloor.eclipse.editor.internal.models.ActiveConnectionSourceModel;
import net.officefloor.eclipse.editor.internal.models.ActiveConnectionSourceModel.ActiveConnectionSource;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnectorImpl;

/**
 * {@link IContentPart} for the {@link AdaptedConnectorImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnectorPart extends AbstractContentPart<Region> {

	@Inject
	private ActiveConnectionSourceModel activeConnectionSource;

	/**
	 * Specifies this as the active {@link AdaptedConnectorPart} for the
	 * {@link CreateAdaptedConnectionOnDragHandler}.
	 * 
	 * @param isActive
	 *            Indicates if active.
	 */
	public void setActiveConnector(boolean isActive) {
		if (isActive) {
			this.activeConnectionSource.setActiveSource(this.getContent().getParentAdaptedConnectable(),
					this.getContent().getAssociationRole());
		} else {
			this.activeConnectionSource.setActiveSource(null, null);
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
		if (content != null && !(content instanceof AdaptedConnectorImpl)) {
			throw new IllegalArgumentException("Only " + AdaptedConnectorImpl.class.getSimpleName() + " supported.");
		}
		super.setContent(content);

		// Listen in on changes to determine if can be a connector
		this.activeConnectionSource.activeSource().addListener((change) -> {
			ActiveConnectionSource activeSource = this.activeConnectionSource.activeSource().get();

			// Determine if clear active source
			if (activeSource == null) {
				this.getVisual().visibleProperty().set(true);
				return;
			}

			// Ensure have content
			if (this.getContent() == null) {
				return;
			}

			// Keep this child visible
			if (activeSource.getSource() == this.getContent().getParentAdaptedConnectable()) {
				return;
			}

			// Determine if can be connected to from the active child
			boolean isAbleToConnect = false;
			AdaptedPotentialConnection potentialConnection = activeSource.getSource()
					.getPotentialConnection(this.getContent().getParentAdaptedConnectable());
			if ((potentialConnection != null) && (potentialConnection.canCreateConnection())) {
				isAbleToConnect = true;

				// Determine if connection to same type
				AdaptedConnectorRole activeRole = activeSource.getRole();
				if ((potentialConnection.getSourceModelClass() == potentialConnection.getTargetModelClass())
						&& (activeRole != null)) {
					// Connection to same type (so determine if same role)
					if (activeRole.equals(this.getContent().getAssociationRole())) {
						isAbleToConnect = false;
					}
				}
			}

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
	protected Region doCreateVisual() {

		// Obtain the parent
		AdaptedChildPart<?, ?> parent = (AdaptedChildPart<?, ?>) this.getParent();

		// Obtain the node for the connector
		Region node = parent.getAdaptedConnectorNode(this.getContent());

		// Add the children group name for CSS
		node.getStyleClass().add("connector");
		node.getStyleClass().add(this.getContent().getConnectionModelClass().getSimpleName());

		// Determine if able to create connection from connector node
		node.getStyleClass()
				.add(this.getContent().isAssociationCreateConnection() ? "connector-create" : "connector-not-create");

		// Return the visual
		return node;
	}

	@Override
	protected void doRefreshVisual(Region visual) {
	}

	@Override
	protected void unregisterFromVisualPartMap(IViewer viewer, Region visual) {
		// AdaptedConnectorPart registered multiple times to same node (so handle)
		Map<Node, IVisualPart<? extends Node>> registry = viewer.getVisualPartMap();
		if (registry.get(visual) == this) {
			registry.remove(visual);
		}
	}

}