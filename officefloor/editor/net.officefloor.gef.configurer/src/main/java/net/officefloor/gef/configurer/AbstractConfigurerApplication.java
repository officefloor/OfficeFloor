package net.officefloor.gef.configurer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.officefloor.gef.editor.AdaptedEditorPlugin;

/**
 * Provides means to test editor configurations without loading the Eclipse
 * platform.
 */
public abstract class AbstractConfigurerApplication extends Application {

	/**
	 * Loads the configuration.
	 * 
	 * @param pane {@link Pane} to load in the configuration.
	 */
	protected abstract void loadConfiguration(Pane pane);

	/*
	 * ==================== Application ==========================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Setup visuals
		Pane parent = new Pane();
		Scene scene = new Scene(parent);
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setWidth(640);
		stage.setHeight(480);
		stage.setTitle(this.getClass().getSimpleName());
		
		// Initialise for Non-Osgi
		AdaptedEditorPlugin.initNonOsgiEnvironment();

		// Load configuration
		this.loadConfiguration(parent);

		// Show
		stage.show();
	}

}