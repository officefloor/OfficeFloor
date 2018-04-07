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

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
	 * Creates the root {@link Model}.
	 * 
	 * @return Root {@link Model}.
	 */
	protected abstract Model createRootModel();

	/**
	 * <p>
	 * Creates the {@link AdaptedEditorModule}.
	 * <p>
	 * Allows overriding the {@link AdaptedEditorModule}.
	 * 
	 * @return {@link AdaptedEditorModule}.
	 */
	protected AdaptedEditorModule createModule() {
		return new AdaptedEditorModule((context) -> this.buildModels(context));
	}

	/*
	 * ==================== Application ==========================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Create the module
		AdaptedEditorModule module = this.createModule();

		// Create the parent
		Parent parent = module.createParent();

		// Setup visuals
		Scene scene = new Scene(parent);
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setWidth(640);
		stage.setHeight(480);
		stage.setTitle(this.getClass().getSimpleName());

		// Load the root model
		Model rootModel = this.createRootModel();
		module.loadRootModel(rootModel);
		
		// Show
		stage.show();
	}

}