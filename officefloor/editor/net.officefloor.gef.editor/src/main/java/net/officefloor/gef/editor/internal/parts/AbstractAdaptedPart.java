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

import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;

import javafx.scene.Node;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.internal.models.AbstractAdaptedFactory;
import net.officefloor.model.Model;

/**
 * Abstract {@link IContentPart} for the {@link AbstractAdaptedFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedPart<M extends Model, A extends AdaptedModel<M>, N extends Node>
		extends AbstractContentPart<N> {

	@Override
	@SuppressWarnings("unchecked")
	public A getContent() {
		return (A) super.getContent();
	}

	@Override
	public void setContent(Object content) {
		if (content != null && !(content instanceof AdaptedModel)) {
			throw new IllegalArgumentException("Only " + AdaptedModel.class.getSimpleName() + " supported.");
		}
		super.setContent(content);

		// Initialise
		if (content != null) {

			// Update visual based on change
			M model = this.getContent().getModel();
			if (model != null) {
				model.addPropertyChangeListener((event) -> this.refreshVisual());
			}

			// Initialise
			this.init();
		}
	}

	/**
	 * May override to initialise.
	 */
	protected void init() {
	}

}
