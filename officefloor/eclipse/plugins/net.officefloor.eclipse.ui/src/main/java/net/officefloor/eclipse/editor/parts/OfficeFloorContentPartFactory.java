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
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.ViewFactory;
import net.officefloor.eclipse.editor.models.AbstractAdaptedModelFactory;
import net.officefloor.eclipse.editor.models.AdaptedParentModelFactory;
import net.officefloor.eclipse.editor.models.ChangeExecutor;
import net.officefloor.eclipse.editor.models.GeometricCurve;
import net.officefloor.eclipse.editor.models.GeometricShape;
import net.officefloor.eclipse.editor.models.ChildrenGroupFactory.ChildrenGroup;
import net.officefloor.model.Model;

public class OfficeFloorContentPartFactory implements IContentPartFactory, AdaptedBuilderContext {

	@Inject
	private Injector injector;

	/**
	 * {@link AbstractAdaptedModelFactory} instances for the {@link Model} types.
	 */
	private final Map<Class<?>, AbstractAdaptedModelFactory<?, ?, ?>> models = new HashMap<>();

	/**
	 * Registers the {@link AbstractAdaptedModelFactory}.
	 * 
	 * @param builder
	 *            {@link AbstractAdaptedModelFactory}.
	 */
	public <M extends Model, E extends Enum<E>> void registerModel(AbstractAdaptedModelFactory<M, E, ?> builder) {
		this.models.put(builder.getModelClass(), builder);
	}

	/**
	 * Validates the {@link Model} configuration.
	 * 
	 * @throws IllegalStateException
	 *             If invalid.
	 */
	public void validateModels() throws IllegalStateException {
		this.models.values().forEach((model) -> model.validate(this.models));
	}

	/**
	 * Creates the wrapper for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @return {@link AbstractAdaptedModelFactory} for the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	public <M extends Model> AdaptedModel<M> createContent(M model) {

		// Obtain the change executer
		ChangeExecutor changeExecutor = this.injector.getInstance(ChangeExecutor.class);

		// Look up the builder
		AbstractAdaptedModelFactory<M, ?, ?> builder = (AbstractAdaptedModelFactory<M, ?, ?>) this.models
				.get(model.getClass());
		if (builder != null) {

			// Create the model adapter
			return builder.createAdaptedModel(model, changeExecutor);
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
		return new AdaptedParentModelFactory<>(modelClass, viewFactory, this);
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

		// Determine if Children Group
		if (content instanceof ChildrenGroup) {
			return this.injector.getInstance(ChildrenGroupPart.class);
		}

		// Look up the factory
		AbstractAdaptedModelFactory<?, ?, ?> factory = this.models.get(content.getClass());
		if (factory == null) {
			throw new IllegalArgumentException("Unhandled model " + content.getClass().getName());
		}

		// Return the content part
		return (IContentPart<? extends Node>) this.injector.getInstance(factory.getPartClass());
	};

}