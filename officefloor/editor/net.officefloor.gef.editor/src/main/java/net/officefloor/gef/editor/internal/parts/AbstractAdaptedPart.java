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
