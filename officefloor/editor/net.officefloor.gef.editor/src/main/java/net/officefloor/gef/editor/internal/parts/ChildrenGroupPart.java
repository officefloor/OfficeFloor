/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
