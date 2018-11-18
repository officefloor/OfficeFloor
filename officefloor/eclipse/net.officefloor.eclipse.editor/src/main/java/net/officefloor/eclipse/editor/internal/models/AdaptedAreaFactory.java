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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.gef.geometry.planar.Dimension;

import javafx.beans.property.Property;
import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.eclipse.editor.AdaptedAreaBuilder;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.ParentToAreaConnectionModel;
import net.officefloor.model.Model;

/**
 * Factory for an {@link AdaptedArea}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAreaFactory<R extends Model, O, M extends Model, E extends Enum<E>> extends
		AbstractAdaptedConnectableFactory<R, O, M, E, AdaptedArea<M>> implements AdaptedAreaBuilder<R, O, M, E> {

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
	 * @param adaptedPathPrefix    Prefix on the configuration path.
	 * @param modelPrototype       {@link Model} prototype.
	 * @param parentAdaptedFactory Parent {@link AbstractAdaptedFactory}.
	 * @param getDimension         Obtains the {@link Dimension} from the
	 *                             {@link Model}.
	 * @param setDimension         Specifies the {@link Dimension} on the
	 *                             {@link Model}.
	 * @param viewFactory          {@link AdaptedModelVisualFactory}.
	 */
	public AdaptedAreaFactory(String adaptedPathPrefix, M modelPrototype,
			AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedFactory, Function<M, Dimension> getDimension,
			BiConsumer<M, Dimension> setDimension, AdaptedModelVisualFactory<M> viewFactory) {
		super(adaptedPathPrefix, modelPrototype, () -> new AdaptedAreaImpl<>(), parentAdaptedFactory);
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
	public static class AdaptedAreaImpl<R extends Model, O, M extends Model, E extends Enum<E>>
			extends AbstractAdaptedConnectable<R, O, M, E, AdaptedArea<M>, AdaptedAreaFactory<R, O, M, E>>
			implements AdaptedArea<M> {

		/**
		 * {@link ParentToAreaConnectionModel}.
		 */
		private ParentToAreaConnectionModel connectionModel = null;

		@Override
		protected void init() {
			super.init();

			// No connection if no parent
			AdaptedModel<?> parent = this.getParent();
			if (parent != null) {

				// Obtain the models and create connection
				Model parentModel = parent.getModel();
				Model areaModel = this.getModel();
				this.connectionModel = new ParentToAreaConnectionModel(parentModel, areaModel);
			}
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
		public ParentToAreaConnectionModel getParentConnection() {
			return this.connectionModel;
		}

		@Override
		public Node createVisual(AdaptedModelVisualFactoryContext<M> context) {
			return this.getFactory().viewFactory.createVisual(this.getModel(), context);
		}

		@Override
		protected void loadDescendantConnections(List<AdaptedConnection<?>> connections) {
			// no descendant connections
		}
	}

}