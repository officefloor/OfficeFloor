/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.woof;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofHttpContinuationToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofHttpInputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofProcedureNextToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofProcedureOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofResourceModel.WoofResourceEvent;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;

/**
 * Configuration for the {@link WoofResourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofResourceItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofResourceModel, WoofResourceEvent, WoofResourceItem> {

	/**
	 * Resource path.
	 */
	private String resourcePath;

	/*
	 * ================ AbstractConfigurableItem =====================
	 */

	@Override
	public WoofResourceModel prototype() {
		return new WoofResourceModel("Resource");
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofResources(), WoofEvent.ADD_WOOF_RESOURCE,
				WoofEvent.REMOVE_WOOF_RESOURCE);
	}

	@Override
	public Pane visual(WoofResourceModel model, AdaptedChildVisualFactoryContext<WoofResourceModel> context) {
		HBox container = new HBox();
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, WoofHttpContinuationToWoofResourceModel.class,
						WoofHttpInputToWoofResourceModel.class, WoofTemplateOutputToWoofResourceModel.class,
						WoofSecurityOutputToWoofResourceModel.class, WoofSectionOutputToWoofResourceModel.class,
						WoofExceptionToWoofResourceModel.class, WoofProcedureNextToWoofResourceModel.class,
						WoofProcedureOutputToWoofResourceModel.class).getNode());
		context.label(container);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getResourcePath(), WoofResourceEvent.CHANGE_RESOURCE_PATH);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofResourceModel itemModel) {
		parentModel.addWoofResource(itemModel);
	}

	@Override
	public WoofResourceItem item(WoofResourceModel model) {
		WoofResourceItem item = new WoofResourceItem();
		if (model != null) {
			item.resourcePath = model.getResourcePath();
		}
		return item;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(new IdeStyle().rule("-fx-background-color",
				"radial-gradient(radius 100.0%, mediumslateblue, mediumpurple)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "lavender"));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Resource");
			builder.text("Resource Path").init((item) -> item.resourcePath)
					.validate(ValueValidator.notEmptyString("Must provide resource path"))
					.setValue((item, value) -> item.resourcePath = value);

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addResource(item.resourcePath));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().refactorResource(context.getModel(), item.resourcePath));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeResource(context.getModel()));

		});
	}

}
