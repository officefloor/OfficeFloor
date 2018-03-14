/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG and others.
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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.officefloor.eclipse.editor.module.OfficeFloorEditorModule;
import net.officefloor.eclipse.editor.views.ViewersComposite;
import net.officefloor.model.Model;

/**
 * Provides means to test editor configurations without loading the Eclipse
 * platform.
 */
public abstract class AbstractEditorApplication extends Application {

	/**
	 * Builds the {@link AdaptedModel} instances.
	 * 
	 * @param context
	 *            {@link AdaptedBuilderContext}.
	 */
	protected abstract void buildModels(AdaptedBuilderContext context);

	/**
	 * Populate the {@link Model} instances.
	 * 
	 * @param models
	 *            {@link Model} instances.
	 */
	protected abstract void populateModels(List<Model> models);

	/**
	 * <p>
	 * Creates the {@link OfficeFloorEditorModule}.
	 * <p>
	 * Allows overriding the {@link OfficeFloorEditorModule}.
	 * 
	 * @return {@link OfficeFloorEditorModule}.
	 */
	protected OfficeFloorEditorModule createModule() {
		return new OfficeFloorEditorModule((context) -> this.buildModels(context));
	}

	/*
	 * ==================== Application ==========================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Create the module
		OfficeFloorEditorModule module = this.createModule();
		Injector injector = Guice.createInjector(module);

		// Obtain the viewers
		IDomain domain = injector.getInstance(IDomain.class);
		IViewer content = domain.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
		IViewer palette = domain.getAdapter(AdapterKey.get(IViewer.class, OfficeFloorEditorModule.PALETTE_VIEWER_ROLE));

		// Setup visuals
		Scene scene = new Scene(new ViewersComposite(content, palette).getComposite());
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setWidth(640);
		stage.setHeight(480);
		stage.setTitle(this.getClass().getSimpleName());

		// Provide CSS based on module
		scene.getStylesheets().add(this.getClass().getName().replace('.', '/') + ".css");

		// Show
		stage.show();

		// Activate the domain
		domain.activate();

		// Load the models
		List<Model> models = new LinkedList<>();
		this.populateModels(models);
		content.getContents().setAll(models);
	}

}