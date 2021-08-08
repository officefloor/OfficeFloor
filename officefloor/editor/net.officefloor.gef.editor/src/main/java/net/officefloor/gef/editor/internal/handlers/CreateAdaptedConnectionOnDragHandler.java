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

package net.officefloor.gef.editor.internal.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.fx.nodes.StraightRouter;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.gestures.ClickDragGesture;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnDragHandler;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.operations.DeselectOperation;
import org.eclipse.gef.mvc.fx.parts.CircleSegmentHandlePart;
import org.eclipse.gef.mvc.fx.parts.IBendableContentPart.BendPoint;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.policies.CreationPolicy;
import org.eclipse.gef.mvc.fx.policies.DeletionPolicy;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multiset;

import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.internal.models.ProxyAdaptedConnection;
import net.officefloor.gef.editor.internal.parts.AdaptedConnectablePart;
import net.officefloor.gef.editor.internal.parts.AdaptedConnectionPart;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;

public class CreateAdaptedConnectionOnDragHandler<R extends Model, O> extends AbstractHandler
		implements IOnDragHandler {

	/**
	 * Source {@link AdaptedConnectablePart}.
	 */
	private AdaptedConnectablePart sourceConnector;

	/**
	 * Drag latency. Increasing helps improve drag performance (but reduces
	 * responsiveness).
	 */
	private int dragLatency = OfficeFloorContentPartFactory.DEFAULT_DRAG_LATENCY;

	/**
	 * Count of the drag events.
	 */
	private int dragEventCount = 0;

	/**
	 * {@link ProxyAdaptedConnection}.
	 */
	private ProxyAdaptedConnection<R, O> connection;

	/**
	 * {@link AdaptedConnectionPart} for the new {@link ProxyAdaptedConnection}.
	 */
	private AdaptedConnectionPart<R, O, ?> connectionPart;

	/**
	 * Target {@link BendPoint} for dragging the {@link ProxyAdaptedConnection}.
	 */
	private CircleSegmentHandlePart bendTargetPart;

	/**
	 * Additional {@link IOnDragHandler} instances.
	 */
	private Map<AdapterKey<? extends IOnDragHandler>, IOnDragHandler> dragPolicies;

	/**
	 * Obtains the {@link AdaptedConnectablePart} for the start of the drag.
	 * 
	 * @return {@link AdaptedConnectablePart}.
	 */
	protected AdaptedConnectablePart getAdaptedConnectablePart() {
		return (AdaptedConnectablePart) getHost();
	}

	/**
	 * Finds the {@link CircleSegmentHandlePart} for the target {@link BendPoint}.
	 * 
	 * @param connectionPart {@link AdaptedConnectionPart}.
	 * @param eventTarget    {@link EventTarget}.
	 * @return Target {@link CircleSegmentHandlePart}.
	 */
	protected CircleSegmentHandlePart findBendTargetPart(AdaptedConnectionPart<R, O, ?> connectionPart,
			EventTarget eventTarget) {
		// Find last segment handle part
		Multiset<IVisualPart<? extends Node>> anchoreds = connectionPart.getAnchoredsUnmodifiable();
		for (IVisualPart<? extends Node> anchored : anchoreds) {
			if (anchored instanceof CircleSegmentHandlePart) {
				CircleSegmentHandlePart circleSegmentHandlePart = (CircleSegmentHandlePart) anchored;
				if (circleSegmentHandlePart.getSegmentParameter() == 1.0) {
					return circleSegmentHandlePart;
				}
			}
		}
		throw new IllegalStateException("Unable to obtain bend target part on creating new connection");
	}

	/**
	 * Obtains the content {@link IViewer}.
	 * 
	 * @return Content {@link IViewer}.
	 */
	protected IViewer getContentViewer() {
		return getHost().getRoot().getViewer().getDomain()
				.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
	}

	/**
	 * Cleans up the drag.
	 */
	protected void cleanupDrag() {

		// Remove the connection
		this.restoreRefreshVisuals(this.connectionPart);
		IRootPart<? extends Node> contentRoot = this.getContentViewer().getRootPart();
		DeletionPolicy deletionPolicy = contentRoot.getAdapter(DeletionPolicy.class);
		init(deletionPolicy);
		deletionPolicy.delete(this.connectionPart);
		commit(deletionPolicy);

		// Connector no longer active
		this.sourceConnector.setActiveConnector(false);

		// Clear drag details
		this.sourceConnector = null;
		this.connectionPart = null;
		this.bendTargetPart = null;
		this.dragPolicies = null;
	}

	/*
	 * ==================== IOnDragHandler =========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void startDrag(MouseEvent event) {

		// Determine if able to create a connection
		AdaptedConnectablePart sourceConnectablePart = this.getAdaptedConnectablePart();
		if (!sourceConnectablePart.getContent().isAssociationCreateConnection()) {
			return;
		}

		// Determine if select only
		AdaptedConnectable<?> parentConnectable = sourceConnectablePart.getContent().getParentAdaptedConnectable();
		if (parentConnectable.getSelectOnly() != null) {
			return; // select only
		}

		// Set up drag latency for new drag
		this.dragLatency = parentConnectable.getDragLatency();
		this.dragEventCount = 0;

		// Create the proxy connection
		this.sourceConnector = sourceConnectablePart;
		this.connection = new ProxyAdaptedConnection<>(this.sourceConnector.getContent());

		// Register the connector as active
		this.sourceConnector.setActiveConnector(true);

		// Create using CreationPolicy from root part
		CreationPolicy creationPolicy = getHost().getRoot().getAdapter(CreationPolicy.class);
		init(creationPolicy);
		this.connectionPart = (AdaptedConnectionPart<R, O, ?>) creationPolicy.create(this.connection,
				getHost().getRoot(), HashMultimap.<IContentPart<? extends Node>, String>create());
		this.connectionPart.getVisual().setRouter(new StraightRouter()); // avoid orthogonal routing issues
		commit(creationPolicy);

		// Build operation to deselect all but the new connection part
		List<IContentPart<? extends Node>> deselected = new ArrayList<>(
				getHost().getRoot().getViewer().getAdapter(SelectionModel.class).getSelectionUnmodifiable());
		deselected.remove(this.connectionPart);
		DeselectOperation deselectOperation = new DeselectOperation(getHost().getRoot().getViewer(), deselected);
		try {
			getHost().getRoot().getViewer().getDomain().execute(deselectOperation, null);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

		// Disable refresh visuals for the connection
		this.storeAndDisableRefreshVisuals(this.connectionPart);

		// Specify location of connection end point
		Point2D localPoint = ((InfiniteCanvasViewer) getHost().getRoot().getViewer()).getCanvas()
				.sceneToLocal(event.getSceneX(), event.getSceneY());
		Point location = new Point(localPoint.getX(), localPoint.getY());
		this.connectionPart.getVisual().setEndPoint(location);

		// Start the dragging
		this.bendTargetPart = this.findBendTargetPart(this.connectionPart, event.getTarget());
		this.dragPolicies = this.bendTargetPart.getAdapters(ClickDragGesture.ON_DRAG_POLICY_KEY);
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.startDrag(event);
			}
		}
	}

	@Override
	public void drag(MouseEvent event, Dimension delta) {
		if (this.bendTargetPart == null) {
			return;
		}

		// Provide latency to improve drag performance
		this.dragEventCount++;
		if ((this.dragEventCount % this.dragLatency) != 0) {
			return;
		}

		// Forward events
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.drag(event, delta);
			}
		}
	}

	@Override
	public void abortDrag() {
		if (this.bendTargetPart == null) {
			return;
		}

		// Forward events
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.abortDrag();
			}
		}

		// Clean up the drag
		this.cleanupDrag();
	}

	@Override
	public void endDrag(MouseEvent event, Dimension delta) {
		if (this.bendTargetPart == null) {
			return;
		}

		// Forward events
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.endDrag(event, delta);
			}
		}

		// Obtain the target
		BendPoint targetBendPoint = this.connection.getTargetBendPoint();
		AdaptedConnector<?> target = (AdaptedConnector<?>) targetBendPoint.getContentAnchorage();
		if (target != null) {

			// Have target so create the connection
			AdaptedConnector<?> source = this.connection.getSourceAdaptedConnector();
			source.getParentAdaptedConnectable().createConnection(target.getParentAdaptedConnectable(),
					this.sourceConnector.getContent().getAssociationRole());
		}

		// Clean up the drag
		this.cleanupDrag();
	}

	@Override
	public void hideIndicationCursor() {
	}

	@Override
	public boolean showIndicationCursor(KeyEvent event) {
		return false;
	}

	@Override
	public boolean showIndicationCursor(MouseEvent event) {
		return false;
	}

}
