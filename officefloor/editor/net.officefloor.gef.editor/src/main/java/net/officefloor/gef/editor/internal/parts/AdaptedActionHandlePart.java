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

package net.officefloor.gef.editor.internal.parts;

import java.util.Set;

import org.eclipse.gef.common.collections.SetMultimapChangeListener;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnClickHandler;
import org.eclipse.gef.mvc.fx.parts.AbstractHandlePart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedActionVisualFactoryContext;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.gef.editor.internal.models.AdaptedAction;
import net.officefloor.model.Model;

public class AdaptedActionHandlePart<R extends Model, O, M extends Model> extends AbstractHandlePart<Node>
		implements AdaptedActionVisualFactoryContext {

	/**
	 * Indicates if registered.
	 */
	private boolean registered = false;

	/**
	 * {@link AdaptedAction}.
	 */
	private AdaptedAction<R, O, M> adaptedAction;

	/**
	 * {@link IOnClickHandler} for the {@link AdaptedAction}.
	 */
	private class AdaptedActionIOnClickHandler extends AbstractHandler implements IOnClickHandler {
		@Override
		public void click(MouseEvent event) {
			AdaptedActionHandlePart.this.adaptedAction.execute();
			event.consume();
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param adaptedAction {@link AdaptedAction}.
	 */
	public void setAdaptedAction(AdaptedAction<R, O, M> adaptedAction) {
		this.adaptedAction = adaptedAction;

		// Handle click action
		this.setAdapter(new AdaptedActionIOnClickHandler());
	}

	/**
	 * {@link SetMultimapChangeListener} for change in anchorages.
	 */
	private final SetMultimapChangeListener<IVisualPart<? extends Node>, String> parentAnchoragesChangeListener = (
			change) -> {
		IViewer oldViewer = getViewer(change.getPreviousContents().keySet());
		IViewer newViewer = getViewer(change.getSetMultimap().keySet());
		if (registered && oldViewer != null && oldViewer != newViewer) {
			oldViewer.unsetAdapter(AdaptedActionHandlePart.this);
		}
		if (!registered && newViewer != null && oldViewer != newViewer) {
			newViewer.setAdapter(AdaptedActionHandlePart.this,
					String.valueOf(System.identityHashCode(AdaptedActionHandlePart.this)));
		}
	};

	/**
	 * Obtains the {@link IViewer} for the {@link IVisualPart} anchorages.
	 * 
	 * @param anchorages {@link IVisualPart} anchorages.
	 * @return {@link IViewer}.
	 */
	private IViewer getViewer(Set<? extends IVisualPart<? extends Node>> anchorages) {
		for (IVisualPart<? extends Node> anchorage : anchorages) {
			if (anchorage.getRoot() != null && anchorage.getRoot().getViewer() != null) {
				return anchorage.getRoot().getViewer();
			}
		}
		return null;
	}

	@Override
	protected void register(IViewer viewer) {
		if (registered) {
			return;
		}
		super.register(viewer);
		registered = true;
	}

	@Override
	public void setParent(IVisualPart<? extends Node> newParent) {
		// Re-attach to new parent
		if (this.getParent() != null) {
			this.getParent().getAnchoragesUnmodifiable().removeListener(this.parentAnchoragesChangeListener);
		}
		if (newParent != null) {
			newParent.getAnchoragesUnmodifiable().addListener(this.parentAnchoragesChangeListener);
		}
		super.setParent(newParent);
	}

	@Override
	protected Node doCreateVisual() {
		return this.adaptedAction.createVisual(this);
	}

	@Override
	public void doRefreshVisual(Node visual) {
		// managed by parent
	}

	@Override
	protected void unregister(IViewer viewer) {
		if (!registered) {
			return;
		}
		super.unregister(viewer);
		registered = false;
	}

	/*
	 * ==================== AdaptedActionVisualFactoryContext ====================
	 */

	@Override
	public <N extends Node> N addNode(Pane parent, N node) {
		parent.getChildren().add(node);
		return node;
	}

	@Override
	public Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath) {
		Node node = DefaultImages.createImageWithHover(resourceClass, imageFilePath, hoverImageFilePath);
		node.getStyleClass().add("action");
		return node;
	}

}
