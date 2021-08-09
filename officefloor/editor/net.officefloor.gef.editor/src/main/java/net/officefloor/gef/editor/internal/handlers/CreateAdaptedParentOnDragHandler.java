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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.gestures.ClickDragGesture;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnDragHandler;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.operations.DeselectOperation;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.LayeredRootPart;
import org.eclipse.gef.mvc.fx.policies.CreationPolicy;
import org.eclipse.gef.mvc.fx.policies.DeletionPolicy;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.common.collect.HashMultimap;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import net.officefloor.gef.editor.AdaptedArea;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnection;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedConnectorRole;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.AdaptedPotentialConnection;
import net.officefloor.gef.editor.ChildrenGroup;
import net.officefloor.gef.editor.ModelAction;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.editor.internal.models.AdaptedPrototype;
import net.officefloor.gef.editor.internal.parts.AdaptedParentPart;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

public class CreateAdaptedParentOnDragHandler<R extends Model, O, M extends Model> extends AbstractHandler
		implements IOnDragHandler {

	/**
	 * {@link AdaptedPrototype}.
	 */
	private AdaptedPrototype<M> prototype;

	/**
	 * {@link AdaptedParentPart}.
	 */
	private AdaptedParentPart<M> prototypePart;

	/**
	 * {@link IOnDragHandler} instances.
	 */
	private Map<AdapterKey<? extends IOnDragHandler>, IOnDragHandler> dragPolicies;

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
	 * Obtains the location of the {@link MouseEvent}.
	 * 
	 * @param event {@link MouseEvent}.
	 * @return Location of the {@link MouseEvent}.
	 */
	protected Point2D getLocation(MouseEvent event) {
		return ((LayeredRootPart) getContentViewer().getRootPart()).getContentLayer().sceneToLocal(event.getSceneX(),
				event.getSceneY());
	}

	/**
	 * Completes the drag.
	 * 
	 * @param isCreateAdaptedParent Indicates whether to create the
	 *                              {@link AdaptedParentPart}.
	 * @param event                 {@link MouseEvent} at completion of drag.
	 */
	protected void completeDrag(boolean isCreateAdaptedParent, MouseEvent event) {
		this.prototypePart.getErrorHandler().isError(() -> {
			// Obtain location to create parent
			Point2D location = this.getLocation(event);

			// Delete the proxy
			this.restoreRefreshVisuals(this.prototypePart);
			IRootPart<? extends Node> contentRoot = this.getContentViewer().getRootPart();
			DeletionPolicy deletionPolicy = contentRoot.getAdapter(DeletionPolicy.class);
			init(deletionPolicy);
			deletionPolicy.delete(this.prototypePart);
			commit(deletionPolicy);

			// Determine if create adapted parent
			if (isCreateAdaptedParent) {

				// Create the parent at the location
				this.prototype.newAdaptedParent(new Point(location.getX(), location.getY()));
			}
		});

		// Clear state
		this.prototype = null;
		this.prototypePart = null;
		this.dragPolicies = null;
	}

	/*
	 * ==================== IOnDragHandler ===========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public AdaptedParentPart<M> getHost() {
		return (AdaptedParentPart<M>) super.getHost();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void startDrag(MouseEvent event) {

		// Obtain the parent
		AdaptedParentPart<M> parentPart = this.getHost();
		AdaptedParent<M> parent = parentPart.getContent();

		// Determine if select only
		if (parent.getSelectOnly() != null) {
			return; // select only
		}

		// Create proxy to create parent at particular location
		this.prototype = parentPart.getAdapter(AdaptedPrototype.class);
		if (this.prototype == null) {
			throw new IllegalStateException(AdaptedParent.class.getSimpleName() + " does not adapt to "
					+ AdaptedPrototype.class.getSimpleName() + " for model " + parent.getModel().getClass().getName());
		}
		parentPart.getErrorHandler().isError(() -> {

			// Create proxy visual for feedback of drag
			ProxyCreateAdaptedParent proxy = new ProxyCreateAdaptedParent(parent, this.prototype);

			// Initially position (drag anchor keeps in sync to relative mouse movement)
			Point2D location = this.getLocation(event);
			proxy.getModel().setX((int) location.getX());
			proxy.getModel().setY((int) location.getY());

			// Create part for proxy to visual parent in drag
			IRootPart<? extends Node> contentRoot = this.getContentViewer().getRootPart();
			CreationPolicy creationPolicy = contentRoot.getAdapter(CreationPolicy.class);
			init(creationPolicy);
			this.prototypePart = (AdaptedParentPart<M>) creationPolicy.create(proxy, contentRoot,
					HashMultimap.create());
			commit(creationPolicy);

			// Disable refresh
			this.storeAndDisableRefreshVisuals(this.prototypePart);

			// Deselect all but the new part
			List<IContentPart<? extends Node>> deselected = new ArrayList<>(
					this.getContentViewer().getAdapter(SelectionModel.class).getSelectionUnmodifiable());
			deselected.remove(this.prototypePart);
			DeselectOperation deselectOperation = new DeselectOperation(this.getContentViewer(), deselected);
			try {
				this.getHost().getRoot().getViewer().getDomain().execute(deselectOperation, new NullProgressMonitor());
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}

			// Trigger drag start policies
			this.dragPolicies = this.prototypePart.getAdapters(ClickDragGesture.ON_DRAG_POLICY_KEY);
			if (this.dragPolicies != null) {
				for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
					dragPolicy.startDrag(event);
				}
			}
		});
	}

	@Override
	public void drag(MouseEvent event, Dimension delta) {
		if (this.prototypePart == null) {
			return;
		}

		// Forward drag event
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.drag(event, delta);
			}
		}
	}

	@Override
	public void abortDrag() {
		if (this.prototypePart == null) {
			return;
		}

		// Forward drag event
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.abortDrag();
			}
		}

		// Finished drag
		this.completeDrag(false, null);
	}

	@Override
	public void endDrag(MouseEvent event, Dimension delta) {
		if (this.prototypePart == null) {
			return;
		}

		// Forward drag event
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.endDrag(event, delta);
			}
		}

		// Create the adapted parent at end drag location
		this.completeDrag(true, event);
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

	/**
	 * Proxy create {@link AdaptedParent} via {@link AdaptedPrototype}.
	 */
	private class ProxyCreateAdaptedParent implements AdaptedPrototype<M>, AdaptedParent<M> {

		/**
		 * {@link AdaptedParent}.
		 */
		private final AdaptedParent<M> parent;

		/**
		 * {@link AdaptedPrototype}.
		 */
		private final AdaptedPrototype<M> prototype;

		/**
		 * Instantiate.
		 * 
		 * @param parent    {@link AdaptedParent}.
		 * @param prototype {@link AdaptedPrototype}.
		 */
		public ProxyCreateAdaptedParent(AdaptedParent<M> parent, AdaptedPrototype<M> prototype) {
			this.parent = parent;
			this.prototype = prototype;
		}

		/*
		 * ===================== AdaptedParent ===================
		 */

		@Override
		public void newAdaptedParent(Point location) {
			this.prototype.newAdaptedParent(location);
		}

		/*
		 * ===================== AdaptedParent ===================
		 */

		@Override
		public ReadOnlyProperty<String> getLabel() {
			return this.parent.getLabel();
		}

		@Override
		public Property<String> getEditLabel() {
			return null;
		}

		@Override
		public Property<String> getStylesheet() {
			return this.parent.getStylesheet();
		}

		@Override
		public ReadOnlyProperty<URL> getStylesheetUrl() {
			return this.parent.getStylesheetUrl();
		}

		@Override
		public List<AdaptedConnection<?>> getConnections() {
			return Collections.emptyList();
		}

		@Override
		public M getModel() {
			return this.parent.getModel();
		}

		@Override
		public AdaptedModel<?> getParent() {
			return this.parent.getParent();
		}

		@Override
		public List<AdaptedArea<?>> getAdaptedAreas() {
			return this.parent.getAdaptedAreas();
		}

		@Override
		public boolean isAreaChangeEvent(String eventName) {
			return this.parent.isAreaChangeEvent(eventName);
		}

		@Override
		public boolean isPalettePrototype() {
			return this.parent.isPalettePrototype();
		}

		@Override
		public void changeLocation(int x, int y) {
		}

		@Override
		public <T> T getAdapter(Class<T> classKey) {
			return this.parent.getAdapter(classKey);
		}

		@Override
		public List<ChildrenGroup<M, ?>> getChildrenGroups() {
			return this.parent.getChildrenGroups();
		}

		@Override
		public List<AdaptedConnector<M>> getAdaptedConnectors() {
			return this.parent.getAdaptedConnectors();
		}

		@Override
		public AdaptedConnector<M> getAdaptedConnector(Class<? extends ConnectionModel> connectionClass,
				AdaptedConnectorRole type) {
			return this.parent.getAdaptedConnector(connectionClass, type);
		}

		@Override
		public <T extends Model> AdaptedPotentialConnection getPotentialConnection(AdaptedConnectable<T> target) {
			return this.parent.getPotentialConnection(target);
		}

		@Override
		public <T extends Model> void createConnection(AdaptedConnectable<T> target, AdaptedConnectorRole sourceRole) {
			this.parent.createConnection(target, sourceRole);
		}

		@Override
		public Node createVisual(AdaptedChildVisualFactoryContext<M> context) {
			return this.parent.createVisual(context);
		}

		@Override
		public <r extends Model, o> void action(ModelAction<r, o, M> action) {
			this.parent.action(action);
		}

		@Override
		public AdaptedErrorHandler getErrorHandler() {
			return this.parent.getErrorHandler();
		}

		@Override
		public int getDragLatency() {
			return this.parent.getDragLatency();
		}

		@Override
		public SelectOnly getSelectOnly() {
			return this.parent.getSelectOnly();
		}
	}

}
