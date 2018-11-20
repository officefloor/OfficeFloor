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

import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.eclipse.editor.AdaptedAreaBuilder;
import net.officefloor.eclipse.editor.AdaptedConnectable;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedConnector;
import net.officefloor.eclipse.editor.AdaptedConnectorRole;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.ParentToAreaConnectionModel;
import net.officefloor.model.ConnectionModel;
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
	 * Minimum {@link Dimension}.
	 */
	private Dimension minimumDimension = new Dimension(50, 50);

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
	 */
	public AdaptedAreaFactory(String adaptedPathPrefix, M modelPrototype,
			AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedFactory, Function<M, Dimension> getDimension,
			BiConsumer<M, Dimension> setDimension) {
		super(adaptedPathPrefix, modelPrototype, () -> new AdaptedAreaImpl<>(), parentAdaptedFactory);
		this.getDimension = getDimension;
		this.setDimension = setDimension;
	}

	/*
	 * ====================== AdaptedAreaBuilder ======================
	 */

	@Override
	public void setMinimumDimension(double width, double height) {
		this.minimumDimension = new Dimension(width, height);
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
			implements AdaptedArea<M>, AdaptedConnector<M> {

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
		public AdaptedConnector<M> getAdaptedConnector(Class<? extends ConnectionModel> connectionClass,
				AdaptedConnectorRole type) {

			// Should only connect to parent
			if (!ParentToAreaConnectionModel.class.equals(connectionClass)) {
				throw new IllegalStateException(this.getClass().getSimpleName() + " should only be connected via "
						+ ParentToAreaConnectionModel.class.getName() + " (but was " + connectionClass.getName() + ")");
			}

			// Return as the connector
			return this;
		}

		@Override
		public Dimension getMinimumDimension() {
			return this.getFactory().minimumDimension;
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
		protected AdaptedConnector<M> createAdaptedConnector(Class<? extends ConnectionModel> connectionClass,
				AdaptedConnectorRole role, ModelToConnection<R, O, M, E, ?> connector) {
			return this;
		}

		@Override
		public ParentToAreaConnectionModel getParentConnection() {
			return this.connectionModel;
		}

		@Override
		protected void loadDescendantConnections(List<AdaptedConnection<?>> connections) {
			// no descendant connections
		}

		@Override
		public AdaptedConnectable<M> getParentAdaptedConnectable() {
			return this;
		}

		@Override
		public Class<? extends ConnectionModel> getConnectionModelClass() {
			return ParentToAreaConnectionModel.class;
		}

		@Override
		public void setAssociation(List<AdaptedConnector<M>> associatedAdaptedConnectors,
				AdaptedConnectorRole associatedRole) {
			// No association
		}

		@Override
		public boolean isAssociationCreateConnection() {
			return false;
		}

		@Override
		public AdaptedConnectorRole getAssociationRole() {
			return AdaptedConnectorRole.SOURCE;
		}
	}

}