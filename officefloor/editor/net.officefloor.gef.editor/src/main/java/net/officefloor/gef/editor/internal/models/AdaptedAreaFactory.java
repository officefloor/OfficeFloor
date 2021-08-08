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

package net.officefloor.gef.editor.internal.models;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.gef.geometry.planar.Dimension;

import net.officefloor.gef.editor.AdaptedActionVisualFactory;
import net.officefloor.gef.editor.AdaptedArea;
import net.officefloor.gef.editor.AdaptedAreaBuilder;
import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnection;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedConnectorRole;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.ModelAction;
import net.officefloor.gef.editor.ParentToAreaConnectionModel;
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
	 * {@link AdaptedActionsFactory}.
	 */
	private final AdaptedActionsFactory<R, O, M> actionsFactory;

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
		this.actionsFactory = new AdaptedActionsFactory<>(parentAdaptedFactory.getContentPartFactory());
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
		this.actionsFactory.addAction(action, visualFactory);
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

		/**
		 * {@link AdaptedActions}.
		 */
		private AdaptedActions<R, O, M> actions = null;

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

			// Load the actions
			this.actions = this.getFactory().actionsFactory.createAdaptedActions(this);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getAdapter(Class<T> classKey) {

			// Attempt to handle adapting
			if (AdaptedActions.class.equals(classKey)) {
				return (T) this.actions;
			}

			// Not able to adapt
			return null;
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
