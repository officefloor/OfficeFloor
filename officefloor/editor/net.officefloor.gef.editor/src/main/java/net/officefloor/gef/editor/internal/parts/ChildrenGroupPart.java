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

import java.util.List;

import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChild;
import net.officefloor.gef.editor.ChildrenGroup;
import net.officefloor.gef.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the {@link ChildrenGroupImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChildrenGroupPart<R extends Model, O> extends AbstractContentPart<Pane> {

	/**
	 * {@link ListChangeListener} to refresh the children.
	 */
	private final ListChangeListener<AdaptedChild<?>> changeListener = (change) -> this.refreshContentChildren();

	@Override
	public ChildrenGroup<?, ?> getContent() {
		return (ChildrenGroup<?, ?>) super.getContent();
	}

	@Override
	public void setContent(Object content) {

		// Stop listen on possible existing content
		if (this.getContent() != null) {
			this.getContent().getChildren().removeListener(this.changeListener);
		}

		// Load the new content
		if ((content != null) && (!(content instanceof ChildrenGroup))) {
			throw new IllegalArgumentException("Only " + ChildrenGroup.class.getSimpleName() + " supported.");
		}
		super.setContent(content);

		// Listen on changes
		if (content != null) {
			this.getContent().getChildren().addListener(this.changeListener);
		}
	}

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return this.getContent().getChildren();
	}

	@Override
	protected Pane doCreateVisual() {

		// Obtain the parent
		AdaptedChildPart<?, ?> parent = (AdaptedChildPart<?, ?>) this.getParent();

		// Obtain the pane for this children group
		Pane pane = parent.getChildrenGroupPane(this.getContent());

		// Add the children group name for CSS
		pane.getStyleClass().add("children");
		pane.getStyleClass().add(this.getContent().getChildrenGroupName());

		// Return the visual
		return pane;
	}

	@Override
	protected void doAddChildVisual(IVisualPart<? extends Node> child, int index) {
		this.getVisual().getChildren().add(index, child.getVisual());
	}

	@Override
	protected void doRemoveChildVisual(IVisualPart<? extends Node> child, int index) {
		this.getVisual().getChildren().remove(index);
	}

	@Override
	protected void doRefreshVisual(Pane visual) {
		// Nothing to refresh
	}

}
