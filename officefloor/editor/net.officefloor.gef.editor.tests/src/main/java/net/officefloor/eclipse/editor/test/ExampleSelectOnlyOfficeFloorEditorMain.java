/*-
 * #%L
 * net.officefloor.gef.editor.tests
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
