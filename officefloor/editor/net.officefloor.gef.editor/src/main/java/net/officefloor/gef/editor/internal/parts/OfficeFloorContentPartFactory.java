/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.gef.editor.internal.parts;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Singleton;

import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.models.GridModel;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.eclipse.gef.mvc.fx.parts.IFeedbackPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Injector;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedArea;
import net.officefloor.gef.editor.AdaptedBuilderContext;
import net.officefloor.gef.editor.AdaptedChild;
import net.officefloor.gef.editor.AdaptedChildVisualFactory;
import net.officefloor.gef.editor.AdaptedConnection;
import net.officefloor.gef.editor.AdaptedConnectionManagementBuilder.ConnectionFactory;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedEditorPlugin;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.AdaptedRootBuilder;
import net.officefloor.gef.editor.ChangeExecutor;
import net.officefloor.gef.editor.ChildrenGroup;
import net.officefloor.gef.editor.ModelActionContext;
import net.officefloor.gef.editor.OverlayVisualFactory;
import net.officefloor.gef.editor.PaletteIndicatorStyler;
import net.officefloor.gef.editor.PaletteStyler;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.editor.internal.models.AbstractAdaptedFactory;
import net.officefloor.gef.editor.internal.models.AdaptedParentFactory;
import net.officefloor.gef.editor.style.StyleRegistry;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

@Singleton
public class OfficeFloorContentPartFactory<R extends Model, O> implements IContentPartFactory, AdaptedRootBuilder<R, O>,
		AdaptedBuilderContext, PaletteIndicatorStyler, PaletteStyler {

	/**
	 * Default drag latency.
	 */
	public static final int DEFAULT_DRAG_LATENCY = 1;

	/**
	 * Indicates if contains an {@link ConnectionModel}.
	 * 
	 * @param targets             Target {@link IVisualPart} instances.
	 * @param createFeedbackParts {@link Function} to create the
	 *                            {@link IFeedbackPart} instances from the filtered
	 *                            list of targets.
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
	private Property<R> rootModel;

	/**
	 * Operations.
	 */
	private O operations;

	/**
	 * Editor {@link Pane}.
	 */
	private Pane editorPane;

	/**
	 * {@link IViewer} for the content.
	 */
	private IViewer contentViewer;

	/**
	 * Style rules for the editor.
	 */
	private Property<String> editorStyle;

	/**
	 * {@link IViewer} for the palette.
	 */
	private IViewer paletteViewer;

	/**
	 * Style rules for the palette {@link IViewer} {@link Pane}.
	 */
	private Property<String> paletteStyle;

	/**
	 * Palette indicator.
	 */
	private Pane paletteIndicator;

	/**
	 * Style rules for the palette indicator.
	 */
	private Property<String> paletteIndicatorStyle;

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
	 * Drag latency.
	 */
	private int dragLatency;

	/**
	 * {@link SelectOnly}. May be <code>null</code>.
	 */
	private SelectOnly selectOnly = null;

	/**
	 * Initialises.
	 * 
	 * @param injector         {@link Injector}.
	 * @param editorPane       Editor {@link Pane}.
	 * @param content          {@link IViewer} content.
	 * @param paletteIndicator Palette indicator {@link Pane}.
	 * @param palette          {@link IViewer} palette.
	 * @param errorHandler     {@link AdaptedErrorHandler}.
	 * @param changeExecutor   {@link ChangeExecutor}.
	 * @param styleRegistry    {@link StyleRegistry}.
	 * @param dragLatency      Drag latency. Higher provides better drag
	 *                         performance, while lower provides better
	 *                         responsiveness.
	 * @param selectOnly       {@link SelectOnly}.
	 */
	public void init(Injector injector, Pane editorPane, IViewer content, Pane paletteIndicator, IViewer palette,
			AdaptedErrorHandler errorHandler, ChangeExecutor changeExecutor, StyleRegistry styleRegistry,
			int dragLatency, SelectOnly selectOnly) {
		this.injector = injector;
		this.editorPane = editorPane;
		this.contentViewer = content;
		this.paletteViewer = palette;
		this.errorHandler = errorHandler;
		this.changeExecutor = changeExecutor;
		this.styleRegistry = styleRegistry;
		this.dragLatency = dragLatency;
		this.selectOnly = selectOnly;

		// Register styling for editor
		this.editorStyle = new SimpleStringProperty(null);
		ReadOnlyProperty<URL> editorUrl = this.styleRegistry.registerStyle("_editor_", this.editorStyle);
		editorUrl.addListener((event, oldValue, newValue) -> {
			if (oldValue != null) {
				this.editorPane.getStylesheets().remove(oldValue.toExternalForm());
			}
			if (newValue != null) {
				this.editorPane.getStylesheets().add(newValue.toExternalForm());
			}
		});
		if (this.selectOnly != null) {
			this.contentViewer.getCanvas().setOnMouseClicked((event) -> {
				this.errorHandler.isError(() -> this.selectOnly.editor(this));
			});
		}

		// Register styling for palette indicator
		this.paletteIndicator = paletteIndicator;
		this.paletteIndicatorStyle = new SimpleStringProperty(null);
		ReadOnlyProperty<URL> paletteIndicatorUrl = this.styleRegistry.registerStyle("_palette_indicator_",
				this.paletteIndicatorStyle);
		paletteIndicatorUrl.addListener((event, oldValue, newValue) -> {
			if (oldValue != null) {
				this.paletteIndicator.getStylesheets().remove(oldValue.toExternalForm());
			}
			if (newValue != null) {
				this.paletteIndicator.getStylesheets().add(newValue.toExternalForm());
			}
		});
		if (this.selectOnly != null) {
			this.paletteIndicator.setOnMouseClicked((event) -> {
				this.errorHandler.isError(() -> this.selectOnly.paletteIndicator(this));
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
					this.errorHandler.isError(() -> this.selectOnly.palette(this));
				}
			});
		}
	}

	/**
	 * Registers the {@link AbstractAdaptedFactory}.
	 *
	 * @param <M>     {@link Model} type.
	 * @param <E>     {@link Model} event type.
	 * @param builder {@link AbstractAdaptedFactory}.
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
	 * @param rootModel Root {@link Model}.
	 */
	public Property<R> loadRootModel(R rootModel) {

		// Lazy create the root model property
		if (this.rootModel == null) {
			this.rootModel = new SimpleObjectProperty<>(rootModel);
			this.loadRootModel(null, rootModel);
			this.rootModel
					.addListener((value, oldRootModel, newRootModel) -> this.loadRootModel(oldRootModel, newRootModel));

		} else {
			// Already created, so just set as new root model
			this.rootModel.setValue(rootModel);
		}

		// Return the root model property
		return this.rootModel;
	}

	/**
	 * Loads the root {@link Model}
	 * 
	 * @throws IllegalStateException If invalid root {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	private void loadRootModel(R oldRootModel, R newRootModel) throws IllegalStateException {

		// Ensure correct root model
		if (newRootModel == null) {
			throw new IllegalStateException("No root model provided");
		}
		if (!this.rootModelClass.equals(newRootModel.getClass())) {
			throw new IllegalStateException("Incorrect root model type " + rootModel.getClass().getName()
					+ " as configured with " + this.rootModelClass.getName());
		}

		// Determine if require initialising (no operations yet loaded)
		boolean isRequireInitialise = (this.operations == null);

		// Load the operations
		this.operations = this.createOperations.apply(newRootModel);

		// Iniitalise
		if (isRequireInitialise) {

			// Load the default styling
			AdaptedEditorPlugin.loadDefaulStylesheet(this.editorPane.getScene());

			// Re-apply styles (after so overrides default style)
			Consumer<Property<String>> reapplyStyle = (style) -> {
				String styleRules = style.getValue();
				if ((styleRules != null) && (styleRules.trim().length() > 0)) {
					style.setValue(""); // clear rules
					style.setValue(styleRules); // re-apply
				}
			};
			reapplyStyle.accept(this.editorStyle);
			reapplyStyle.accept(this.paletteIndicatorStyle);
			reapplyStyle.accept(this.paletteStyle);

			// Initialise all the models
			this.models.values().forEach((model) -> model.init(this.injector, this.models));

			// Validate all the models
			this.models.values().forEach((model) -> model.validate());

			// Load with dependencies injected
			OfficeFloorContentPartFactory<R, O> factory = this.injector
					.getInstance(OfficeFloorContentPartFactory.class);

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
		}

		// Load of content models
		this.loadContentModels();

		// Create property change listener to reload on change
		newRootModel.addPropertyChangeListener((event) -> {
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
			List<? extends Model> parents = getParents.apply(this.rootModel.getValue());
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

		// Adapt the parent models
		List<AdaptedParent<?>> adaptedParents = new ArrayList<>();
		for (Model model : contentModels) {

			// Create the adapted parent (which has no parent adapted model)
			AdaptedParent<?> adaptedParent = (AdaptedParent<?>) this.createAdaptedModel(model, null);

			// Add the adapted parent (only once)
			if (!adaptedContentModels.contains(adaptedParent)) {
				adaptedParents.add(adaptedParent);

				// Listen to adding areas
				adaptedParent.getModel().addPropertyChangeListener((event) -> {

					// Filter to only area events
					if (!adaptedParent.isAreaChangeEvent(event.getPropertyName())) {
						return;
					}

					// Area changed, so update content
					this.loadContentModels();
				});
			}
		}

		// Load the area models into content
		for (AdaptedParent<?> adaptedParent : adaptedParents) {
			for (AdaptedArea<?> adaptedArea : adaptedParent.getAdaptedAreas()) {

				// Add the adapted area (only once)
				if (!adaptedContentModels.contains(adaptedArea)) {
					adaptedContentModels.add(adaptedArea);
				}
			}
		}

		// Load the parent models into content (after areas to ensure z-order above)
		for (AdaptedParent<?> adaptedParent : adaptedParents) {
			adaptedContentModels.add(adaptedParent);
		}

		// Load the adapted connections (afterwards so z-order in front)
		for (AdaptedParent<?> adaptedParent : adaptedParents) {
			List<AdaptedConnection<?>> connections = adaptedParent.getConnections();
			if (connections != null) {
				for (AdaptedConnection<?> connection : connections) {

					// Ignore null connections
					if (connection == null) {
						continue;
					}

					// Add the adapted connection (only once)
					if (!adaptedContentModels.contains(connection)) {
						adaptedContentModels.add(connection);
					}
				}
			}
		}

		// Merge in the adapted models (attempting to keep order)
		List<Object> currentContent = new ArrayList<>(Arrays.asList(this.contentViewer.getContents().toArray()));
		for (Object required : adaptedContentModels) {
			if (!currentContent.contains(required)) {
				// Not added, so add
				this.contentViewer.getContents().add(required);
			}
			currentContent.remove(required); // remove to determine content to remove
		}
		for (Object removed : currentContent) {
			this.contentViewer.getContents().remove(removed);
		}
	}

	/**
	 * Creates the wrapper for the {@link Model}.
	 *
	 * @param <M>                {@link Model} type.
	 * @param model              {@link Model}.
	 * @param parentAdaptedModel Parent {@link AdaptedModel}.
	 * @return {@link AbstractAdaptedFactory} for the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	public <M extends Model> AdaptedModel<M> createAdaptedModel(M model, AdaptedModel<?> parentAdaptedModel) {

		// Ensure have a model
		if (model == null) {
			throw new IllegalArgumentException("Must provide model");
		}

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
			adapted = builder.newAdaptedModel(this.rootModel.getValue(), this.operations, parentAdaptedModel, model);
			this.modelToAdaption.put(model, adapted);
			return adapted;
		}

		// As here, model is not configured
		throw new IllegalStateException("Non-adapted model " + model.getClass().getName());
	}

	/**
	 * Adds the {@link ConnectionModel} to the {@link Model} structure.
	 * 
	 * @param <S>              Source {@link Model} type.
	 * @param <T>              Target {@link Model} type.
	 * @param <C>              {@link ConnectionModel} type.
	 * @param source           Source {@link Model}.
	 * @param target           Target {@link Model}.
	 * @param createConnection {@link ConnectionFactory}.
	 */
	public <S extends Model, T extends Model, C extends ConnectionModel> void addConnection(S source, T target,
			ConnectionFactory<R, O, S, C, T> createConnection) {
		this.errorHandler
				.isError(() -> createConnection.addConnection(source, target, new ModelActionContext<R, O, C>() {

					@Override
					public R getRootModel() {
						return OfficeFloorContentPartFactory.this.rootModel.getValue();
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

	/**
	 * Obtains the drag latency.
	 * 
	 * @return Drag latency.
	 */
	public int getDragLatency() {
		return this.dragLatency;
	}

	/**
	 * Obtains the {@link SelectOnly}.
	 * 
	 * @return {@link SelectOnly} or <code>null</code> if no {@link SelectOnly}.
	 */
	public SelectOnly getSelectOnly() {
		return this.selectOnly;
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
			M modelPrototype, Function<R, List<M>> getParents, AdaptedChildVisualFactory<M> viewFactory,
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
	 * ===================== EditorStyler =========================
	 */

	@Override
	public Parent getEditor() {
		return this.editorPane;
	}

	@Override
	public GridModel getGridModel() {
		return this.contentViewer.getAdapter(GridModel.class);
	}

	@Override
	public Property<String> editorStyle() {
		return this.editorStyle;
	}

	/*
	 * ================== PaletteIndicatorStyler ====================
	 */

	@Override
	public Node getPaletteIndicator() {
		return this.paletteIndicator;
	}

	@Override
	public Property<String> paletteIndicatorStyle() {
		return this.paletteIndicatorStyle;
	}

	/*
	 * ===================== PaletteStyler ==========================
	 */

	@Override
	public Node getPalette() {
		return this.paletteViewer.getCanvas();
	}

	@Override
	public Property<String> paletteStyle() {
		return this.paletteStyle;
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
		} else if (content instanceof AdaptedArea) {
			return this.injector.getInstance(AdaptedAreaPart.class);
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
