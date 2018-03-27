/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.internal.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Injector;

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedBuilderContext;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedConnectionBuilder.ConnectionFactory;
import net.officefloor.eclipse.editor.internal.models.AbstractAdaptedFactory;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnector;
import net.officefloor.eclipse.editor.internal.models.AdaptedParentFactory;
import net.officefloor.eclipse.editor.internal.models.ChangeExecutor;
import net.officefloor.eclipse.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ModelActionContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

public class OfficeFloorContentPartFactory<R extends Model, O>
		implements IContentPartFactory, AdaptedRootBuilder<R, O>, AdaptedBuilderContext {

	/**
	 * Indicates if contains an {@link ConnectionModel}.
	 * 
	 * @param targets
	 *            Target {@link IVisualPart} instances.
	 * @return <code>true</code> if contains {@link ConnectionModel}.
	 */
	public static boolean isContainsConnection(List<? extends IVisualPart<? extends Node>> targets) {
		for (IVisualPart<? extends Node> target : targets) {
			if (target instanceof AdaptedConnectionPart) {
				return true;
			}
		}

		// As here, no connection
		return false;
	}

	@Inject
	private Injector injector;

	/**
	 * Root {@link Model} {@link Class}.
	 */
	private Class<R> rootModelClass;

	/**
	 * {@link Function} to create the operations.
	 */
	private Function<R, O> createOperations;

	/**
	 * {@link List} of the {@link Function} instances to obtain the parent
	 * {@link Model}.
	 */
	private final List<Function<R, List<? extends Model>>> getParentFunctions = new LinkedList<>();

	/**
	 * {@link AbstractAdaptedFactory} instances for the {@link Model} types.
	 */
	private final Map<Class<?>, AbstractAdaptedFactory<R, O, ?, ?, ?>> models = new HashMap<>();

	/**
	 * Mapping of {@link Model} to its {@link AdaptedModel}.
	 */
	private final Map<Model, AdaptedModel<?>> modelToAdaption = new HashMap<>();

	/**
	 * Root {@link Model}.
	 */
	private R rootModel;

	/**
	 * Operations.
	 */
	private O operations;

	/**
	 * {@link IViewer} for the content.
	 */
	private IViewer contentViewer;

	/**
	 * Registers the {@link AbstractAdaptedFactory}.
	 * 
	 * @param builder
	 *            {@link AbstractAdaptedFactory}.
	 */
	public <M extends Model, E extends Enum<E>> void registerModel(AbstractAdaptedFactory<R, O, M, E, ?> builder) {
		this.models.put(builder.getModelClass(), builder);
	}

	/**
	 * Loads the root {@link Model}.
	 * 
	 * @param rootModel
	 *            Root {@link Model}.
	 * @param content
	 *            {@link IViewer} for the content.
	 * @param palette
	 *            {@link IViewer} for the palette.
	 */
	@SuppressWarnings("unchecked")
	public void loadRootModel(Model rootModel, IViewer content, IViewer palette) {

		// Ensure correct root model
		if (!this.rootModelClass.equals(rootModel.getClass())) {
			throw new IllegalStateException("Incorrect root model type " + rootModel.getClass().getName()
					+ " as configured with " + this.rootModelClass.getName());
		}
		this.rootModel = (R) rootModel;
		this.operations = this.createOperations.apply(this.rootModel);
		this.contentViewer = content;

		// Initialise all the models
		this.models.values().forEach((model) -> model.init(this.injector, this.models));

		// Validate all the models
		this.models.values().forEach((model) -> model.validate());

		// Load with dependencies injected
		OfficeFloorContentPartFactory<?, ?> factory = injector.getInstance(OfficeFloorContentPartFactory.class);

		// Load the palette models
		List<AdaptedModel<?>> paletteModels = new LinkedList<>();
		for (AbstractAdaptedFactory<R, O, ?, ?, ?> adaptedFactory : this.models.values()) {
			if (adaptedFactory instanceof AdaptedParentFactory) {
				AdaptedParentFactory<R, O, ?, ?> parentFactory = (AdaptedParentFactory<R, O, ?, ?>) adaptedFactory;

				// Include if providing prototype
				Model prototype = parentFactory.getPalettePrototype();
				if (prototype != null) {
					AdaptedModel<?> adaptedPrototype = factory.createAdaptedModel(prototype);
					paletteModels.add(adaptedPrototype);
				}
			}
		}
		palette.getContents().setAll(paletteModels);

		// Initial load of content models
		this.loadContentModels();

		// Create property change listener to reload on change
		this.rootModel.addPropertyChangeListener((event) -> {
			this.loadContentModels();
		});
	}

	/**
	 * Loads the content {@link Model} instances into the content {@link IViewer}.
	 */
	public void loadContentModels() {

		// Load the content models
		List<Model> contentModels = new LinkedList<>();
		for (Function<R, List<? extends Model>> getParents : this.getParentFunctions) {
			List<? extends Model> parents = getParents.apply(this.rootModel);
			contentModels.addAll(parents);
		}

		// Adapt the content models
		List<AdaptedModel<?>> adaptedContentModels = new ArrayList<AdaptedModel<?>>();
		List<AdaptedParent<?>> adaptedParents = new ArrayList<>();
		for (Model model : contentModels) {
			AdaptedParent<?> adaptedModel = (AdaptedParent<?>) this.createAdaptedModel(model);

			// Add the adapted model (only once)
			if (!adaptedContentModels.contains(adaptedModel)) {
				adaptedContentModels.add(adaptedModel);
				adaptedParents.add(adaptedModel);
			}
		}

		// Load the adapted connections (aferwards so z-order in front)
		for (AdaptedParent<?> adaptedParent : adaptedParents) {
			for (AdaptedConnection<?> connection : adaptedParent.getConnections()) {

				// Add the adapted conenction (only once)
				if (!adaptedContentModels.contains(connection)) {
					adaptedContentModels.add(connection);
				}
			}
		}

		// Load the adapted models
		this.contentViewer.getContents().setAll(adaptedContentModels);
	}

	/**
	 * Creates the wrapper for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @return {@link AbstractAdaptedFactory} for the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	public <M extends Model> AdaptedModel<M> createAdaptedModel(M model) {

		// Determine if already and adapted model
		AdaptedModel<M> adapted = (AdaptedModel<M>) this.modelToAdaption.get(model);
		if (adapted != null) {
			return adapted; // already adapted
		}

		// Look up the builder
		AbstractAdaptedFactory<R, O, M, ?, ?> builder = (AbstractAdaptedFactory<R, O, M, ?, ?>) this.models
				.get(model.getClass());
		if (builder != null) {

			// Create and register the adapted model
			adapted = builder.newAdaptedModel(this.rootModel, this.operations, model);
			this.modelToAdaption.put(model, adapted);
			return adapted;
		}

		// As here, model is not configured
		throw new IllegalStateException("Non-adapted model " + model.getClass().getName());
	}

	/**
	 * Adds the {@link ConnectionModel} to the {@link Model} structure.
	 * 
	 * @param source
	 *            Source {@link Model}.
	 * @param target
	 *            Target {@link Model}.
	 * @param createConnection
	 *            {@link ConnectionFactory}.
	 */
	public <S extends Model, T extends Model, C extends ConnectionModel> void addConnection(S source, T target,
			ConnectionFactory<R, O, S, C, T> createConnection) {
		createConnection.addConnection(source, target, new ModelActionContext<R, O, C, AdaptedConnection<C>>() {

			@Override
			public R getRootModel() {
				return OfficeFloorContentPartFactory.this.rootModel;
			}

			@Override
			public O getOperations() {
				return OfficeFloorContentPartFactory.this.operations;
			}

			@Override
			public C getModel() {
				return null;
			}

			@Override
			public AdaptedConnection<C> getAdaptedModel() {
				return null;
			}

			@Override
			public void execute(Change<?> change) {
				this.getInjector().getInstance(ChangeExecutor.class).execute(change);
			}

			@Override
			public Injector getInjector() {
				return OfficeFloorContentPartFactory.this.injector;
			}
		});
	}

	/*
	 * ====================== AdaptedBuilderContext =======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <r extends Model, o> AdaptedRootBuilder<r, o> root(Class<r> rootModelClass,
			Function<r, o> createOperations) {
		this.rootModelClass = (Class<R>) rootModelClass;
		this.createOperations = (Function<R, O>) createOperations;
		return (AdaptedRootBuilder<r, o>) this;
	};

	/*
	 * ====================== AdaptedBuilderContext =======================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <M extends Model, E extends Enum<E>, RE extends Enum<RE>> AdaptedParentBuilder<R, O, M, E> parent(
			Class<M> modelClass, Function<R, List<M>> getParents,
			AdaptedModelVisualFactory<M, AdaptedParent<M>> viewFactory, RE... changeParentEvents) {
		this.getParentFunctions.add((Function) getParents);
		return new AdaptedParentFactory<R, O, M, E>(modelClass, viewFactory, this);
	}

	/*
	 * ==================== IContentPartFactory =====================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public IContentPart<? extends Node> createContentPart(Object content, Map<Object, Object> contextMap) {

		// Provide part for adapted
		if (content instanceof AdaptedParent) {
			return this.injector.getInstance(AdaptedParentPart.class);
		} else if (content instanceof ChildrenGroupImpl) {
			return this.injector.getInstance(ChildrenGroupPart.class);
		} else if (content instanceof AdaptedChild) {
			return this.injector.getInstance(AdaptedChildPart.class);
		} else if (content instanceof AdaptedConnection) {
			return this.injector.getInstance(AdaptedConnectionPart.class);
		} else if (content instanceof AdaptedConnector) {
			return this.injector.getInstance(AdaptedConnectorPart.class);
		}

		// Unknown model
		throw new IllegalArgumentException("Unhandled model " + content.getClass().getName());
	}

}