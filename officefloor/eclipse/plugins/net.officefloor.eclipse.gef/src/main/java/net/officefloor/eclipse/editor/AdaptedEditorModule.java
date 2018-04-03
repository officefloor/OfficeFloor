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
package net.officefloor.eclipse.editor;

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
import org.eclipse.gef.mvc.fx.handlers.BendOnSegmentDragHandler;
import org.eclipse.gef.mvc.fx.handlers.ConnectedSupport;
import org.eclipse.gef.mvc.fx.handlers.DeleteSelectedOnTypeHandler;
import org.eclipse.gef.mvc.fx.handlers.FocusAndSelectOnClickHandler;
import org.eclipse.gef.mvc.fx.handlers.HoverOnHoverHandler;
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
import org.eclipse.gef.mvc.fx.parts.RectangleSegmentHandlePart;
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

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.editor.internal.behaviors.PaletteFocusBehavior;
import net.officefloor.eclipse.editor.internal.handlers.CreateAdaptedConnectionOnDragHandler;
import net.officefloor.eclipse.editor.internal.handlers.CreateAdaptedParentOnDragHandler;
import net.officefloor.eclipse.editor.internal.models.ActiveConnectionSourceModel;
import net.officefloor.eclipse.editor.internal.parts.AdaptedConnectionPart;
import net.officefloor.eclipse.editor.internal.parts.AdaptedConnectorPart;
import net.officefloor.eclipse.editor.internal.parts.AdaptedParentPart;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorFocusFeedbackPartFactory;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorHoverFeedbackPartFactory;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorHoverHandlePartFactory;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorSelectionFeedbackPartFactory;
import net.officefloor.eclipse.editor.internal.parts.OfficeFloorSelectionHandlePartFactory;
import net.officefloor.eclipse.editor.internal.parts.PaletteRootPart;
import net.officefloor.eclipse.editor.internal.policies.ContentRestrictedChangeViewportPolicy;
import net.officefloor.eclipse.editor.internal.views.ViewersComposite;
import net.officefloor.model.Model;

public class AdaptedEditorModule extends MvcFxModule {

	/**
	 * Palette {@link IViewer} role.
	 */
	public static final String PALETTE_VIEWER_ROLE = "paletteViewer";

	/**
	 * {@link AdaptedBuilder}.
	 */
	private final AdaptedBuilder adaptedBuilder;

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
	 * Instantiate.
	 * 
	 * @param adaptedBuilder
	 *            {@link AdaptedBuilder}.
	 */
	public AdaptedEditorModule(AdaptedBuilder adaptedBuilder) {
		this.adaptedBuilder = adaptedBuilder;
	}

	/**
	 * Creates the {@link Parent}.
	 * 
	 * @param injector
	 *            {@link Injector} created with this {@link AdaptedEditorModule}.
	 * @return {@link Parent}.
	 */
	public Parent createParent(Injector injector) {
		this.injector = injector;

		// Obtain the viewers
		this.domain = injector.getInstance(IDomain.class);
		this.content = this.domain.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
		this.palette = this.domain.getAdapter(AdapterKey.get(IViewer.class, AdaptedEditorModule.PALETTE_VIEWER_ROLE));

		// Determine if can create any modules
		OfficeFloorContentPartFactory<?, ?> factory = this.injector.getInstance(OfficeFloorContentPartFactory.class);
		Parent composite = new ViewersComposite(this.content, this.palette, factory.isCreateParent()).getComposite();

		// Hide palette if no creation
		if (!factory.isCreateParent()) {
			this.palette.getCanvas().setVisible(false);
		}

		// Return the composite
		return composite;
	}

	/**
	 * Convenience method to create the {@link Parent}.
	 * 
	 * @return {@link Parent}.
	 */
	public Parent createParent() {

		// Create the injector from this modeul
		Injector injector = Guice.createInjector(this);

		// Return the created parent
		return this.createParent(injector);
	}

	/**
	 * Loads the root {@link Model}.
	 * 
	 * @param rootModel
	 *            Root {@link Model}.
	 */
	public <R extends Model> void loadRootModel(R rootModel) {
		OfficeFloorContentPartFactory<?, ?> factory = this.injector.getInstance(OfficeFloorContentPartFactory.class);
		factory.loadRootModel(rootModel, this.content, this.palette);

		// Activate the domain
		this.domain.activate();

		// Load the default styling
		this.content.getCanvas().getScene().getStylesheets().add(this.getClass().getName().replace('.', '/') + ".css");
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

	protected void bindFXRectangleSegmentHandlePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(BendFirstAnchorageOnSegmentHandleDragHandler.class);
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

		// drag individual segments
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(BendOnSegmentDragHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TransformPolicy.class);
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
				.to(OfficeFloorHoverHandlePartFactory.class);
	}

	protected void bindHoverModelAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverModel.class);
	}

	protected OfficeFloorContentPartFactory<?, ?> bindIContentPartFactory() {
		OfficeFloorContentPartFactory<?, ?> contentPartFactory = new OfficeFloorContentPartFactory<>();
		binder().bind(IContentPartFactory.class).toInstance(contentPartFactory);
		binder().bind(OfficeFloorContentPartFactory.class).toInstance(contentPartFactory);
		return contentPartFactory;
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

	protected void bindRectangleSegmentHandlePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(BendFirstAnchorageOnSegmentHandleDragHandler.class);
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

		// register transform policies (writing changes also to model)
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TransformPolicy.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TranslateSelectedOnDragHandler.class);
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

		// Bind the content part factory and initialise it
		OfficeFloorContentPartFactory<?, ?> contentPartFactory = bindIContentPartFactory();
		this.adaptedBuilder.build(contentPartFactory);

		// Bind in the models
		bindAdaptedParentInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(), AdaptedParentPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));
		bindAdaptedConnectorInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(), AdaptedConnectorPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));
		bindAdaptedConnectionInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(),
				AdaptedConnectionPart.class, AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));

		// Bind listening for active adapted child in connection drag creation
		ActiveConnectionSourceModel activeConnectionSource = new ActiveConnectionSourceModel();
		binder().bind(ActiveConnectionSourceModel.class).toInstance(activeConnectionSource);

		// Connection selection handles
		bindCircleSegmentHandlePartAdapters(AdapterMaps.getAdapterMapBinder(binder(), CircleSegmentHandlePart.class));
		bindRectangleSegmentHandlePartAdapters(
				AdapterMaps.getAdapterMapBinder(binder(), RectangleSegmentHandlePart.class));

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