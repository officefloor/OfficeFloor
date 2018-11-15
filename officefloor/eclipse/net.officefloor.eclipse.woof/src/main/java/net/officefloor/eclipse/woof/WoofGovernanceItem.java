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

import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedModelVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.eclipse.ide.editor.AbstractItem;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel.WoofGovernanceEvent;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;

/**
 * Configuration for the {@link WoofGovernanceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofGovernanceItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofGovernanceModel, WoofGovernanceEvent, WoofGovernanceItem> {

	/**
	 * Test configuration.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		WoofEditor.launchConfigurer(new WoofGovernanceItem(), (model) -> {
			model.setWoofGovernanceName("Governance");
			model.setGovernanceSourceClassName(ClassGovernanceSource.class.getName());
			model.addProperty(
					new PropertyModel(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, Object.class.getName()));
		});
	}

	/**
	 * {@link Governance} name.
	 */
	private String name;

	/**
	 * {@link GovernanceSource} {@link Class} name.
	 */
	private String sourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link GovernanceType}.
	 */
	private GovernanceType<?, ?> type;

	/*
	 * ================ AbstractConfigurableItem =====================
	 */

	@Override
	public WoofGovernanceModel prototype() {
		return new WoofGovernanceModel("Governance", null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((parent) -> parent.getWoofGovernances(), WoofEvent.ADD_WOOF_GOVERNANCE,
				WoofEvent.REMOVE_WOOF_GOVERNANCE);
	}

	@Override
	public Node visual(WoofGovernanceModel model, AdaptedModelVisualFactoryContext<WoofGovernanceModel> context) {
		HBox container = new HBox();
		context.label(container);
		return container;
	}

	@Override
	public AbstractItem<WoofModel, WoofChanges, WoofModel, WoofEvent, WoofGovernanceModel, WoofGovernanceEvent>.IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofGovernanceName(),
				WoofGovernanceEvent.CHANGE_WOOF_GOVERNANCE_NAME);

	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofGovernanceModel itemModel) {
		parentModel.addWoofGovernance(itemModel);
	}

	@Override
	protected WoofGovernanceItem item(WoofGovernanceModel model) {
		WoofGovernanceItem item = new WoofGovernanceItem();
		if (model != null) {
			item.name = model.getWoofGovernanceName();
			item.sourceClassName = model.getGovernanceSourceClassName();
			item.properties = this.translateToPropertyList(model.getProperties(), (p) -> p.getName(),
					(p) -> p.getValue());
		}
		return item;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 50.0%, khaki, darkkhaki)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "lemonchiffon"));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Governance");

			// Required
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			builder.clazz("Source").init((item) -> item.sourceClassName).superType(GovernanceSource.class)
					.validate(ValueValidator.notEmptyString("Must specify source"))
					.setValue((item, value) -> item.sourceClassName = value);
			builder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);

			// Validate by loading type
			builder.validate((ctx) -> {
				WoofGovernanceItem item = ctx.getModel();

				// Obtain the Governance Source
				Class<? extends GovernanceSource> governanceSourceClass = this.getConfigurableContext().getOsgiBridge()
						.loadClass(item.sourceClassName, GovernanceSource.class);

				// Obtain the loader
				GovernanceLoader loader = this.getConfigurableContext().getOsgiBridge().getOfficeFloorCompiler()
						.getGovernanceLoader();

				// Load the type
				item.type = loader.loadGovernanceType(governanceSourceClass, item.properties);
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addGovernance(item.name, item.sourceClassName, item.properties,
						item.type));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().refactorGovernance(context.getModel(), item.name,
						item.sourceClassName, item.properties, item.type));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeGovernance(context.getModel()));
		});
	}

}