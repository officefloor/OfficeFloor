/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.woof;

import java.util.List;
import java.util.Map;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.eclipse.ide.editor.AbstractConfigurableItem;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionModel.WoofSectionEvent;

/**
 * Configuration for the {@link WoofSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionItem extends
		AbstractConfigurableItem<WoofModel, WoofEvent, WoofChanges, WoofSectionModel, WoofSectionEvent, WoofSectionItem> {

	/**
	 * Mock section {@link Class} for testing.
	 */
	public static class MockSection {

		@FlowInterface
		public static interface Flows {
			void flow();
		}

		public void input(Flows flows) {
		}
	}

	/**
	 * Test configuration.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		WoofEditor.launchConfigurer(new WoofSectionItem(), (model) -> {
			model.setWoofSectionName("Section");
			model.setSectionSourceClassName(ClassSectionSource.class.getName());
			model.setSectionLocation(MockSection.class.getName());
		});
	}

	/**
	 * Loads the {@link SectionType} for the {@link WoofSectionItem}.
	 * 
	 * @param item
	 *            {@link WoofSectionItem}.
	 * @param osgiBridge
	 *            {@link OfficeFloorOsgiBridge}.
	 * @return {@link SectionType}.
	 * @throws Exception
	 *             If fails to load the {@link SectionType}.
	 */
	public static SectionType loadSectionType(WoofSectionItem item, OfficeFloorOsgiBridge osgiBridge) throws Exception {
		SectionLoader loader = osgiBridge.getOfficeFloorCompiler().getSectionLoader();
		Class<? extends SectionSource> sourceClass = osgiBridge.loadClass(item.sourceClassName, SectionSource.class);
		return loader.loadSectionType(sourceClass, item.location, item.properties);
	}

	/**
	 * Choice {@link Class}.
	 */
	private static final int CHOICE_CLASS = 0;

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Choice.
	 */
	private Integer choice = null;

	/**
	 * {@link SectionSource} {@link Class} name.
	 */
	private String sourceClassName;

	/**
	 * Location.
	 */
	private String location;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link SectionType}.
	 */
	private SectionType sectionType;

	/**
	 * {@link SectionInputType} name mapping.
	 */
	private Map<String, String> inputNameMapping;

	/**
	 * {@link SectionOutputType} name mapping.
	 */
	private Map<String, String> outputNameMapping;

	/*
	 * =================== AbstractConfigurableItem ====================
	 */

	@Override
	public WoofSectionModel prototype() {
		return new WoofSectionModel("Section", null, null);
	}

	@Override
	public IdeExtractor extract() {
		return new IdeExtractor((root) -> root.getWoofSections(), WoofEvent.ADD_WOOF_SECTION,
				WoofEvent.REMOVE_WOOF_SECTION);
	}

	@Override
	public void loadToParent(WoofModel parentModel, WoofSectionModel itemModel) {
		parentModel.addWoofSection(itemModel);
	}

	@Override
	public Pane visual(WoofSectionModel model, AdaptedChildVisualFactoryContext<WoofSectionModel> context) {
		VBox container = new VBox();
		context.label(container);
		HBox children = context.addNode(container, new HBox());
		VBox inputs = context.addNode(children, new VBox());
		context.childGroup(WoofSectionInputItem.class.getSimpleName(), inputs);
		VBox outputs = context.addNode(children, new VBox());
		context.childGroup(WoofSectionOutputItem.class.getSimpleName(), outputs);
		return container;
	}

	@Override
	public IdeLabeller label() {
		return new IdeLabeller((model) -> model.getWoofSectionName(), WoofSectionEvent.CHANGE_WOOF_SECTION_NAME);
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 100.0%, green, mediumseagreen)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "honeydew"));
	}

	@Override
	protected WoofSectionItem item(WoofSectionModel model) {
		WoofSectionItem item = new WoofSectionItem();
		if (model != null) {
			item.name = model.getWoofSectionName();
			item.sourceClassName = model.getSectionSourceClassName();
			item.location = model.getSectionLocation();
			item.properties = this.translateToPropertyList(model.getProperties(), (p) -> p.getName(),
					(p) -> p.getValue());
			if (ClassSectionSource.class.getName().equals(item.sourceClassName)) {
				item.choice = CHOICE_CLASS;
			}
		}
		return item;
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(new WoofSectionInputItem()));
		childGroups.add(new IdeChildrenGroup(new WoofSectionOutputItem()));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Section");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			ChoiceBuilder<WoofSectionItem> choices = builder.choices("").init((item) -> item.choice)
					.validate(ValueValidator.notNull("Must select")).setValue((item, value) -> {
						if (value == CHOICE_CLASS) {
							item.sourceClassName = ClassSectionSource.class.getName();
						}
					});

			// Choice: class
			ConfigurationBuilder<WoofSectionItem> classBuilder = choices.choice("Class");
			classBuilder.clazz("Class").init((item) -> item.location)
					.validate(ValueValidator.notEmptyString("Must specify class"))
					.setValue((item, value) -> item.location = value);

			// Choice: source
			ConfigurationBuilder<WoofSectionItem> sourceBuilder = choices.choice("Source");
			sourceBuilder.clazz("Source").init((item) -> item.sourceClassName).superType(SectionSource.class)
					.validate(ValueValidator.notEmptyString("Must specify source"))
					.setValue((item, value) -> item.sourceClassName = value);
			sourceBuilder.text("Location").init((item) -> item.location)
					.setValue((item, value) -> item.location = value);
			sourceBuilder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);

			// Validate (ensure loads type)
			builder.validate((ctx) -> {
				OfficeFloorOsgiBridge osgiBridge = this.getConfigurableContext().getOsgiBridge();

				// Validate the type
				WoofSectionItem item = ctx.getModel();

				// Attempt to load the type
				item.sectionType = loadSectionType(item, osgiBridge);

				// Load the mappings
				item.inputNameMapping = this.translateToNameMappings(item.sectionType.getSectionInputTypes(),
						(input) -> input.getSectionInputName());
				item.outputNameMapping = this.translateToNameMappings(item.sectionType.getSectionOutputTypes(),
						(output) -> output.getSectionOutputName());
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(context.getOperations().addSection(item.name, item.sourceClassName, item.location,
						item.properties, item.sectionType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(context.getOperations().refactorSection(context.getModel(), item.name,
						item.sourceClassName, item.location, item.properties, item.sectionType, item.inputNameMapping,
						item.outputNameMapping));
			});

		}).delete((context) -> {
			context.execute(context.getOperations().removeSection(context.getModel()));
		});
	}

}