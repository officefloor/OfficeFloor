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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Singleton;

import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.models.GridModel;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.eclipse.gef.mvc.fx.parts.IFeedbackPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import com.google.inject.Injector;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedBuilderContext;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedConnectionManagementBuilder.ConnectionFactory;
import net.officefloor.eclipse.editor.AdaptedEditorPlugin;
import net.officefloor.eclipse.editor.AdaptedErrorHandler;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactory;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.ModelActionContext;
import net.officefloor.eclipse.editor.OverlayVisualFactory;
import net.officefloor.eclipse.editor.SelectOnly;
import net.officefloor.eclipse.editor.internal.models.AbstractAdaptedFactory;
import net.officefloor.eclipse.editor.internal.models.AdaptedConnectorImpl;
import net.officefloor.eclipse.editor.internal.models.AdaptedParentFactory;
import net.officefloor.eclipse.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.eclipse.editor.internal.style.StyleRegistry;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

@Singleton
public class OfficeFloorContentPartFactory<R extends Model, O>
		implements IContentPartFactory, AdaptedRootBuilder<R, O>, AdaptedBuilderContext {

	/**
	 * Indicates if contains an {@link ConnectionModel}.
	 * 
	 * @param targets
	 *            Target {@link IVisualPart} instances.
	 * @param createFeedbackParts
	 *            {@link Function} to create the {@link IFeedbackPart} instances
	 *            from the filtered list of targets.
	 * @return Filtered targets.
	 */
	public static List<IFeedbackPart<? extends Node>> createFeedbackParts(
			List<? extends IVisualPart<? extends Node>> targets,
			Function<List<IVisualPart<? extends Node>>, List<IFeedbackPart<? extends Node>>> createFeedbackParts) {

		// Filter out connections that can not be removed
		List<IVisualPart<? extends Node>> filteredTargets = new ArrayList<>(targets.size());
		NEXT_TARGET: for (IVisualPart<? extends Node> target : targets) {

			// Determine if filter out the target from selection
			if (target instanceof AdaptedConnectionPart) {
				AdaptedConnectionPart<?, ?, ?> connectionPart = (AdaptedConnectionPart<?, ?, ?>) target;
				if (!connectionPart.getContent().canRemove()) {
					continue NEXT_TARGET;
				}

			} else {
				// Always filter out feedback on other items
				continue NEXT_TARGET;
			}

			// Include the target
			filteredTargets.add(target);
		}

		// Return the feedback parts
		return filteredTargets.size() == 0 ? Collections.emptyList() : createFeedbackParts.apply(filteredTargets);
	}

	/**
	 * {@link Injector}.
	 */
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
	 * {@link AbstractAdaptedFactory} instances maintaining the order they were
	 * registered.
	 */
	private final List<AbstractAdaptedFactory<R, O, ?, ?, ?>> orderedModels = new LinkedList<>();

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
	 * {@link InfiniteCanvasViewer} for the content.
	 */
	private InfiniteCanvasViewer contentViewer;

	/**
	 * Default content {@link Background}.
	 */
	private Background defaultContentBackground;

	/**
	 * Style rules for the content {@link IViewer} {@link Pane}.
	 */
	private Property<String> contentStyle;

	/**
	 * {@link IViewer} for the palette.
	 */
	private IViewer paletteViewer;

	/**
	 * Style rules for the palette indicator.
	 */
	private Property<String> paletteIndicatorStyle;

	/**
	 * Style rules for the palette {@link IViewer} {@link Pane}.
	 */
	private Property<String> paletteStyle;

	/**
	 * {@link ChangeExecutor}.
	 */
	private ChangeExecutor changeExecutor;

	/**
	 * {@link AdaptedErrorHandler}.
	 */
	private AdaptedErrorHandler errorHandler;

	/**
	 * {@link StyleRegistry}.s
	 */
	private StyleRegistry styleRegistry;

	/**
	 * {@link SelectOnly}. May be <code>null</code>.
	 */
	private SelectOnly selectOnly = null;

	/**
	 * Initialises.
	 * 
	 * @param injector
	 *            {@link Injector}.
	 * @param content
	 *            {@link IViewer} content.
	 * @param paletteIndicator
	 *            Palette indicator {@link Pane}.
	 * @param palette
	 *            {@link IViewer} palette.
	 * @param errorHandler
	 *            {@link AdaptedErrorHandler}.
	 * @param changeExecutor
	 *            {@link ChangeExecutor}.
	 */
	public void init(Injector injector, IViewer content, Pane paletteIndicator, IViewer palette,
			AdaptedErrorHandler errorHandler, ChangeExecutor changeExecutor, StyleRegistry styleRegistry,
			SelectOnly selectOnly) {
		this.injector = injector;
		this.contentViewer = (InfiniteCanvasViewer) content;
		this.paletteViewer = palette;
		this.errorHandler = errorHandler;
		this.changeExecutor = changeExecutor;
		this.styleRegistry = styleRegistry;
		this.selectOnly = selectOnly;

		// Register styling for palette indicator
		this.paletteIndicatorStyle = new SimpleStringProperty(null);
		ReadOnlyProperty<URL> paletteIndicatorUrl = this.styleRegistry.registerStyle("_palette_indicator_",
				this.paletteIndicatorStyle);
		paletteIndicatorUrl.addListener((event, oldValue, newValue) -> {
			if (oldValue != null) {
				paletteIndicator.getStylesheets().remove(oldValue.toExternalForm());
			}
			if (newValue != null) {
				paletteIndicator.getStylesheets().add(newValue.toExternalForm());
			}
		});
		if (this.selectOnly != null) {
			paletteIndicator.setOnMouseClicked((event) -> {
				this.selectOnly.paletteIndicator(this.paletteIndicatorStyle);
			});
		}

		// Register styling for palette
		this.paletteStyle = new SimpleStringProperty(null);
		ReadOnlyProperty<URL> paletteUrl = this.styleRegistry.registerStyle("_palette_", this.paletteStyle);
		paletteUrl.addListener((event, oldValue, newValue) -> {
			// Obtain the pane for the palette
			if (oldValue != null) {
				this.paletteViewer.getCanvas().getStylesheets().remove(oldValue.toExternalForm());
			}
			if (newValue != null) {
				this.paletteViewer.getCanvas().getStylesheets().add(newValue.toExternalForm());
			}
		});
		if (this.selectOnly != null) {
			this.paletteViewer.getCanvas().setOnMouseClicked((event) -> {
				if (event.getTarget() != this.paletteViewer.getCanvas()) {
					this.selectOnly.palette(this.paletteStyle);
				}
			});
		}

		// Register styling for content
		this.contentStyle = new SimpleStringProperty(null);
		ReadOnlyProperty<URL> contentUrl = this.styleRegistry.registerStyle("_content_", this.contentStyle);
		contentUrl.addListener((event, oldValue, newValue) -> {
			if (oldValue != null) {
				this.contentViewer.getCanvas().getStylesheets().remove(oldValue.toExternalForm());
			}
			if (newValue != null) {
				this.contentViewer.getCanvas().getStylesheets().add(newValue.toExternalForm());
			}
		});
		if (this.selectOnly != null) {
			this.contentViewer.getCanvas().setOnMouseClicked((event) -> {
				this.selectOnly.content(this);
			});
		}
	}

	/**
	 * Registers the {@link AbstractAdaptedFactory}.
	 * 
	 * @param builder
	 *            {@link AbstractAdaptedFactory}.
	 */
	public <M extends Model, E extends Enum<E>> void registerModel(AbstractAdaptedFactory<R, O, M, E, ?> builder) {
		this.models.put(builder.getModelClass(), builder);
		this.orderedModels.add(builder);
	}

	/**
	 * Obtains the {@link StyleRegistry}.
	 * 
	 * @return {@link StyleRegistry}.
	 */
	public StyleRegistry getStyleRegistry() {
		return this.styleRegistry;
	}

	/**
	 * Indicates if able to create an {@link AdaptedParent}.
	 * 
	 * @return <code>true</code> if able to create an {@link AdaptedParent}.
	 */
	@SuppressWarnings("unchecked")
	public boolean isCreateParent() {

		// Determine if can create a parent
		for (AbstractAdaptedFactory<R, O, ?, ?, ?> adaptedFactory : this.models.values()) {
			if (adaptedFactory instanceof AdaptedParentFactory) {
				AdaptedParentFactory<R, O, ?, ?> parentFactory = (AdaptedParentFactory<R, O, ?, ?>) adaptedFactory;

				// Determine if can create
				if (parentFactory.isCreate()) {
					return true; // able to create parent
				}
			}
		}

		// As here, no able to create a parent
		return false;
	}

	/**
	 * Loads the root {@link Model}.
	 * 
	 * @param rootModel
	 *            Root {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	public void loadRootModel(Model rootModel) {

		// Ensure correct root model
		if (!this.rootModelClass.equals(rootModel.getClass())) {
			throw new IllegalStateException("Incorrect root model type " + rootModel.getClass().getName()
					+ " as configured with " + this.rootModelClass.getName());
		}
		this.rootModel = (R) rootModel;
		this.operations = this.createOperations.apply(this.rootModel);

		// Load the default styling
		AdaptedEditorPlugin.loadDefaulStylesheet(this.contentViewer.getCanvas().getScene());

		// Initialise all the models
		this.models.values().forEach((model) -> model.init(this.injector, this.models));

		// Validate all the models
		this.models.values().forEach((model) -> model.validate());

		// Load with dependencies injected
		OfficeFloorContentPartFactory<R, O> factory = this.injector.getInstance(OfficeFloorContentPartFactory.class);

		// Load the palette models
		List<AdaptedModel<?>> paletteModels = new LinkedList<>();
		for (AbstractAdaptedFactory<R, O, ?, ?, ?> adaptedFactory : this.orderedModels) {
			if (adaptedFactory instanceof AdaptedParentFactory) {
				AdaptedParentFactory<R, O, ?, ?> parentFactory = (AdaptedParentFactory<R, O, ?, ?>) adaptedFactory;

				// Include if able to create
				if (parentFactory.isCreate()) {
					AdaptedModel<?> adaptedPrototype = parentFactory.createPrototype(factory);
					paletteModels.add(adaptedPrototype);
				}
			}
		}
		this.paletteViewer.getContents().setAll(paletteModels);

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
			if (parents != null) {
				contentModels.addAll(parents);
			}
		}

		// Clear any null models
		Iterator<?> iterator = contentModels.iterator();
		while (iterator.hasNext()) {
			Object model = iterator.next();
			if (model == null) {
				iterator.remove();
			}
		}

		// Adapt the content models
		List<AdaptedModel<?>> adaptedContentModels = new ArrayList<AdaptedModel<?>>();
		List<AdaptedParent<?>> adaptedParents = new ArrayList<>();
		for (Model model : contentModels) {

			// Create the adapted parent (which has no parent adapted model)
			AdaptedParent<?> adaptedModel = (AdaptedParent<?>) this.createAdaptedModel(model, null);

			// Add the adapted model (only once)
			if (!adaptedContentModels.contains(adaptedModel)) {
				adaptedContentModels.add(adaptedModel);
				adaptedParents.add(adaptedModel);
			}
		}

		// Load the adapted connections (aferwards so z-order in front)
		for (AdaptedParent<?> adaptedParent : adaptedParents) {
			List<AdaptedConnection<?>> connections = adaptedParent.getConnections();
			if (connections != null) {
				for (AdaptedConnection<?> connection : connections) {

					// Ignore null connections
					if (connection == null) {
						continue;
					}

					// Add the adapted conenction (only once)
					if (!adaptedContentModels.contains(connection)) {
						adaptedContentModels.add(connection);
					}
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
	 * @param parentAdaptedModel
	 *            Parent {@link AdaptedModel}.
	 * @return {@link AbstractAdaptedFactory} for the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	public <M extends Model> AdaptedModel<M> createAdaptedModel(M model, AdaptedModel<?> parentAdaptedModel) {

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
			adapted = builder.newAdaptedModel(this.rootModel, this.operations, parentAdaptedModel, model);
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
		this.errorHandler
				.isError(() -> createConnection.addConnection(source, target, new ModelActionContext<R, O, C>() {

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
					public AdaptedModel<C> getAdaptedModel() {
						return null;
					}

					@Override
					public void overlay(OverlayVisualFactory overlayVisualFactory) {

						// Obtain the location of the target
						Point location = new Point(target.getX(), target.getY());

						// Add the overlay
						OfficeFloorContentPartFactory.this.overlay(location.x, location.y, overlayVisualFactory);
					}

					@Override
					public ChangeExecutor getChangeExecutor() {
						return OfficeFloorContentPartFactory.this.changeExecutor;
					}

					@Override
					public Injector getInjector() {
						return OfficeFloorContentPartFactory.this.injector;
					}

					@Override
					public C position(C model) {

						// Position the model
						model.setX(target.getX());
						model.setY(target.getY());

						// Return the model
						return model;
					}
				}));
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
	 * ======================= AdaptedRootBuilder =========================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <M extends Model, E extends Enum<E>, RE extends Enum<RE>> AdaptedParentBuilder<R, O, M, E> parent(
			M modelPrototype, Function<R, List<M>> getParents, AdaptedModelVisualFactory<M> viewFactory,
			RE... changeParentEvents) {
		this.getParentFunctions.add((Function) getParents);
		return new AdaptedParentFactory<R, O, M, E>(this.rootModelClass.getSimpleName(), modelPrototype, viewFactory,
				this);
	}

	@Override
	public void overlay(double x, double y, OverlayVisualFactory overlayVisualFactory) {
		this.contentViewer.getRootPart().addChild(new AdaptedOverlayHandlePart(new Point(x, y), overlayVisualFactory));
	}

	@Override
	public Property<String> paletteStyle() {
		return this.paletteStyle;
	}

	@Override
	public Property<String> paletteIndicatorStyle() {
		return this.paletteIndicatorStyle;
	}

	@Override
	public void setContentBackground(Background background) {

		// Ensure capture the default background
		if (this.defaultContentBackground == null) {
			this.defaultContentBackground = this.contentViewer.getCanvas().getBackground();
		}

		// Specify the background
		this.contentViewer.getCanvas().setBackground(background == null ? this.defaultContentBackground : background);
	}

	@Override
	public GridModel getGridModel() {
		return this.contentViewer.getAdapter(GridModel.class);
	}

	@Override
	public Property<String> contentStyle() {
		return this.contentStyle;
	}

	@Override
	public AdaptedErrorHandler getErrorHandler() {
		if (this.errorHandler == null) {
			throw new IllegalStateException(
					AdaptedErrorHandler.class.getSimpleName() + " not initialised for " + this.getClass().getName());
		}
		return this.errorHandler;
	}

	@Override
	public ChangeExecutor getChangeExecutor() {
		if (this.changeExecutor == null) {
			throw new IllegalStateException(
					ChangeExecutor.class.getSimpleName() + " not initialised for " + this.getClass().getName());
		}
		return this.changeExecutor;
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
		} else if (content instanceof AdaptedConnectorImpl) {
			return this.injector.getInstance(AdaptedConnectorPart.class);
		}

		// Unknown model
		throw new IllegalArgumentException("Unhandled model " + content.getClass().getName());
	}

}