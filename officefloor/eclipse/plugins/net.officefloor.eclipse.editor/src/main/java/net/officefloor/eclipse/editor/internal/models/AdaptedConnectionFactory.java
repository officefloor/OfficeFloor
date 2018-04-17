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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.gef.geometry.planar.Point;

import com.google.inject.Injector;

import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedConnectionBuilder;
import net.officefloor.eclipse.editor.AdaptedErrorHandler;
import net.officefloor.eclipse.editor.ModelActionContext;
import net.officefloor.eclipse.editor.OverlayVisualFactory;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Factory for an {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnectionFactory<R extends Model, O, S extends Model, C extends ConnectionModel, E extends Enum<E>>
		extends AbstractAdaptedFactory<R, O, C, E, AdaptedConnection<C>>
		implements AdaptedConnectionBuilder<R, O, S, C, E> {

	/**
	 * {@link Class} of the source {@link Model}.
	 */
	private final Class<S> sourceModelClass;

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
	private ModelToConnection<R, O, ?, ?, ?> targetConnector;

	/**
	 * {@link ConnectionFactory}.
	 */
	private ConnectionFactory<R, O, ? extends Model, ? extends ConnectionModel, ? extends Model> createConnection;

	/**
	 * {@link ConnectionRemover}.
	 */
	private ConnectionRemover<R, O, C> removeConnection;

	/**
	 * {@link AdaptedErrorHandler}.
	 */
	private final AdaptedErrorHandler errorHandler;

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
	public AdaptedConnectionFactory(Class<C> connectionClass, Class<S> sourceModelClass, Function<C, S> getSource,
			AdaptedChildFactory<R, O, ?, ?, ?> adaptedChildModelFactory) {
		super(connectionClass, () -> new AdaptedConnectionImpl<>(), adaptedChildModelFactory);
		this.sourceModelClass = sourceModelClass;
		this.getSource = getSource;
		this.errorHandler = adaptedChildModelFactory.getContentPartFactory().getErrorHandler();
	}

	/**
	 * Obtains the source {@link Model} {@link Class}.
	 * 
	 * @return Source {@link Model} {@link Class}.
	 */
	public Class<?> getSourceModelClass() {
		return this.sourceModelClass;
	}

	/**
	 * Obtains the target {@link Model} {@link Class}.
	 * 
	 * @return Target {@link Model} {@link Class}.
	 */
	public Class<?> getTargetModelClass() {
		return this.targetModelClass;
	}

	/**
	 * Creates a {@link ConnectionModel} between the source {@link Model} and target
	 * {@link Model}.
	 * 
	 * @param source
	 *            Source {@link Model}.
	 * @param target
	 *            Target {@link Model}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void createConnection(Model source, Model target) {

		// Determine if need to reverse source/target
		if (this.sourceModelClass.equals(target.getClass())) {
			Model swap = source;
			source = target;
			target = swap;
		}
		if ((!(this.sourceModelClass.equals(source.getClass())))
				|| (!(this.targetModelClass.equals(target.getClass())))) {
			throw new IllegalStateException("Models " + source.getClass().getName() + " and "
					+ target.getClass().getName() + " does not match connection " + this.sourceModelClass.getName()
					+ " source and " + this.targetModelClass.getName() + " target");
		}

		// Create the connection
		this.getContentPartFactory().addConnection(source, target, (ConnectionFactory) this.createConnection);
	}

	/*
	 * ===================== AbstractAdaptedFactory =====================
	 */

	@Override
	public void init(Injector injector, Map<Class<?>, AbstractAdaptedFactory<R, O, ?, ?, ?>> models) {
		super.init(injector, models);

		// Load the target to connection information to target model
		AbstractAdaptedFactory<R, O, ?, ?, ?> factory = models.get(this.targetModelClass);
		if (factory == null) {
			throw new IllegalStateException("Target model " + this.targetModelClass.getName() + " of connection "
					+ this.getModelClass().getName() + " is not configured");
		}
		if (!(factory instanceof AdaptedChildFactory)) {
			throw new IllegalStateException("Target model " + this.targetModelClass.getName() + " of connection "
					+ this.getModelClass().getName() + " must be an " + AdaptedChildFactory.class.getName());
		}
		AdaptedChildFactory<R, O, ?, ?, ?> adaptedChildFactory = (AdaptedChildFactory<R, O, ?, ?, ?>) factory;
		adaptedChildFactory.loadModelToConnection(this.getModelClass(), this.targetConnector);
	}

	/*
	 * ===================== AdaptedConnectionBuilder ===================
	 */

	@Override
	@SafeVarargs
	public final <T extends Model, TE extends Enum<TE>> void toOne(Class<T> targetModel, Function<T, C> getConnection,
			Function<C, T> getTarget, ConnectionFactory<R, O, S, C, T> createConnection,
			ConnectionRemover<R, O, C> removeConnection, TE... targetChangeEvents) {
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
			Function<T, List<C>> getConnections, Function<C, T> getTarget,
			ConnectionFactory<R, O, S, C, T> createConnection, ConnectionRemover<R, O, C> removeConnection,
			TE... targetChangeEvents) {
		this.targetModelClass = targetModel;
		this.getTarget = getTarget;
		this.targetConnector = new ModelToConnection<R, O, T, TE, C>(getConnections, targetChangeEvents, this);
		this.createConnection = createConnection;
		this.removeConnection = removeConnection;
	}

	/**
	 * {@link AdaptedConnection} implementation.
	 */
	public static class AdaptedConnectionImpl<R extends Model, O, S extends Model, C extends ConnectionModel, E extends Enum<E>>
			extends AbstractAdaptedModel<R, O, C, E, AdaptedConnection<C>, AdaptedConnectionFactory<R, O, S, C, E>>
			implements AdaptedConnection<C>, ModelActionContext<R, O, C, AdaptedConnection<C>> {

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

		@Override
		public void remove() {
			this.getFactory().errorHandler.isError(() -> this.getFactory().removeConnection.removeConnection(this));
		}

		/*
		 * ===================== ModelActionContext =======================
		 */

		@Override
		public AdaptedConnection<C> getAdaptedModel() {
			return this;
		}

		@Override
		public void execute(Change<?> change) {
			this.getChangeExecutor().execute(change);
		}

		@Override
		public void overlay(OverlayVisualFactory overlayVisualFactory) {

			// Obtain the location of this target
			Model model = this.getTarget().getModel();
			Point location = new Point(model.getX(), model.getY());

			// Add the overlay
			this.getFactory().getContentPartFactory().overlay(location, overlayVisualFactory);
		}
	}

}