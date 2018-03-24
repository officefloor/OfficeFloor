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
import org.eclipse.gef.fx.nodes.OrthogonalRouter;
import org.eclipse.gef.fx.nodes.PolyBezierInterpolator;
import org.eclipse.gef.mvc.fx.parts.IBendableContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.providers.IAnchorProvider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.eclipse.editor.models.ProxyAdaptedConnection;
import net.officefloor.model.ConnectionModel;

public class AdaptedConnectionPart<C extends ConnectionModel>
		extends AbstractAdaptedPart<C, AdaptedConnection<C>, Connection> implements IBendableContentPart<Connection> {

	/**
	 * Capture {@link IVisualPart} parent on removing to enable handles to update.
	 */
	private IVisualPart<? extends Node> parent = null;

	/**
	 * Source {@link AdaptedChild}.
	 */
	private AdaptedConnector<?> sourceConnector;

	/**
	 * Target {@link AdaptedChild}.
	 */
	private AdaptedConnector<?> targetConnector;

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		SetMultimap<Object, String> anchorages = HashMultimap.create();

		// Determine if proxy connection
		if (this.getContent() instanceof ProxyAdaptedConnection) {
			ProxyAdaptedConnection proxy = (ProxyAdaptedConnection) this.getContent();
			anchorages.put(proxy.getSourceAdaptedConnector(), SOURCE_ROLE);
			return anchorages; // never connected
		}

		// Load the source
		AdaptedChild<?> sourceChild = this.getContent().getSource();
		if (sourceChild != null) {
			this.sourceConnector = sourceChild.getAdaptedConnector(this.getContent().getModel().getClass());
			anchorages.put(this.sourceConnector, SOURCE_ROLE);
		}

		// Load the target
		AdaptedChild<?> targetChild = this.getContent().getTarget();
		if (targetChild != null) {
			this.targetConnector = targetChild.getAdaptedConnector(this.getContent().getModel().getClass());
			anchorages.put(this.targetConnector, TARGET_ROLE);
		}

		// Return the anchorages
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

		// Create the connection
		Connection connection = new Connection();
		connection.setInterpolator(new PolyBezierInterpolator());
		connection.setRouter(new OrthogonalRouter());

		// Add the children group name for CSS
		connection.getStyleClass().add("connection");
		if (this.getContent().getModel() != null) {
			connection.getStyleClass().add(this.getContent().getModel().getClass().getSimpleName());
		}

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
	public IVisualPart<? extends Node> getParent() {
		if (this.parent != null) {
			return this.parent;
		}
		return super.getParent();
	}

	@Override
	public void setContentBendPoints(List<BendPoint> bendPoints) {

		// Load the bend points to proxy (to create actual connection)
		if (this.getContent() instanceof ProxyAdaptedConnection) {

			// Load bend points for proxy connection to create connection
			ProxyAdaptedConnection proxy = (ProxyAdaptedConnection) this.getContent();
			proxy.setBendPoints(bendPoints);

			// Capture parent (so delete still considers in model)
			this.parent = this.getParent();

			// Return to create the connection
			return;
		}

		// Actual connection change, so determine if still connected
		if (bendPoints.size() >= 2) {
			BendPoint start = bendPoints.get(0);
			BendPoint end = bendPoints.get(bendPoints.size() - 1);

			// Obtain the anchors
			AdaptedConnector<?> startConnector = (AdaptedConnector<?>) start.getContentAnchorage();
			AdaptedConnector<?> endConnector = (AdaptedConnector<?>) end.getContentAnchorage();

			// Determine if no change to connector
			if ((startConnector == this.sourceConnector) && (endConnector == this.targetConnector)) {
				return; // no change to connector
			}

			// Capture parent (so considers still in model for updating handles)
			this.parent = this.getParent();

			// Detached, so remove the connection
			this.getContent().remove();

			// Determine if connected to new anchors (and connect if so)
			if ((start.isAttached()) && (end.isAttached())) {
				startConnector.getParentAdaptedChild().createConnection(endConnector.getParentAdaptedChild());
			}
		}
	}

}