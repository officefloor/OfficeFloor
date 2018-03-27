/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.configurer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} configurer that uses JavaFx.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaFxConfigurationBuilder<M> implements ConfigurationBuilder<M> {

	/**
	 * Loads the configuration to {@link Composite}.
	 * 
	 * @param model
	 *            Model.
	 * @param parent
	 *            Parent {@link Composite}.
	 */
	public void loadConfiguration(M model, Composite parent) {

		// Create pane for configuration components
		Pane pane = new Pane();

		// Create the FX Canvas
		FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				this.getScene().getWindow().sizeToScene();
				int width = (int) getScene().getWidth();
				int height = (int) getScene().getHeight();
				return new Point(width, height);
			}
		};

		// Load scene into canvas
		org.eclipse.swt.graphics.Color background = parent.getBackground();
		Scene scene = new Scene(pane, Color.rgb(background.getRed(), background.getGreen(), background.getBlue()));

		// TODO provide styling

		// Load the scene to the canvas
		fxCanvas.setScene(scene);

		// Load the configuration
		this.loadConfiguration(model, pane);
	}

	/**
	 * Loads the configuration to the parent {@link Pane}.
	 * 
	 * @param model
	 *            Model.
	 * @param parent
	 *            Parent {@link Pane}.
	 */
	public void loadConfiguration(M model, Pane parent) {
		parent.getChildren().add(new Label(model.getClass().getSimpleName()));
	}

	/*
	 * ================== ConfigurationBuilder ====================
	 */

	@Override
	public TextBuilder<M> text(String label, ValueLoader<M, String> textLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassBuilder<M> clazz(String label, ValueLoader<M, String> classLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableStringValue resource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableBooleanValue flag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validate(ValueValidator<M> validator) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E extends Enum<E>> ChoiceBuilder<M, E> choices(String label, Class<E> choiceEnumClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <I> ItemBuilder<I> list(String label, Class<I> itemClass, ValueLoader<M, List<I>> itemsLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertiesBuilder<M> properties(String label, ValueLoader<M, PropertyList> propertiesLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void map(String label, Function<M, List<String>> getMappedItems,
			ValueLoader<M, Map<String, String>> mapping) {
		// TODO Auto-generated method stub

	}

	@Override
	public void apply(Consumer<M> applier) {
		// TODO Auto-generated method stub

	}

}