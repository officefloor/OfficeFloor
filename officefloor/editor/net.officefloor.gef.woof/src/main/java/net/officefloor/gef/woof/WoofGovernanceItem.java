/*-
 * #%L
 * [bundle] Section Editor
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

package net.officefloor.gef.woof;

import java.util.List;

import org.eclipse.gef.geometry.planar.Dimension;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedAreaBuilder;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.gef.editor.ParentToAreaConnectionModel;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
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
	public Node visual(WoofGovernanceModel model, AdaptedChildVisualFactoryContext<WoofGovernanceModel> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.OBJECT, ParentToAreaConnectionModel.class).getNode());
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
	public WoofGovernanceItem item(WoofGovernanceModel model) {
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
	protected void furtherAdapt(
			AdaptedParentBuilder<WoofModel, WoofChanges, WoofGovernanceModel, WoofGovernanceEvent> builder) {

		// Allow adding area
		builder.action((context) -> {
			context.getChangeExecutor()
					.execute(context.getOperations().addGovernanceArea(context.getAdaptedModel().getModel(), 100, 80));
		}, DefaultImages.ADD);

		// Governance Area
		AdaptedAreaBuilder<WoofModel, WoofChanges, WoofGovernanceAreaModel, WoofGovernanceEvent> area = builder.area(
				new WoofGovernanceAreaModel(), (p) -> p.getGovernanceAreas(),
				(a) -> new Dimension(a.getWidth(), a.getHeight()), (a, dimension) -> {
					a.setWidth((int) dimension.getWidth());
					a.setHeight((int) dimension.getHeight());
				}, WoofGovernanceEvent.ADD_GOVERNANCE_AREA, WoofGovernanceEvent.REMOVE_GOVERNANCE_AREA);
		area.action((context) -> {
			context.getChangeExecutor()
					.execute(context.getOperations().removeGovernanceArea(context.getAdaptedModel().getModel()));
		}, DefaultImages.DELETE);
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
				Class<? extends GovernanceSource> governanceSourceClass = this.getConfigurableContext()
						.getEnvironmentBridge().loadClass(item.sourceClassName, GovernanceSource.class);

				// Obtain the loader
				GovernanceLoader loader = this.getConfigurableContext().getEnvironmentBridge().getOfficeFloorCompiler()
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
