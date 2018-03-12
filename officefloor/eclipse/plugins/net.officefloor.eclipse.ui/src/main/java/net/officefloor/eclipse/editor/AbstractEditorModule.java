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

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.common.adapt.inject.AdaptableScopes;
import org.eclipse.gef.common.adapt.inject.AdapterInjectionSupport;
import org.eclipse.gef.common.adapt.inject.AdapterInjectionSupport.LoggingMode;
import org.eclipse.gef.common.adapt.inject.AdapterMaps;
import org.eclipse.gef.mvc.fx.MvcFxModule;
import org.eclipse.gef.mvc.fx.behaviors.ConnectionClickableAreaBehavior;
import org.eclipse.gef.mvc.fx.behaviors.ContentPartPool;
import org.eclipse.gef.mvc.fx.behaviors.FocusBehavior;
import org.eclipse.gef.mvc.fx.behaviors.HoverBehavior;
import org.eclipse.gef.mvc.fx.behaviors.HoverIntentBehavior;
import org.eclipse.gef.mvc.fx.behaviors.SelectionBehavior;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.handlers.BendFirstAnchorageOnSegmentHandleDragHandler;
import org.eclipse.gef.mvc.fx.handlers.BendOnSegmentDragHandler;
import org.eclipse.gef.mvc.fx.handlers.DeleteSelectedOnTypeHandler;
import org.eclipse.gef.mvc.fx.handlers.FocusAndSelectOnClickHandler;
import org.eclipse.gef.mvc.fx.handlers.HoverOnHoverHandler;
import org.eclipse.gef.mvc.fx.handlers.RotateSelectedOnRotateHandler;
import org.eclipse.gef.mvc.fx.handlers.SelectAllOnTypeHandler;
import org.eclipse.gef.mvc.fx.handlers.SelectFocusedOnTypeHandler;
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
import org.eclipse.gef.mvc.fx.providers.GeometricOutlineProvider;
import org.eclipse.gef.mvc.fx.providers.ShapeBoundsProvider;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Provider;
import com.google.inject.multibindings.MapBinder;

import javafx.scene.paint.Color;
import net.officefloor.eclipse.editor.behaviors.PaletteFocusBehavior;
import net.officefloor.eclipse.editor.handlers.CloneCurveSupport;
import net.officefloor.eclipse.editor.handlers.CloneOnClickHandler;
import net.officefloor.eclipse.editor.handlers.CreateAndTranslateShapeOnDragHandler;
import net.officefloor.eclipse.editor.handlers.CreateCurveOnDragHandler;
import net.officefloor.eclipse.editor.handlers.CreationMenuItemProvider;
import net.officefloor.eclipse.editor.handlers.CreationMenuOnClickHandler;
import net.officefloor.eclipse.editor.handlers.DeleteFirstAnchorageOnClickHandler;
import net.officefloor.eclipse.editor.parts.GeometricCurveCreationHoverHandlePart;
import net.officefloor.eclipse.editor.parts.GeometricCurvePart;
import net.officefloor.eclipse.editor.parts.GeometricElementDeletionHandlePart;
import net.officefloor.eclipse.editor.parts.GeometricShapePart;
import net.officefloor.eclipse.editor.parts.AdaptedParentPart;
import net.officefloor.eclipse.editor.parts.OfficeFloorContentPartFactory;
import net.officefloor.eclipse.editor.parts.OfficeFloorHoverHandlePartFactory;
import net.officefloor.eclipse.editor.parts.OfficeFloorSelectionHandlePartFactory;
import net.officefloor.eclipse.editor.parts.AdaptedChildPart;
import net.officefloor.eclipse.editor.parts.PaletteRootPart;
import net.officefloor.eclipse.editor.policies.ContentRestrictedChangeViewportPolicy;

public abstract class AbstractEditorModule extends MvcFxModule {

	public static final String PALETTE_VIEWER_ROLE = "paletteViewer";

	/**
	 * Builds the {@link AdaptedBuilder}.
	 * 
	 * @param builder {@link AdaptedBuilder}.
	 */
	protected abstract void buildModels(AdaptedBuilder builder);

	@Override
	protected void bindAbstractContentPartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		super.bindAbstractContentPartAdapters(adapterMapBinder);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(FocusAndSelectOnClickHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(SelectFocusedOnTypeHandler.class);
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

	protected void bindCreateCurveHandlePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreateCurveOnDragHandler.class);
	}

	protected void bindDeleteHandlePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(DeleteFirstAnchorageOnClickHandler.class);
	}

	protected void bindFocusFeedbackFactoryAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(FocusBehavior.FOCUS_FEEDBACK_PART_FACTORY))
				.to(DefaultFocusFeedbackPartFactory.class);
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

	protected void bindGeometricCurvePartAdaptersInContentViewerContext(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		// hover on hover
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);

		// geometry provider for selection feedback
		adapterMapBinder
				.addBinding(AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		// geometry provider for selection handles
		adapterMapBinder
				.addBinding(AdapterKey.role(DefaultSelectionHandlePartFactory.SELECTION_HANDLES_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		adapterMapBinder
				.addBinding(
						AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_LINK_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		// geometry provider for hover feedback
		adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
		// geometry provider for focus feedback
		adapterMapBinder.addBinding(AdapterKey.role(DefaultFocusFeedbackPartFactory.FOCUS_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);

		// transaction policy for resize + transform
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ResizePolicy.class);

		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(BendConnectionPolicy.class);

		// interaction handler to relocate on drag
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TranslateSelectedOnDragHandler.class);

		// drag individual segments
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(BendOnSegmentDragHandler.class);

		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TransformPolicy.class);

		// cloning
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CloneCurveSupport.class);

		// clickable area resizing
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ConnectionClickableAreaBehavior.class);

		// clone on shift+click
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CloneOnClickHandler.class);
	}

	protected void bindGeometricShapePartAdapterInPaletteViewerContext(
			MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreateAndTranslateShapeOnDragHandler.class);
		adapterMapBinder.addBinding(AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER))
				.to(GeometricOutlineProvider.class);
	}

	protected void bindHoverFeedbackFactoryAsPaletteViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		adapterMapBinder.addBinding(AdapterKey.role(HoverBehavior.HOVER_FEEDBACK_PART_FACTORY))
				.to(DefaultHoverFeedbackPartFactory.class);
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

	protected OfficeFloorContentPartFactory bindIContentPartFactory() {
		OfficeFloorContentPartFactory contentPartFactory = new OfficeFloorContentPartFactory();
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
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreationMenuOnClickHandler.class);
		adapterMapBinder.addBinding(AdapterKey.role(CreationMenuOnClickHandler.MENU_ITEM_PROVIDER_ROLE))
				.to(CreationMenuItemProvider.class);
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

	protected void bindModelAdapterAdaptersInContentViewerContext(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {

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

		//
		// // bind dynamic anchor provider
		// adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(DefaultAnchorProvider.class);
		//
		// normalize connected on drag
		// adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ConnectedSupport.class);
		// adapterMapBinder.addBinding(AdapterKey.role(SnapToGrid.SOURCE_SNAPPING_LOCATION_PROVIDER))
		// .to(TopLeftSnappingLocationProvider.class);
		// adapterMapBinder.addBinding(AdapterKey.role(SnapToGeometry.SOURCE_SNAPPING_LOCATION_PROVIDER))
		// .toInstance(ISnappingLocationProvider.union(
		// Arrays.asList(new CenterSnappingLocationProvider(), new
		// BoundsSnappingLocationProvider())));
		// adapterMapBinder.addBinding(AdapterKey.role(SnapToGeometry.TARGET_SNAPPING_LOCATION_PROVIDER))
		// .to(BoundsSnappingLocationProvider.class);
	}

	@Override
	protected void configure() {
		super.configure();

		// Bind the content part factory and initialise it
		OfficeFloorContentPartFactory contentPartFactory = bindIContentPartFactory();
		this.buildModels(contentPartFactory);
		contentPartFactory.validateModels();

		// Bind in the models
		bindModelAdapterAdaptersInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(), AdaptedParentPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));
		AdapterMaps.getAdapterMapBinder(binder(), AdaptedChildPart.class,
				AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));

		// content viewer
		bindGeometricCurvePartAdaptersInContentViewerContext(AdapterMaps.getAdapterMapBinder(binder(),
				GeometricCurvePart.class, AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE)));

		// curve selection handles
		bindCircleSegmentHandlePartAdapters(AdapterMaps.getAdapterMapBinder(binder(), CircleSegmentHandlePart.class));

		bindRectangleSegmentHandlePartAdapters(
				AdapterMaps.getAdapterMapBinder(binder(), RectangleSegmentHandlePart.class));

		// hover handles
		bindDeleteHandlePartAdapters(
				AdapterMaps.getAdapterMapBinder(binder(), GeometricElementDeletionHandlePart.class));
		bindCreateCurveHandlePartAdapters(
				AdapterMaps.getAdapterMapBinder(binder(), GeometricCurveCreationHoverHandlePart.class));

		// palette
		bindPaletteViewerAdapters(AdapterMaps.getAdapterMapBinder(binder(), IViewer.class,
				AdapterKey.get(IViewer.class, PALETTE_VIEWER_ROLE)));
		bindPaletteRootPartAdaptersInPaletteViewerContext(AdapterMaps.getAdapterMapBinder(binder(), IRootPart.class,
				AdapterKey.get(IViewer.class, PALETTE_VIEWER_ROLE)));
		bindGeometricShapePartAdapterInPaletteViewerContext(AdapterMaps.getAdapterMapBinder(binder(),
				GeometricShapePart.class, AdapterKey.get(IViewer.class, PALETTE_VIEWER_ROLE)));
	}

	@Override
	protected void enableAdapterMapInjection() {
		install(new AdapterInjectionSupport(LoggingMode.PRODUCTION));
	}

}