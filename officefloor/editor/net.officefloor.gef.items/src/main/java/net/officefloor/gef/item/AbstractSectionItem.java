/*-
 * #%L
 * [bundle] Abstract IDE Items
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

package net.officefloor.gef.item;

import java.util.List;
import java.util.Map;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.activity.ActivitySectionSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.gef.ide.editor.AbstractItem;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Configuration for the abstract {@link SubSection} / {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSectionItem<R extends Model, RE extends Enum<RE>, O, M extends Model, E extends Enum<E>, I extends AbstractSectionItem<R, RE, O, M, E, I>>
		extends AbstractConfigurableItem<R, RE, O, M, E, I> {

	/**
	 * Loads the {@link SectionType} for the {@link AbstractSectionItem}.
	 * 
	 * @param item      {@link AbstractSectionItem}.
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link SectionType}.
	 * @throws Exception If fails to load the {@link SectionType}.
	 */
	public static SectionType loadSectionType(AbstractSectionItem<?, ?, ?, ?, ?, ?> item, EnvironmentBridge envBridge)
			throws Exception {
		SectionLoader loader = envBridge.getOfficeFloorCompiler().getSectionLoader();
		Class<? extends SectionSource> sourceClass = envBridge.loadClass(item.sourceClassName, SectionSource.class);
		return loader.loadSectionType(sourceClass, item.location, item.properties);
	}

	/**
	 * Choice {@link ActivitySectionSource}.
	 */
	private static final int CHOICE_ACTIVITY = 0;

	/**
	 * Choice {@link Class}.
	 */
	private static final int CHOICE_CLASS = 1;

	/**
	 * Choice {@link SectionSource}.
	 */
	private static final int CHOICE_SECTION = 2;

	/**
	 * Name.
	 */
	protected String name;

	/**
	 * Choice.
	 */
	protected Integer choice = null;

	/**
	 * {@link SectionSource} {@link Class} name.
	 */
	protected String sourceClassName;

	/**
	 * Location.
	 */
	protected String location;

	/**
	 * {@link PropertyList}.
	 */
	protected PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link SectionType}.
	 */
	protected SectionType sectionType;

	/**
	 * {@link SectionInputType} name mapping.
	 */
	protected Map<String, String> inputNameMapping;

	/**
	 * {@link SectionOutputType} name mapping.
	 */
	protected Map<String, String> outputNameMapping;

	/**
	 * Creates the {@link AbstractSectionItem} implementation.
	 * 
	 * @return {@link AbstractSectionItem} implementation.
	 */
	protected abstract I createItem();

	/**
	 * Obtains the name from the {@link Model}.
	 * 
	 * @param model {@link Model}.
	 * @return Name.
	 */
	protected abstract String getSectionName(M model);

	/**
	 * Obtains the {@link SectionSource} {@link Class} name from {@link Model}.
	 * 
	 * @param model {@link Model}.
	 * @return {@link SectionSource} {@link Class} name.
	 */
	protected abstract String getSectionSourceClassName(M model);

	/**
	 * Obtains the location from the {@link Model}.
	 * 
	 * @param model {@link Model}.
	 * @return Location.
	 */
	protected abstract String getSectionLocation(M model);

	/**
	 * Obtains the {@link PropertyList} from the {@link Model}.
	 * 
	 * @param model {@link Model}.
	 * @return {@link PropertyList} from the {@link Model}.
	 */
	protected abstract PropertyList getSectionProperties(M model);

	/**
	 * Creates the {@link SectionInput} {@link AbstractItem} implementation.
	 * 
	 * @return {@link SectionInput} {@link AbstractItem} implementation.
	 */
	protected abstract AbstractItem<R, O, M, E, ?, ?> createInputItem();

	/**
	 * Creates the {@link SectionOutput} {@link AbstractItem} implementation.
	 * 
	 * @return {@link SectionOutput} {@link AbstractItem} implementation.
	 */
	protected abstract AbstractItem<R, O, M, E, ?, ?> createOutputItem();

	/**
	 * Creates {@link Change} to add {@link SubSection} / {@link OfficeSection}.
	 * 
	 * @param operations      Operations.
	 * @param name            Name.
	 * @param sourceClassName {@link SectionSource} {@link Class} name.
	 * @param location        Location.
	 * @param properties      {@link PropertyList}.
	 * @param sectionType     {@link SectionType}.
	 * @return {@link Change} to add {@link SubSection} / {@link OfficeSection}.
	 */
	protected abstract Change<M> addSection(O operations, String name, String sourceClassName, String location,
			PropertyList properties, SectionType sectionType);

	/**
	 * Creates {@link Change} to refactor {@link SubSection} /
	 * {@link OfficeSection}.
	 * 
	 * @param operations        Operations.
	 * @param model             {@link Model} to refactor.
	 * @param name              Name.
	 * @param sourceClassName   {@link SectionSource} {@link Class} name.
	 * @param location          Location.
	 * @param properties        {@link PropertyList}.
	 * @param sectionType       {@link SectionType}.
	 * @param inputNameMapping  {@link SectionInput} name mapping.
	 * @param outputNameMapping {@link SectionOutput} name mapping.
	 * @return {@link Change} to refactor {@link SubSection} /
	 *         {@link OfficeSection}.
	 */
	protected abstract Change<M> refactorSection(O operations, M model, String name, String sourceClassName,
			String location, PropertyList properties, SectionType sectionType, Map<String, String> inputNameMapping,
			Map<String, String> outputNameMapping);

	/**
	 * Creates {@link Change} to remove {@link SubSection} / {@link OfficeSection}.
	 * 
	 * @param operations Operations.
	 * @param model      {@link Model} to remove.
	 * @return {@link Change} to remove {@link SubSection} / {@link OfficeSection}.
	 */
	protected abstract Change<M> removeSection(O operations, M model);

	/*
	 * =================== AbstractConfigurableItem ====================
	 */

	@Override
	public Pane visual(M model, AdaptedChildVisualFactoryContext<M> context) {
		VBox container = new VBox();
		context.label(container);
		HBox children = context.addNode(container, new HBox());
		VBox inputs = context.addNode(children, new VBox());
		context.childGroup(this.createInputItem().getClass().getSimpleName(), inputs);
		VBox outputs = context.addNode(children, new VBox());
		context.childGroup(this.createOutputItem().getClass().getSimpleName(), outputs);
		return container;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(
				new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 100.0%, green, mediumseagreen)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "honeydew"));
	}

	@Override
	public I item(M model) {
		I item = this.createItem();
		if (model != null) {
			item.name = this.getSectionName(model);
			item.sourceClassName = this.getSectionSourceClassName(model);
			item.location = this.getSectionLocation(model);
			item.properties = this.getSectionProperties(model);
			if (ActivitySectionSource.class.getName().equals(item.sourceClassName)) {
				item.choice = CHOICE_ACTIVITY;
			} else if (ClassSectionSource.class.getName().equals(item.sourceClassName)) {
				item.choice = CHOICE_CLASS;
			} else {
				item.choice = CHOICE_SECTION;
			}
		}
		return item;
	}

	@Override
	protected void children(List<IdeChildrenGroup> childGroups) {
		childGroups.add(new IdeChildrenGroup(this.createInputItem()));
		childGroups.add(new IdeChildrenGroup(this.createOutputItem()));
	}

	@Override
	public IdeConfigurer configure() {
		return new IdeConfigurer().addAndRefactor((builder, context) -> {
			builder.title("Section");
			builder.text("Name").init((item) -> item.name).validate(ValueValidator.notEmptyString("Must provide name"))
					.setValue((item, value) -> item.name = value);
			ChoiceBuilder<I> choices = builder.choices("").init((item) -> item.choice)
					.validate(ValueValidator.notNull("Must select")).setValue((item, value) -> {
						if (value.equals(CHOICE_ACTIVITY)) {
							item.sourceClassName = ActivitySectionSource.class.getName();
						} else if (value.equals(CHOICE_CLASS)) {
							item.sourceClassName = ClassSectionSource.class.getName();
						}
					});

			// Choice: activity
			ConfigurationBuilder<I> activityBuilder = choices.choice("Activity");
			activityBuilder.resource("Activity").init((item) -> item.location).validate((ctx) -> {
				// Ensure Activity is available
				EnvironmentBridge envBridge = this.getConfigurableContext().getEnvironmentBridge();
				Class<?> activitySectionSourceClass = null;
				try {
					activitySectionSourceClass = envBridge.loadClass(ActivitySectionSource.class.getName(), null);
				} catch (ClassNotFoundException ex) {
					activitySectionSourceClass = null;
				}
				if (activitySectionSourceClass == null) {
					ctx.setError("Please add Activity to project's class path");
				}
			}).validate(ValueValidator.notEmptyString("Must specify activity"))
					.setValue((item, value) -> item.location = value);

			// Choice: class
			ConfigurationBuilder<I> classBuilder = choices.choice("Class");
			classBuilder.clazz("Class").init((item) -> item.location)
					.validate(ValueValidator.notEmptyString("Must specify class"))
					.setValue((item, value) -> item.location = value);

			// Choice: source
			ConfigurationBuilder<I> sourceBuilder = choices.choice("Source");
			sourceBuilder.clazz("Source").init((item) -> item.sourceClassName).superType(SectionSource.class)
					.validate(ValueValidator.notEmptyString("Must specify source"))
					.setValue((item, value) -> item.sourceClassName = value);
			sourceBuilder.text("Location").init((item) -> item.location)
					.setValue((item, value) -> item.location = value);
			sourceBuilder.properties("Properties").init((item) -> item.properties)
					.setValue((item, value) -> item.properties = value);

			// Validate (ensure loads type)
			builder.validate((ctx) -> {
				EnvironmentBridge envBridge = this.getConfigurableContext().getEnvironmentBridge();

				// Validate the type
				I item = ctx.getModel();

				// Attempt to load the type
				item.sectionType = loadSectionType(item, envBridge);

				// Load the mappings
				item.inputNameMapping = this.translateToNameMappings(item.sectionType.getSectionInputTypes(),
						(input) -> input.getSectionInputName());
				item.outputNameMapping = this.translateToNameMappings(item.sectionType.getSectionOutputTypes(),
						(output) -> output.getSectionOutputName());
			});

		}).add((builder, context) -> {
			builder.apply("Add", (item) -> {
				context.execute(this.addSection(context.getOperations(), item.name, item.sourceClassName, item.location,
						item.properties, item.sectionType));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(this.refactorSection(context.getOperations(), context.getModel(), item.name,
						item.sourceClassName, item.location, item.properties, item.sectionType, item.inputNameMapping,
						item.outputNameMapping));
			});

		}).delete((context) -> {
			context.execute(this.removeSection(context.getOperations(), context.getModel()));
		});
	}

}
