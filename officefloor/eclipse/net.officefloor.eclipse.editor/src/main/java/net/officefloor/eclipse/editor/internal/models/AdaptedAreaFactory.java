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

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.gef.geometry.planar.Dimension;

import javafx.beans.property.Property;
import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.eclipse.editor.AdaptedAreaBuilder;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.model.Model;

/**
 * Factory for an {@link AdaptedArea}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAreaFactory<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AbstractAdaptedFactory<R, O, M, E, AdaptedArea<M>> implements AdaptedAreaBuilder<R, O, M, E> {

	/**
	 * Obtains the {@link Dimension} from the {@link Model}.
	 */
	private final Function<M, Dimension> getDimension;

	/**
	 * Specifies the {@link Dimension} on the {@link Model}.
	 */
	private final BiConsumer<M, Dimension> setDimension;

	/**
	 * {@link AdaptedModelVisualFactory}.
	 */
	private final AdaptedModelVisualFactory<M> viewFactory;

	/**
	 * Instantiate.
	 * 
	 * @param adaptedPathPrefix  Prefix on the configuration path.
	 * @param modelPrototype     {@link Model} prototype.
	 * @param parentAdaptedModel Parent {@link AbstractAdaptedFactory}.
	 * @param getDimension       Obtains the {@link Dimension} from the
	 *                           {@link Model}.
	 * @param setDimension       Specifies the {@link Dimension} on the
	 *                           {@link Model}.
	 * @param viewFactory        {@link AdaptedModelVisualFactory}.
	 */
	@SuppressWarnings("unchecked")
	public AdaptedAreaFactory(String adaptedPathPrefix, M modelPrototype,
			AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedModel, Function<M, Dimension> getDimension,
			BiConsumer<M, Dimension> setDimension, AdaptedModelVisualFactory<M> viewFactory) {
		super(adaptedPathPrefix, (Class<M>) modelPrototype.getClass(), () -> new AdaptedAreaImpl<>(),
				parentAdaptedModel);
		this.getDimension = getDimension;
		this.setDimension = setDimension;
		this.viewFactory = viewFactory;
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

		@Override
		public Dimension getDimension() {
			return this.getFactory().getDimension.apply(this.getModel());
		}

		@Override
		public void setDimension(Dimension dimension) {
			this.getFactory().setDimension.accept(this.getModel(), dimension);
		}

		@Override
		public Node createVisual(AdaptedModelVisualFactoryContext<M> context) {
			return this.getFactory().viewFactory.createVisual(this.getModel(), context);
		}
	}

}