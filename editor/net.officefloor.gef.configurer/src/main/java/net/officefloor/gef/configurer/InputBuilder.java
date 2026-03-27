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

package net.officefloor.gef.configurer;

import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import net.officefloor.compile.properties.PropertyList;

/**
 * Builder of the inputs.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputBuilder<M> extends ItemBuilder<M> {

	/**
	 * Builds choices in configuration.
	 * 
	 * @param label Label for the choices.
	 * @return {@link ChoiceBuilder}.
	 */
	ChoiceBuilder<M> choices(String label);

	/**
	 * Configures creating a list of items.
	 * 
	 * @param <I>      Item type.
	 * @param label    Label for the items.
	 * @param itemType Item type.
	 * @return {@link ListBuilder}.
	 */
	<I> ListBuilder<M, I> list(String label, Class<I> itemType);

	/**
	 * Configures selecting from a list of items.
	 * 
	 * @param <I>      Item type.
	 * @param label    Label for the selection.
	 * @param getItems Function to extract the items.
	 * @return {@link SelectBuilder}.
	 */
	<I> SelectBuilder<M, I> select(String label, Function<M, ObservableList<I>> getItems);

	/**
	 * Configures optional configuration.
	 * 
	 * @param isShow {@link Predicate} on whether to show the optional
	 *               configuration.
	 * @return {@link OptionalBuilder}.
	 */
	OptionalBuilder<M> optional(Predicate<M> isShow);

	/**
	 * Configures multiple items.
	 *
	 * @param <I>      Item type.
	 * @param label    Label for the items.
	 * @param itemType Item type.
	 * @return {@link MultipleBuilder}.
	 */
	<I> MultipleBuilder<M, I> multiple(String label, Class<I> itemType);

	/**
	 * Configures {@link PropertyList}.
	 * 
	 * @param label Label for the {@link Properties}.
	 * @return {@link PropertiesBuilder}.
	 */
	PropertiesBuilder<M> properties(String label);

	/**
	 * Configures a mapping of name to name.
	 * 
	 * @param label      Label for the mapping.
	 * @param getSources {@link Function} to extract the sources.
	 * @param getTargets {@link Function} to extract the targets.
	 * @return {@link MappingBuilder}.
	 */
	MappingBuilder<M> map(String label, Function<M, ObservableList<String>> getSources,
			Function<M, ObservableList<String>> getTargets);

	/**
	 * Adds a {@link Class} property to be configured.
	 * 
	 * @param label Label.
	 * @return {@link ClassBuilder}.
	 */
	ClassBuilder<M> clazz(String label);

	/**
	 * Adds a resource property to be configured.
	 * 
	 * @param label Label.
	 * @return {@link ResourceBuilder}.
	 */
	ResourceBuilder<M> resource(String label);

}
