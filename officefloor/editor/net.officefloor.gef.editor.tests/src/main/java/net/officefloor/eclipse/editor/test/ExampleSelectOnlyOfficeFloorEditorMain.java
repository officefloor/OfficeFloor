/*-
 * #%L
 * net.officefloor.gef.editor.tests
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
