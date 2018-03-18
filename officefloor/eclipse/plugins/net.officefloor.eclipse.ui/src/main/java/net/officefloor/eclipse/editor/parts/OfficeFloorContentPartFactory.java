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
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;

import com.google.inject.Injector;

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedBuilderContext;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.ViewFactory;
import net.officefloor.eclipse.editor.models.AbstractAdaptedFactory;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.eclipse.editor.models.AdaptedParentFactory;
import net.officefloor.eclipse.editor.models.ChildrenGroupFactory.ChildrenGroup;
import net.officefloor.eclipse.editor.models.GeometricCurve;
import net.officefloor.eclipse.editor.models.GeometricShape;
import net.officefloor.model.Model;

public class OfficeFloorContentPartFactory implements IContentPartFactory, AdaptedBuilderContext {

	@Inject
	private Injector injector;

	/**
	 * {@link AbstractAdaptedFactory} instances for the {@link Model} types.
	 */
	private final Map<Class<?>, AbstractAdaptedFactory<?, ?, ?>> models = new HashMap<>();

	/**
	 * Mapping of {@link Model} to its {@link AdaptedModel}.
	 */
	private final Map<Model, AdaptedModel<?>> modelToAdaption = new HashMap<>();

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
	 * Initialises from {@link Injector}.
	 */
	public void initialiseFromInjector() {

		// Initialise all the models
		this.models.values().forEach((model) -> model.init(this.injector, this.models));

		// Validate all the models
		this.models.values().forEach((model) -> model.validate(this.models));
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
	 * ======================= AdaptedModelBuilder ========================
	 */

	@Override
	public <M extends Model, E extends Enum<E>> AdaptedParentBuilder<M, E> addParent(Class<M> modelClass,
			ViewFactory<M, AdaptedParent<M>> viewFactory) {
		return new AdaptedParentFactory<>(modelClass, viewFactory, this);
	}

	/*
	 * ==================== IContentPartFactory =====================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public IContentPart<? extends Node> createContentPart(Object content, Map<Object, Object> contextMap) {

		// TODO REMOVE (for example set up of project)
		if (content instanceof GeometricShape) {
			return injector.getInstance(GeometricShapePart.class);
		} else if (content instanceof GeometricCurve) {
			return injector.getInstance(GeometricCurvePart.class);
		}

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
	};

}