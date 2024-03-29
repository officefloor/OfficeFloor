/*-
 * #%L
 * [bundle] Abstract IDE Items
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

package net.officefloor.gef.item;

import java.util.List;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.DefaultConnectors;
import net.officefloor.gef.ide.editor.AbstractConfigurableItem;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Configuration for abstract {@link Exception} item.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractExceptionItem<R extends Model, RE extends Enum<RE>, O, M extends Model, E extends Enum<E>, I extends AbstractExceptionItem<R, RE, O, M, E, I>>
		extends AbstractConfigurableItem<R, RE, O, M, E, I> {

	/**
	 * {@link Exception} {@link Class}.
	 */
	protected String exceptionClassName;

	/**
	 * Creates the {@link AbstractExceptionItem} implementation.
	 * 
	 * @return {@link AbstractExceptionItem} implementation.
	 */
	protected abstract I createItem();

	/**
	 * Obtains the {@link Exception} {@link Class} name.
	 * 
	 * @param model {@link Model}.
	 * @return {@link Exception} {@link Class} name.
	 */
	protected abstract String getExceptionClassName(M model);

	/**
	 * Obtains the input {@link ConnectionModel} {@link Class} instances.
	 * 
	 * @return Input {@link ConnectionModel} {@link Class} instances.
	 */
	protected abstract Class<? extends ConnectionModel>[] getInputConnectionClasses();

	/**
	 * Creates a {@link Change} to add an {@link Exception}.
	 * 
	 * @param operations         Operations.
	 * @param exceptionClassName {@link Exception} {@link Class} name.
	 * @return {@link Change} to add an {@link Exception}.
	 */
	protected abstract Change<M> addException(O operations, String exceptionClassName);

	/**
	 * Creates a {@link Change} to refactor the {@link Exception}.
	 * 
	 * @param operations         Operations.
	 * @param model              {@link Model} to refactor.
	 * @param exceptionClassName {@link Exception} {@link Class} name.
	 * @return {@link Change} to refactor the {@link Exception}.
	 */
	protected abstract Change<M> refactorException(O operations, M model, String exceptionClassName);

	/**
	 * Creates a {@link Change} to remove the {@link Exception}.
	 * 
	 * @param operations Operations.
	 * @param model      {@link Model} to remove.
	 * @return {@link Change} to remove the {@link Exception}.
	 */
	protected abstract Change<M> removeException(O operations, M model);

	/*
	 * ================ AbstractConfigurableItem =====================
	 */

	@Override
	public Pane visual(M model, AdaptedChildVisualFactoryContext<M> context) {
		HBox container = new HBox();
		context.label(container);
		context.addNode(container,
				context.connector(DefaultConnectors.FLOW, this.getInputConnectionClasses()).getNode());
		return container;
	}

	@Override
	public I item(M model) {
		I item = this.createItem();
		if (model != null) {
			item.exceptionClassName = this.getExceptionClassName(model);
		}
		return item;
	}

	@Override
	protected void loadStyles(List<IdeStyle> styles) {
		styles.add(new IdeStyle().rule("-fx-background-color", "radial-gradient(radius 100.0%, tomato, darkorange)"));
		styles.add(new IdeStyle(".${model} .label").rule("-fx-text-fill", "moccasin"));
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
				context.execute(this.addException(context.getOperations(), item.exceptionClassName));
			});

		}).refactor((builder, context) -> {
			builder.apply("Refactor", (item) -> {
				context.execute(
						this.refactorException(context.getOperations(), context.getModel(), item.exceptionClassName));
			});

		}).delete((context) -> {
			context.execute(this.removeException(context.getOperations(), context.getModel()));

		});
	}

}
