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
package net.officefloor.eclipse.woof;

import java.io.IOException;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofExceptionModel.WoofExceptionEvent;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;

/**
 * Configuration for the {@link WoofExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofExceptionItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofExceptionModel, WoofExceptionEvent, WoofExceptionItem> {

	/**
	 * Test configuration.
	 */
	public static void main(String[] args) {
		WoofEditor.launchConfigurer(new WoofExceptionItem(), (model) -> {
			model.setClassName(IOException.class.getName());
		});
	}

	/**
	 * {@link Exception} {@link Class}.
	 */
	private String exceptionClassName;

	/*
	 * ================ AbstractConfigurableItem =====================
	 */

	@Override
	public WoofExceptionModel prototype() {
		return new WoofExceptionModel("Exception");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofExceptions(), WoofEvent.ADD_WOOF_EXCEPTION,
				WoofEvent.REMOVE_WOOF_EXCEPTION);
	}

	@Override
	public Pane visual(WoofExceptionModel model, AdaptedModelVisualFactoryContext<WoofExceptionModel> context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getClassName(), WoofExceptionEvent.CHANGE_CLASS_NAME);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofExceptionModel itemModel) {
		parentModel.addWoofException(itemModel);
	}

	@Override
	protected WoofExceptionItem item(WoofExceptionModel model) {
		WoofExceptionItem item = new WoofExceptionItem();
		if (model != null) {
			item.exceptionClassName = model.getClassName();
		}
		return item;
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Exception");
			builder.clazz("Exception").init((item) -> item.exceptionClassName).superType(Throwable.class)
					.validate(ValueValidator.notEmptyString("Must provide exception class"))
					.setValue((item, value) -> item.exceptionClassName = value);

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addException(item.exceptionClassName));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().refactorException(context.getModel(), item.exceptionClassName));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeException(context.getModel()));

		});
	}

}