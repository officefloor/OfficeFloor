/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal.inputs;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ClassBuilder;
import net.officefloor.gef.configurer.FlagBuilder;
import net.officefloor.gef.configurer.ListBuilder;
import net.officefloor.gef.configurer.MappingBuilder;
import net.officefloor.gef.configurer.MultipleBuilder;
import net.officefloor.gef.configurer.OptionalBuilder;
import net.officefloor.gef.configurer.PropertiesBuilder;
import net.officefloor.gef.configurer.ResourceBuilder;
import net.officefloor.gef.configurer.SelectBuilder;
import net.officefloor.gef.configurer.TextBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.AbstractConfigurationBuilder;
import net.officefloor.gef.configurer.internal.OptionalValueInput;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;
import net.officefloor.gef.configurer.internal.ValueRendererFactory;

/**
 * {@link OptionalBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalBuilderImpl<M> extends AbstractBuilder<M, M, ValueInput, OptionalBuilder<M>>
		implements OptionalBuilder<M> {

	/**
	 * {@link ValueRendererFactory} for not showing optional.
	 */
	@SuppressWarnings("unchecked")
	private final ValueRendererFactory<M, ? extends ValueInput>[] NO_OPTIONAL = new ValueRendererFactory[0];

	/**
	 * Indicates whether to show optional.
	 */
	private final Predicate<M> isShow;

	/**
	 * Delegate {@link AbstractConfigurationBuilder}.
	 */
	private final AbstractConfigurationBuilder<M> delegate;

	/**
	 * Instantiate.
	 * 
	 * @param isShow    Indicates whether to show optional.
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public OptionalBuilderImpl(Predicate<M> isShow, EnvironmentBridge envBridge) {
		super(null);
		this.isShow = isShow;

		// Create the delegate configuration builder
		this.delegate = new AbstractConfigurationBuilder<M>(envBridge) {
		};
	}

	/*
	 * =============== OptionalBuilder =======================
	 */

	@Override
	protected OptionalValueInput<M> createInput(ValueInputContext<M, M> context) {

		// Obtain the model
		final M model = context.getModel();

		// Obtain the value renderer factories
		final ValueRendererFactory<M, ? extends ValueInput>[] valueRendererFactories = this.delegate
				.getValueRendererFactories();

		// Return the input
		return new OptionalValueInput<M>() {

			/**
			 * Loads optional content.
			 */
			private Consumer<ValueRendererFactory<M, ? extends ValueInput>[]> loader;

			/**
			 * Initially not shown.
			 */
			private boolean isShown = false;

			@Override
			public Node getNode() {
				throw new IllegalStateException("Should not load node for " + OptionalValueInput.class.getSimpleName());
			}

			@Override
			public void setOptionalLoader(Consumer<ValueRendererFactory<M, ? extends ValueInput>[]> loader) {
				this.loader = loader;

				// Load initial state
				this.loadOptional(true);
			}

			@Override
			public void reload() {
				this.loadOptional(false);
			}

			private void loadOptional(boolean isForce) {

				// Determine if change to show
				boolean isShow = OptionalBuilderImpl.this.isShow.test(model);
				if ((!isForce) && (isShow == this.isShown)) {
					return; // no change
				}

				// Determine if show optional content
				ValueRendererFactory<M, ? extends ValueInput>[] optional = isShow ? valueRendererFactories
						: NO_OPTIONAL;

				// Update the content
				this.loader.accept(optional);

				// Update shown state
				this.isShown = isShow;
			}
		};
	}

	/*
	 * =============== InputBuilder =======================
	 */

	@Override
	public ChoiceBuilder<M> choices(String label) {
		return this.delegate.choices(label);
	}

	@Override
	public <I> ListBuilder<M, I> list(String label, Class<I> itemType) {
		return this.delegate.list(label, itemType);
	}

	@Override
	public <I> SelectBuilder<M, I> select(String label, Function<M, ObservableList<I>> getItems) {
		return this.delegate.select(label, getItems);
	}

	@Override
	public OptionalBuilder<M> optional(Predicate<M> isShow) {
		return this.delegate.optional(isShow);
	}

	@Override
	public <I> MultipleBuilder<M, I> multiple(String label, Class<I> itemType) {
		return this.delegate.multiple(label, itemType);
	}

	@Override
	public PropertiesBuilder<M> properties(String label) {
		return this.delegate.properties(label);
	}

	@Override
	public MappingBuilder<M> map(String label, Function<M, ObservableList<String>> getSources,
			Function<M, ObservableList<String>> getTargets) {
		return this.delegate.map(label, getSources, getTargets);
	}

	@Override
	public ClassBuilder<M> clazz(String label) {
		return this.delegate.clazz(label);
	}

	@Override
	public ResourceBuilder<M> resource(String label) {
		return this.delegate.resource(label);
	}

	@Override
	public TextBuilder<M> text(String label) {
		return this.delegate.text(label);
	}

	@Override
	public FlagBuilder<M> flag(String label) {
		return this.delegate.flag(label);
	}

}
