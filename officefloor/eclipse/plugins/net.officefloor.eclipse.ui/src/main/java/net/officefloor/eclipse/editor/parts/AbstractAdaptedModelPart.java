/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor.parts;

import javax.inject.Inject;

import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.models.AbstractAdaptedModelFactory;
import net.officefloor.model.Model;

/**
 * Abstract {@link IContentPart} for the {@link AbstractAdaptedModelFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractAdaptedModelPart<M extends Model, A extends AdaptedModel<M>, N extends Node>
		extends AbstractContentPart<N> {

	@Inject
	private OfficeFloorContentPartFactory contentFactory;

	/**
	 * {@link AbstractAdaptedModelFactory}.
	 */
	private A adapter;

	@Override
	@SuppressWarnings("unchecked")
	public M getContent() {
		return (M) super.getContent();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setContent(Object content) {
		if (content != null && !(content instanceof Model)) {
			throw new IllegalArgumentException("Only " + Model.class.getSimpleName() + " supported.");
		}

		// Wrap content with adapter
		M model = (M) content;
		this.adapter = (A) this.contentFactory.createContent(model);

		// Load content once have adapter
		super.setContent(content);

		// Initialise
		this.init();
	}

	/**
	 * May override to initialise from {@link Model}.
	 */
	protected void init() {
	}

	/**
	 * Obtains the {@link AbstractAdaptedModelFactory}.
	 * 
	 * @return {@link AbstractAdaptedModelFactory}.
	 */
	protected A getContentAdapter() {
		return this.adapter;
	}

}