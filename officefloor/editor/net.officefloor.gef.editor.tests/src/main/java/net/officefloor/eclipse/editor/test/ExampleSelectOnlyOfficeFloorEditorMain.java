package net.officefloor.eclipse.editor.test;

import javafx.application.Application;
import net.officefloor.gef.editor.AdaptedModelStyler;
import net.officefloor.gef.editor.EditorStyler;
import net.officefloor.gef.editor.PaletteIndicatorStyler;
import net.officefloor.gef.editor.PaletteStyler;
import net.officefloor.gef.editor.SelectOnly;

/**
 * Example {@link SelectOnly} {@link ExampleOfficeFloorEditorMain}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleSelectOnlyOfficeFloorEditorMain extends ExampleOfficeFloorEditorMain {

	/**
	 * Main to run the editor.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/**
	 * Configure {@link SelectOnly}.
	 */
	public ExampleSelectOnlyOfficeFloorEditorMain() {
		this.setSelectOnly(new SelectOnly() {

			@Override
			public void paletteIndicator(PaletteIndicatorStyler styler) {
				styler.paletteIndicatorStyle().setValue(".palette-indicator { -fx-background-color: lawngreen }");
			}

			@Override
			public void palette(PaletteStyler styler) {
				styler.paletteStyle().setValue(".palette { -fx-background-color: mediumspringgreen }");
			}

			private boolean isContentToggle = false;

			@Override
			public void editor(EditorStyler editorStyler) {
				this.isContentToggle = !this.isContentToggle;
				editorStyler.editorStyle()
						.setValue(this.isContentToggle ? ".editor { -fx-background-color: lawngreen }" : null);
				editorStyler.getGridModel().setShowGrid(!this.isContentToggle);
			}

			@Override
			public void model(AdaptedModelStyler styler) {
				int red = (int) (Math.random() * 255);
				int green = (int) (Math.random() * 255);
				int blue = (int) (Math.random() * 255);
				styler.style().setValue("." + styler.getModel().getClass().getSimpleName()
						+ " { -fx-background-color: rgb(" + red + "," + green + "," + blue + ") }");
			}
		});
	}
}