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
package net.officefloor.eclipse.editor.internal.models;

import javafx.beans.property.Property;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.eclipse.editor.AdaptedAreaBuilder;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;

/**
 * Factory for an {@link AdaptedArea}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAreaFactory<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AbstractAdaptedFactory<R, O, M, E, AdaptedArea<M>> implements AdaptedAreaBuilder<R, O, M, E> {

	/**
	 * Instantiate.
	 * 
	 * @param adaptedPathPrefix Prefix on the configuration path.
	 * @param modelPrototype    {@link Model} prototype.
	 * @param contentFactory    {@link OfficeFloorContentPartFactory}.
	 */
	public AdaptedAreaFactory(String adaptedPathPrefix, Class<M> modelClass,
			OfficeFloorContentPartFactory<R, O> contentFactory) {
		super(adaptedPathPrefix, modelClass, () -> new AdaptedAreaImpl<>(), contentFactory);
	}

	/*
	 * ====================== AdaptedAreaBuilder ======================
	 */

	@Override
	public Property<String> style() {
		// TODO implement AdaptedAreaBuilder<R,O,M,E>.style(...)
		throw new UnsupportedOperationException("TODO implement AdaptedAreaBuilder<R,O,M,E>.style(...)");
	}

	@Override
	public void create(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {
		// TODO implement AdaptedAreaBuilder<R,O,M,E>.create(...)
		throw new UnsupportedOperationException("TODO implement AdaptedAreaBuilder<R,O,M,E>.create(...)");
	}

	@Override
	public void action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {
		// TODO implement AdaptedAreaBuilder<R,O,M,E>.action(...)
		throw new UnsupportedOperationException("TODO implement AdaptedAreaBuilder<R,O,M,E>.action(...)");
	}

	/**
	 * {@link AdaptedArea} implementation.
	 */
	public static class AdaptedAreaImpl<R extends Model, O, M extends Model, E extends Enum<E>> extends
			AbstractAdaptedModel<R, O, M, E, AdaptedArea<M>, AdaptedAreaFactory<R, O, M, E>> implements AdaptedArea<M> {

		@Override
		protected void init() {
			// nothing to initialise
		}

	}

}