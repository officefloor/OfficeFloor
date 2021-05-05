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

package net.officefloor.gef.editor;

import java.util.Arrays;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.common.adapt.inject.AdaptableScopes;
import org.eclipse.gef.common.adapt.inject.AdapterInjectionSupport;
import org.eclipse.gef.common.adapt.inject.AdapterInjectionSupport.LoggingMode;
import org.eclipse.gef.common.adapt.inject.AdapterMaps;
import org.eclipse.gef.mvc.fx.MvcFxModule;
import org.eclipse.gef.mvc.fx.behaviors.ContentPartPool;
import org.eclipse.gef.mvc.fx.behaviors.FocusBehavior;
import org.eclipse.gef.mvc.fx.behaviors.HoverBehavior;
import org.eclipse.gef.mvc.fx.behaviors.HoverIntentBehavior;
import org.eclipse.gef.mvc.fx.behaviors.SelectionBehavior;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.handlers.BendFirstAnchorageOnSegmentHandleDragHandler;
import org.eclipse.gef.mvc.fx.handlers.ConnectedSupport;
import org.eclipse.gef.mvc.fx.handlers.DeleteSelectedOnTypeHandler;
import org.eclipse.gef.mvc.fx.handlers.FocusAndSelectOnClickHandler;
import org.eclipse.gef.mvc.fx.handlers.HoverOnHoverHandler;
import org.eclipse.gef.mvc.fx.handlers.ResizeTransformSelectedOnHandleDragHandler;
import org.eclipse.gef.mvc.fx.handlers.ResizeTranslateFirstAnchorageOnHandleDragHandler;
import org.eclipse.gef.mvc.fx.handlers.RotateSelectedOnRotateHandler;
import org.eclipse.gef.mvc.fx.handlers.SelectAllOnTypeHandler;
import org.eclipse.gef.mvc.fx.handlers.SelectFocusedOnTypeHandler;
import org.eclipse.gef.mvc.fx.handlers.SnapToGeometry;
import org.eclipse.gef.mvc.fx.handlers.SnapToGrid;
import org.eclipse.gef.mvc.fx.handlers.TranslateSelectedOnDragHandler;
import org.eclipse.gef.mvc.fx.handlers.TraverseFocusOnTypeHandler;
import org.eclipse.gef.mvc.fx.models.FocusModel;
import org.eclipse.gef.mvc.fx.models.HoverModel;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.parts.CircleSegmentHandlePart;
import org.eclipse.gef.mvc.fx.parts.DefaultFocusFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultHoverFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultHoverIntentHandlePartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultSelectionFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultSelectionHandlePartFactory;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.SquareSegmentHandlePart;
import org.eclipse.gef.mvc.fx.policies.BendConnectionPolicy;
import org.eclipse.gef.mvc.fx.policies.ResizePolicy;
import org.eclipse.gef.mvc.fx.policies.TransformPolicy;
import org.eclipse.gef.mvc.fx.providers.BoundsSnappingLocationProvider;
import org.eclipse.gef.mvc.fx.providers.CenterSnappingLocationProvider;
import org.eclipse.gef.mvc.fx.providers.DefaultAnchorProvider;
import org.eclipse.gef.mvc.fx.providers.GeometricOutlineProvider;
import org.eclipse.gef.mvc.fx.providers.ISnappingLocationProvider;
import org.eclipse.gef.mvc.fx.providers.ShapeBoundsProvider;
import org.eclipse.gef.mvc.fx.providers.TopLeftSnappingLocationProvider;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.multibindings.MapBinder;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.officefloor.gef.editor.internal.behaviors.PaletteFocusBehavior;
import net.officefloor.gef.editor.internal.handlers.CreateAdaptedConnectionOnDragHandler;
import net.officefloor.gef.editor.internal.handlers.CreateAdaptedParentOnDragHandler;
import net.officefloor.gef.editor.internal.models.ActiveConnectionSourceModel;
import net.officefloor.gef.editor.internal.models.ChangeExecutorImpl;
import net.officefloor.gef.editor.internal.parts.AdaptedAreaPart;
import net.officefloor.gef.editor.internal.parts.AdaptedConnectionPart;
import net.officefloor.gef.editor.internal.parts.AdaptedConnectorPart;
import net.officefloor.gef.editor.internal.parts.AdaptedParentPart;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.gef.editor.internal.parts.OfficeFloorFocusFeedbackPartFactory;
import net.officefloor.gef.editor.internal.parts.OfficeFloorHoverFeedbackPartFactory;
import net.officefloor.gef.editor.internal.parts.OfficeFloorHoverIntentHandlePartFactory;
import net.officefloor.gef.editor.internal.parts.OfficeFloorSelectionFeedbackPartFactory;
import net.officefloor.gef.editor.internal.parts.OfficeFloorSelectionHandlePartFactory;
import net.officefloor.gef.editor.internal.parts.PaletteRootPart;
import net.officefloor.gef.editor.internal.policies.ContentRestrictedChangeViewportPolicy;
import net.officefloor.gef.editor.internal.views.ViewersComposite;
import net.officefloor.gef.editor.style.StyleRegistry;
import net.officefloor.model.Model;

public class AdaptedEditorModule extends MvcFxModule {

	/**
	 * Palette {@link IViewer} role.
	 */
	public static final String PALETTE_VIEWER_ROLE = "paletteViewer";

	/**
	 * {@link Injector}.
	 */
	private Injector injector;

	/**
	 * {@link IDomain}.
	 */
	private IDomain domain;

	/**
	 * {@link IViewer} content.
	 */
	private IViewer content;

	/**
	 * {@link IViewer} palette.
	 */
	private IViewer palette;

	/**
	 * {@link ViewersComposite}.
	 */
	private ViewersComposite viewersComposite;

	/**
	 * Drag latency. Higher values provide better drag performance, while lower
	 * provides better responsiveness.
	 */
	private int dragLatency = 5;

	/**
	 * {@link SelectOnly}.
	 */
	private SelectOnly selectOnly = null;

	/**
	 * {@link OfficeFloorContentPartFactory}.
	 */
	private OfficeFloorContentPartFactory<?, ?> factory;

	/**
	 * Specifies the drag latency.
	 * 
	 * @param dragLatency Drag latency. Higher values provide better drag
	 *                    performance, while lower provides better responsiveness.
	 */
	public void setDragLatency(int dragLatency) {
		this.dragLatency = dragLatency;
	}

	/**
	 * Flags that the editor is select only.
	 * 
	 * @param selectOnly {@link SelectOnly}.
	 */
	public void setSelectOnly(SelectOnly selectOnly) {
		if (this.factory != null) {
			throw new IllegalStateException(
					"May not configure " + SelectOnly.class.getSimpleName() + " after loading editor");
		}

		// Specify select only
		this.selectOnly = selectOnly;
	}

	/*
	 * ========== Convenience creation methods ==============
	 */

	/**
	 * Creates the parent {@link Pane}.
	 * 
	 * @param adaptedBuilder {@link AdaptedBuilder}.
	 * @return Parent {@link Pane}.
	 */
	public Pane createParent(AdaptedBuilder adaptedBuilder) {

		// Lazy initialise
		if (this.domain == null) {

			// Create the injector from this module
			Injector injector = Guice.createInjector(this);

			// Load the domain and viewers
			IDomain domain = injector.getInstance(IDomain.class);
			this.initialise(domain, injector);
		}

		// Lazy configure
		if (this.factory == null) {
			this.configure(adaptedBuilder);
		}

		// Return the composite
		return this.viewersComposite.getComposite();
	}

	/**
	 * Activates the {@link IDomain}.
	 * 
	 * @param rootModel Root {@link Model}.
	 * @return {@link Property} to change the root {@link Model}.
	 */
	public <R extends Model> Property<R> activateDomain(R rootModel) {

		// Load the root model
		Property<R> rootModelProperty = this.loadRootModel(rootModel);

		// Activate domain
		this.domain.activate();

		// Return the root model property
		return rootModelProperty;
	}

	/*
	 * =========== EditorPart creation methods ================
	 */

	/**
	 * <p>
	 * Initialises.
	 * <p>
	 * This may be called before JavaFx has been initialised.
	 * 
	 * @param domain   {@link IDomain}.
	 * @param injector {@link Injector}.
	 */
	public void initialise(IDomain domain, Injector injector) {
		this.injector = injector;

		// Load domain and views
		this.domain = domain;
		this.content = this.domain.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
		this.palette = this.domain.getAdapter(AdapterKey.get(IViewer.class, AdaptedEditorModule.PALETTE_VIEWER_ROLE));
	}

	/**
	 * <p>
	 * Configures.
	 * <p>
	 * This must be called after JavaFX has been initialised.
	 * 
	 * @param adaptedBuilder {@link AdaptedBuilder}.
	 */
	public void configure(AdaptedBuilder adaptedBuilder) {

		// Load the factory
		this.factory = this.injector.getInstance(OfficeFloorContentPartFactory.class);

		// Create the style registry for the factory
		StyleRegistry styleRegistry = AdaptedEditorPlugin.createStyleRegistry();

		// Load the viewers and change executor
		this.viewersComposite = new ViewersComposite(this.content, this.palette, this.selectOnly);
		ChangeExecutor changeExecutor = new ChangeExecutorImpl(this.factory, this.domain);
		this.factory.init(this.injector, this.viewersComposite.getComposite(), this.content,
				this.viewersComposite.getPaletteIndicator(), this.palette, this.viewersComposite, changeExecutor,
				styleRegistry, this.dragLatency, this.selectOnly);

		// Configure the models
		adaptedBuilder.build(this.factory);
	}

	/**
	 * Obtains the content {@link IViewer}.
	 * 
	 * @return Content {@link IViewer}.
	 */
	public IViewer getContentViewer() {
		return this.content;
	}

	/**
	 * Obtains the palette {@link IViewer}.
	 * 
	 * @return Palette {@link IViewer}.
	 */
	public IViewer getPaletteViewer() {
		return this.palette;
	}

	/**
	 * Loads the root {@link Model}.
	 * 
	 * @param rootModel Root {@link Model}.
	 * @return {@link Property} to change the root {@link Model}.
	 */
	public <R extends Model> Property<R> loadRootModel(R rootModel) {

		// Configured so initialise the factory
		this.viewersComposite.init(this.factory.isCreateParent());

		// Connect up scene
		this.viewersComposite.getComposite().applyCss();

		// Load the root model
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Property<R> rootModelProperty = ((OfficeFloorContentPartFactory) this.factory).loadRootModel(rootModel);

		// Completed load
		this.viewersComposite.loadComplete();

		// Return the root model property
		return rootModelProperty;
	}

	/*
	 * ======================= Module ============================
	 */

	@Override
	protected void bindAbstractContentPartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		super.bindAbstractContentPartAdapters(adapterMapBinder);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(FocusAndSelectOnClickHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(SelectFocusedOnTypeHandler.class);
	}

	@Override
	protected void bindFocusFeedbackPartFactoryAsContentViewerAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(FocusBehavior.FOCUS_FEEDBACK_PART_FACTORY))
				.to(OfficeFloorFocusFeedbackPartFactory.class);
	}

	@Override
	protected void bindSelectionFeedbackPartFactoryAsContentViewerAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(SelectionBehavior.SELECTION_FEEDBACK_PART_FACTORY))
				.to(OfficeFloorSelectionFeedbackPartFactory.class);
	}

	@Override
	protected void bindHoverFeedbackPartFactoryAsContentViewerAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(HoverBehavior.HOVER_FEEDBACK_PART_FACTORY))
				.to(OfficeFloorHoverFeedbackPartFactory.class);
	}

	protected void bindCircleSegmentHandlePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(BendFirstAnchorageOnSegmentHandleDragHandler.class);
	}

	protected void bindSquareSegmentHandlePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole())
				.to(ResizeTranslateFirstAnchorageOnHandleDragHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ResizeTransformSelectedOnHandleDragHandler.class);
	}

	protected void bindContentPartPoolAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ContentPartPool.class);
	}

	protected void bindContentRestrictedChangeViewportPolicyAsFXRootPartAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ContentRestrictedChangeViewportPolicy.class);
	}

	protected void bindFocusFeedbackFactoryAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(FocusBehavior.FOCUS_FEEDBACK_PART_FACTORY))
				.to(OfficeFloorFocusFeedbackPartFactory.class);
	}

	protected void bindFocusModelAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(FocusModel.class);
	}

	protected void bindFXPaletteViewerAsFXDomainAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(PALETTE_VIEWER_ROLE)).to(IViewer.class);
	}

	protected void bindAdaptedConnectionInContentViewerContext(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		// hover on hover
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);

		// geometry provider for selection feedback
		adapterMapBinder
				.addBinding(AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		adapterMapBinder
				.addBinding(AdapterKey.role(DefaultSelectionHandlePartFactory.SELECTION_HANDLES_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		adapterMapBinder
				.addBinding(
						AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_LINK_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		adapterMapBinder.addBinding(AdapterKey.role(DefaultFocusFeedbackPartFactory.FOCUS_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);

		// Allow moving
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ResizePolicy.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(BendConnectionPolicy.class);

		// Interaction handler to relocate on drag
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TranslateSelectedOnDragHandler.class);
	}

	protected void bindAdaptedParentPartInPaletteViewerContext(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreateAdaptedParentOnDragHandler.class);
		adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER))
				.to(ShapeBoundsProvider.class);
	}

	protected void bindHoverFeedbackFactoryAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(HoverBehavior.HOVER_FEEDBACK_PART_FACTORY))
				.to(OfficeFloorHoverFeedbackPartFactory.class);
	}

	protected void bindHoverHandleFactoryAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(HoverIntentBehavior.HOVER_INTENT_HANDLE_PART_FACTORY))
				.to(DefaultHoverIntentHandlePartFactory.class);
	}

	@Override
	protected void bindHoverHandlePartFactoryAsContentViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(HoverIntentBehavior.HOVER_INTENT_HANDLE_PART_FACTORY))
				.to(OfficeFloorHoverIntentHandlePartFactory.class);
	}

	protected void bindHoverModelAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverModel.class);
	}

	protected void bindIContentPartFactory() {
		OfficeFloorContentPartFactory<?, ?> contentPartFactory = new OfficeFloorContentPartFactory<>();
		binder().bind(IContentPartFactory.class).toInstance(contentPartFactory);
		binder().bind(OfficeFloorContentPartFactory.class).toInstance(contentPartFactory);
	}

	protected void bindIContentPartFactoryAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(IContentPartFactory.class);
	}

	@Override
	protected void bindIDomainAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		super.bindIDomainAdapters(adapterMapBinder);
		bindPaletteViewerAsDomainAdapter(adapterMapBinder);
	}

	@Override
	protected void bindIRootPartAdaptersForContentViewer(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		super.bindIRootPartAdaptersForContentViewer(adapterMapBinder);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(DeleteSelectedOnTypeHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(RotateSelectedOnRotateHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TraverseFocusOnTypeHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(SelectFocusedOnTypeHandler.class);
		bindSelectAllOnTypeHandlerAsContentViewerRootPartAdapter(adapterMapBinder);
	}

	protected void bindPaletteFocusBehaviorAsFXRootPartAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(PaletteFocusBehavior.class);
	}

	protected void bindPaletteRootPartAdaptersInPaletteViewerContext(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		bindHoverOnHoverHandlerAsIRootPartAdapter(adapterMapBinder);
		bindPanOrZoomOnScrollHandlerAsIRootPartAdapter(adapterMapBinder);
		bindPanOnTypeHandlerAsIRootPartAdapter(adapterMapBinder);
		bindContentRestrictedChangeViewportPolicyAsFXRootPartAdapter(adapterMapBinder);
		bindContentBehaviorAsIRootPartAdapter(adapterMapBinder);
		bindHoverBehaviorAsIRootPartAdapter(adapterMapBinder);
		bindPaletteFocusBehaviorAsFXRootPartAdapter(adapterMapBinder);
		bindFocusTraversalPolicyAsIRootPartAdapter(adapterMapBinder);
	}

	protected void bindPaletteRootPartAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(PALETTE_VIEWER_ROLE)).to(PaletteRootPart.class)
				.in(AdaptableScopes.typed(IViewer.class));
	}

	protected void bindPaletteViewerAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		// viewer models
		bindFocusModelAsPaletteViewerAdapter(adapterMapBinder);
		bindHoverModelAsPaletteViewerAdapter(adapterMapBinder);
		bindSelectionModelAsPaletteViewerAdapter(adapterMapBinder);

		// root part
		bindPaletteRootPartAsPaletteViewerAdapter(adapterMapBinder);

		// feedback and handles factories
		bindSelectionFeedbackFactoryAsPaletteViewerAdapter(adapterMapBinder);
		bindFocusFeedbackFactoryAsPaletteViewerAdapter(adapterMapBinder);
		bindHoverFeedbackFactoryAsPaletteViewerAdapter(adapterMapBinder);
		bindSelectionHandleFactoryAsPaletteViewerAdapter(adapterMapBinder);
		bindHoverHandleFactoryAsPaletteViewerAdapter(adapterMapBinder);

		// content part factory and content part pool
		bindContentPartPoolAsPaletteViewerAdapter(adapterMapBinder);
		bindIContentPartFactoryAsPaletteViewerAdapter(adapterMapBinder);

		// change hover feedback color by binding a respective provider
		adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_COLOR_PROVIDER))
				.toInstance(new Provider<Color>() {
					@Override
					public Color get() {
						return Color.WHITE;
					}
				});
	}

	protected void bindPaletteViewerAsDomainAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(PALETTE_VIEWER_ROLE)).to(IViewer.class);
	}

	protected void bindSelectAllOnTypeHandlerAsContentViewerRootPartAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(SelectAllOnTypeHandler.class);
	}

	protected void bindSelectionFeedbackFactoryAsPaletteViewerAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(SelectionBehavior.SELECTION_FEEDBACK_PART_FACTORY))
				.to(DefaultSelectionFeedbackPartFactory.class);
	}

	protected void bindSelectionHandleFactoryAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(SelectionBehavior.SELECTION_HANDLE_PART_FACTORY))
				.to(DefaultSelectionHandlePartFactory.class);
	}

	@Override
	protected void bindSelectionHandlePartFactoryAsContentViewerAdapter(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(SelectionBehavior.SELECTION_HANDLE_PART_FACTORY))
				.to(OfficeFloorSelectionHandlePartFactory.class);
	}

	protected void bindSelectionModelAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(SelectionModel.class);
	}

	protected void bindAdaptedParentInContentViewerContext(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {

		// Hover feedback
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);
		adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER))
				.to(ShapeBoundsProvider.class);
		adapterMapBinder
				.addBinding(AdapterKey.role(DefaultHoverIntentHandlePartFactory.HOVER_INTENT_HANDLES_GEOMETRY_PROVIDER))
				.to(ShapeBoundsProvider.class);

		// Selection feedback
		adapterMapBinder
				.addBinding(AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_FEEDBACK_GEOMETRY_PROVIDER))
				.toProvider(new Provider<ShapeBoundsProvider>() {
					@Override
					public ShapeBoundsProvider get() {
						return new ShapeBoundsProvider(0.5);
					}
				});
		adapterMapBinder
				.addBinding(AdapterKey.role(DefaultSelectionHandlePartFactory.SELECTION_HANDLES_GEOMETRY_PROVIDER))
				.toProvider(new Provider<ShapeBoundsProvider>() {
					@Override
					public ShapeBoundsProvider get() {
						return new ShapeBoundsProvider(0.5);
					}
				});
		adapterMapBinder
				.addBinding(
						AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_LINK_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);

		// Focus feedback
		adapterMapBinder.addBinding(AdapterKey.role(DefaultFocusFeedbackPartFactory.FOCUS_FEEDBACK_GEOMETRY_PROVIDER))
				.toProvider(new Provider<ShapeBoundsProvider>() {
					@Override
					public ShapeBoundsProvider get() {
						return new ShapeBoundsProvider(0.5);
					}
				});

		// Handle relocate (if part supports it)
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TranslateSelectedOnDragHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TransformPolicy.class);

		// Handle resize (if part supports it)
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ResizePolicy.class);
	}

	protected void bindAdaptedConnectorInContentViewerContext(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {

		// Bind dynamic anchor provider
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(DefaultAnchorProvider.class);

		// Drag to create connection
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreateAdaptedConnectionOnDragHandler.class);

		// Normalize connected on drag
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ConnectedSupport.class);
		adapterMapBinder.addBinding(AdapterKey.role(SnapToGrid.SOURCE_SNAPPING_LOCATION_PROVIDER))
				.to(TopLeftSnappingLocationProvider.class);
		adapterMapBinder.addBinding(AdapterKey.role(SnapToGeometry.SOURCE_SNAPPING_LOCATION_PROVIDER))
				.toInstance(ISnappingLocationProvider.union(
						Arrays.asList(new CenterSnappingLocationProvider(), new BoundsSnappingLocationProvider())));
		adapterMapBinder.addBinding(AdapterKey.role(SnapToGeometry.TARGET_SNAPPING_LOCATION_PROVIDER))
				.to(BoundsSnappingLocationProvider.class);
	}

	@Override
	protected void configure() {
		super.configure();

		// Bind the content part factory
		bindIContentPartFactory();

		// Bind in the models
		bindAdaptedParentInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(), AdaptedParentPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));
		MapBinder<AdapterKey<?>, Object> areaBinder = AdapterMaps.getAdapterMapBinder(binder(), AdaptedAreaPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
		bindAdaptedParentInContentViewerContext(areaBinder);
		bindAdaptedConnectorInContentViewerContext(areaBinder);
		bindAdaptedConnectorInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(), AdaptedConnectorPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));
		bindAdaptedConnectionInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(),
				AdaptedConnectionPart.class, AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));

		// Bind listening for active adapted child in connection drag creation
		ActiveConnectionSourceModel activeConnectionSource = new ActiveConnectionSourceModel();
		binder().bind(ActiveConnectionSourceModel.class).toInstance(activeConnectionSource);

		// Connection selection handles
		bindSquareSegmentHandlePartAdapters(AdapterMaps.getAdapterMapBinder(binder(), SquareSegmentHandlePart.class));
		bindCircleSegmentHandlePartAdapters(AdapterMaps.getAdapterMapBinder(binder(), CircleSegmentHandlePart.class));

		// Palette
		bindPaletteViewerAdapters(AdapterMaps.getAdapterMapBinder(binder(), IViewer.class,
				AdapterKey.get(IViewer.class, PALETTE_VIEWER_ROLE)));
		bindPaletteRootPartAdaptersInPaletteViewerContext(AdapterMaps.getAdapterMapBinder(binder(), IRootPart.class,
				AdapterKey.get(IViewer.class, PALETTE_VIEWER_ROLE)));
		bindAdaptedParentPartInPaletteViewerContext(AdapterMaps.getAdapterMapBinder(binder(), AdaptedParentPart.class,
				AdapterKey.get(IViewer.class, PALETTE_VIEWER_ROLE)));
	}

	@Override
	protected void enableAdapterMapInjection() {
		install(new AdapterInjectionSupport(LoggingMode.PRODUCTION));
	}

}
