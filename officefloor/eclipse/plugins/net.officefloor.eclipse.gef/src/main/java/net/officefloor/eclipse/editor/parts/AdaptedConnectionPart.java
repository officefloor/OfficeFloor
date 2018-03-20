/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.fx.anchors.IAnchor;
import org.eclipse.gef.fx.nodes.Connection;
import org.eclipse.gef.fx.nodes.PolyBezierInterpolator;
import org.eclipse.gef.mvc.fx.parts.IBendableContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.providers.IAnchorProvider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.model.ConnectionModel;

public class AdaptedConnectionPart<C extends ConnectionModel>
		extends AbstractAdaptedPart<C, AdaptedConnection<C>, Connection> implements IBendableContentPart<Connection> {

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		SetMultimap<Object, String> anchorages = HashMultimap.create();
		AdaptedConnector<?> source = this.getContent().getSource()
				.getAdaptedConnector(this.getContent().getModel().getClass());
		anchorages.put(source, SOURCE_ROLE);
		AdaptedConnector<?> target = this.getContent().getTarget()
				.getAdaptedConnector(this.getContent().getModel().getClass());
		anchorages.put(target, TARGET_ROLE);
		return anchorages;
	}

	@Override
	protected void doAttachToAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
		if (!(anchorage instanceof AdaptedConnectorPart)) {
			throw new IllegalStateException("Attempting to attach non " + AdaptedConnectorPart.class.getSimpleName()
					+ " anchor to " + this.getClass().getSimpleName() + " for model "
					+ this.getContent().getModel().getClass().getName());
		}

		// Anchoring
		IAnchorProvider anchorProvider = anchorage.getAdapter(IAnchorProvider.class);
		if (anchorProvider == null) {
			// Needs to be in hierarchy to obtain adapters for view
			if (anchorage.getParent() == null) {
				throw new IllegalStateException("No parent for " + anchorage.getClass().getName());
			}

			// Should have anchor provider
			throw new IllegalStateException(
					"No " + IAnchorProvider.class.getSimpleName() + " provided by " + anchorage.getClass().getName());
		}
		IAnchor anchor = anchorProvider.get(this, role);
		if (role.equals(SOURCE_ROLE)) {
			this.getVisual().setStartAnchor(anchor);
		} else if (role.equals(TARGET_ROLE)) {
			this.getVisual().setEndAnchor(anchor);
		} else {
			throw new IllegalStateException("Cannot attach to anchor with role '" + role + "' for model "
					+ this.getContent().getClass().getName());
		}
	}

	@Override
	protected void doDetachFromAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
		// Should be removed via operations
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected Connection doCreateVisual() {
		Connection connection = new Connection();
		connection.setInterpolator(new PolyBezierInterpolator());
		return connection;
	}

	@Override
	protected void doRefreshVisual(Connection visual) {
	}

	@Override
	public List<BendPoint> getContentBendPoints() {
		return Collections.emptyList();
	}

	@Override
	public void setContentBendPoints(List<BendPoint> bendPoints) {

		// Update the bend points
		for (BendPoint point : bendPoints) {
			if (!point.isAttached()) {
				System.out.println("TODO REMOVE bp " + point.getPosition() + " [disconnect]");
			} else {
				AdaptedConnector<?> connector = (AdaptedConnector<?>) point.getContentAnchorage();
				System.out.println("TODO REMOVE bp " + point.getPosition() + " - "
						+ connector.getParentAdaptedChild().getModel().getClass().getName());
			}
		}
	}

}