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
package net.officefloor.eclipse.editor.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.inject.Injector;

import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedConnectionBuilder;
import net.officefloor.eclipse.editor.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Factory for an {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnectionFactory<S extends Model, C extends ConnectionModel, E extends Enum<E>>
		extends AbstractAdaptedFactory<C, E, AdaptedConnection<C>> implements AdaptedConnectionBuilder<S, C, E> {

	/**
	 * {@link Function} to obtain the source {@link Model}.
	 */
	private final Function<C, S> getSource;

	/**
	 * {@link Function} to obtain the target {@link Model}.
	 */
	private Function<C, ? extends Model> getTarget;

	/**
	 * {@link Class} of the target {@link Model}.
	 */
	private Class<?> targetModelClass;

	/**
	 * {@link ModelToConnection} for target {@link Model}.
	 */
	private ModelToConnection<?, ?, ?> targetConnector;

	/**
	 * Instantiate.
	 * 
	 * @param connectionClass
	 *            {@link ConnectionModel} {@link Class}.
	 * @param getSource
	 *            {@link Function} to obtain the source {@link Model}.
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	public AdaptedConnectionFactory(Class<C> connectionClass, Function<C, S> getSource,
			AdaptedChildFactory<?, ?, ?> adaptedChildModelFactory) {
		super(connectionClass, () -> new AdaptedConnectionImpl<>(), adaptedChildModelFactory);
		this.getSource = getSource;
	}

	@Override
	public void init(Injector injector, Map<Class<?>, AbstractAdaptedFactory<?, ?, ?>> models) {
		super.validate(models);

		// Load the target to connection information to target model
		AbstractAdaptedFactory<?, ?, ?> factory = models.get(this.targetModelClass);
		if (factory == null) {
			throw new IllegalStateException("Target model " + this.targetModelClass.getName() + " of connection "
					+ this.getModelClass().getName() + " is not configured");
		}
		if (!(factory instanceof AdaptedChildFactory)) {
			throw new IllegalStateException("Target model " + this.targetModelClass.getName() + " of connection "
					+ this.getModelClass().getName() + " must be an " + AdaptedChildFactory.class.getName());
		}
		AdaptedChildFactory<?, ?, ?> adaptedChildFactory = (AdaptedChildFactory<?, ?, ?>) factory;
		adaptedChildFactory.loadModelToConnection(this.getModelClass(), this.targetConnector);
	}

	/*
	 * ===================== AdaptedConnectionBuilder ===================
	 */

	@Override
	@SafeVarargs
	public final <T extends Model, TE extends Enum<TE>> void toOne(Class<T> targetModel, Function<T, C> getConnection,
			Function<C, T> getTarget, ConnectionFactory<S, C, T> createConnection,
			Function<C, Change<C>> removeConnection, TE... targetChangeEvents) {
		this.toMany(targetModel, (model) -> {
			C connection = getConnection.apply(model);
			if (connection == null) {
				return Collections.emptyList();
			} else {
				return Arrays.asList(connection);
			}
		}, getTarget, createConnection, removeConnection, targetChangeEvents);
	}

	@Override
	@SafeVarargs
	public final <T extends Model, TE extends Enum<TE>> void toMany(Class<T> targetModel,
			Function<T, List<C>> getConnections, Function<C, T> getTarget, ConnectionFactory<S, C, T> createConnection,
			Function<C, Change<C>> removeConnection, TE... targetChangeEvents) {
		this.targetModelClass = targetModel;
		this.getTarget = getTarget;
		this.targetConnector = new ModelToConnection<>(getConnections, targetChangeEvents);
	}

	/**
	 * {@link AdaptedConnection} implementation.
	 */
	public static class AdaptedConnectionImpl<S extends Model, C extends ConnectionModel, E extends Enum<E>>
			extends AbstractAdaptedModel<C, E, AdaptedConnection<C>, AdaptedConnectionFactory<S, C, E>>
			implements AdaptedConnection<C> {

		@Override
		protected void init() {
		}

		@Override
		public AdaptedChild<?> getSource() {
			Model source = this.getFactory().getSource.apply(this.getModel());
			return (AdaptedChild<?>) this.getFactory().getAdaptedModel(source);
		}

		@Override
		public AdaptedChild<?> getTarget() {
			Model target = this.getFactory().getTarget.apply(this.getModel());
			return (AdaptedChild<?>) this.getFactory().getAdaptedModel(target);
		}
	}

}