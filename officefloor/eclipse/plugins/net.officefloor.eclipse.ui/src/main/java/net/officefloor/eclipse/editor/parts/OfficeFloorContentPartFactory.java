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
package net.officefloor.eclipse.editor.parts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;

import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Injector;

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedBuilderContext;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ViewFactory;
import net.officefloor.eclipse.editor.models.AbstractAdaptedFactory;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.eclipse.editor.models.AdaptedParentFactory;
import net.officefloor.eclipse.editor.models.ChildrenGroupFactory.ChildrenGroup;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

public class OfficeFloorContentPartFactory<R extends Model>
		implements IContentPartFactory, AdaptedRootBuilder<R>, AdaptedBuilderContext {

	@Inject
	private Injector injector;

	/**
	 * Root {@link Model} {@link Class}.
	 */
	private Class<R> rootModelClass;

	/**
	 * {@link List} of the {@link Function} instances to obtain the parent
	 * {@link Model}.
	 */
	private final List<Function<R, List<? extends Model>>> getParentFunctions = new LinkedList<>();

	/**
	 * {@link AbstractAdaptedFactory} instances for the {@link Model} types.
	 */
	private final Map<Class<?>, AbstractAdaptedFactory<?, ?, ?>> models = new HashMap<>();

	/**
	 * Mapping of {@link Model} to its {@link AdaptedModel}.
	 */
	private final Map<Model, AdaptedModel<?>> modelToAdaption = new HashMap<>();

	/**
	 * Root {@link Model}.
	 */
	private R rootModel;

	/**
	 * Registers the {@link AbstractAdaptedFactory}.
	 * 
	 * @param builder
	 *            {@link AbstractAdaptedFactory}.
	 */
	public <M extends Model, E extends Enum<E>> void registerModel(AbstractAdaptedFactory<M, E, ?> builder) {
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

		// Initialise all the models
		this.models.values().forEach((model) -> model.init(this.injector, this.models));

		// Validate all the models
		this.models.values().forEach((model) -> model.validate(this.models));

		// Load the models
		List<Model> models = new LinkedList<>();
		for (Function<R, List<? extends Model>> getParents : this.getParentFunctions) {
			List<? extends Model> parents = getParents.apply(this.rootModel);
			models.addAll(parents);
		}

		// Adapt the models
		Set<AdaptedModel<?>> adaptedModels = new HashSet<>();
		OfficeFloorContentPartFactory<?> factory = injector.getInstance(OfficeFloorContentPartFactory.class);
		for (Model model : models) {
			AdaptedParent<?> adaptedModel = (AdaptedParent<?>) factory.createAdaptedModel(model);

			// Add the adapted model
			adaptedModels.add(adaptedModel);

			// Load the connections
			List<ConnectionModel> connections = adaptedModel.getConnections();
			for (ConnectionModel connection : connections) {

				// Add the adapted connection
				AdaptedModel<?> adaptedConnection = factory.createAdaptedModel(connection);
				adaptedModels.add(adaptedConnection);
			}
		}

		// Load the adapted models
		content.getContents().setAll(adaptedModels);
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
		AbstractAdaptedFactory<M, ?, ?> builder = (AbstractAdaptedFactory<M, ?, ?>) this.models.get(model.getClass());
		if (builder != null) {

			// Create and register the adapted model
			adapted = builder.newAdaptedModel(model);
			this.modelToAdaption.put(model, adapted);
			return adapted;
		}

		// As here, model is not configured
		throw new IllegalStateException("Non-adapted model " + model.getClass().getName());
	}

	/*
	 * ====================== AdaptedBuilderContext =======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <r extends Model> AdaptedRootBuilder<r> setRootModel(Class<r> rootModelClass) {
		this.rootModelClass = (Class<R>) rootModelClass;
		return (AdaptedRootBuilder<r>) this;
	};

	/*
	 * ====================== AdaptedBuilderContext =======================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <M extends Model, E extends Enum<E>> AdaptedParentBuilder<M, E> addParent(Class<M> modelClass,
			Function<R, List<M>> getParents, ViewFactory<M, AdaptedParent<M>> viewFactory) {
		this.getParentFunctions.add((Function) getParents);
		return new AdaptedParentFactory<>(modelClass, viewFactory, this);
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
		} else if (content instanceof ChildrenGroup) {
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