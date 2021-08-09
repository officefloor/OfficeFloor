/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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

package net.officefloor.gef.ide.editor;

import java.util.LinkedList;
import java.util.List;

import javafx.scene.layout.Pane;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.CloseListener;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder.MessageOnlyApplyException;
import net.officefloor.gef.configurer.Configurer;
import net.officefloor.gef.editor.AdaptedErrorHandler.MessageOnlyException;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.ChangeExecutor;
import net.officefloor.gef.editor.DefaultImages;
import net.officefloor.gef.ide.ConfigurableItem;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Abstract {@link ConfigurableItem}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConfigurableItem<R extends Model, RE extends Enum<RE>, O, M extends Model, E extends Enum<E>, I>
		extends AbstractItem<R, O, R, RE, M, E> implements ConfigurableItem<I> {

	/**
	 * Creates an item from the {@link Model}.
	 * 
	 * @param model {@link Model}. May be <code>null</code> if create a
	 *              {@link Model}.
	 * @return Item.
	 */
	public abstract I item(M model);

	/**
	 * IDE configurer.
	 */
	public class IdeConfigurer {

		/**
		 * Add {@link ItemConfigurer} instances.
		 */
		private final List<ItemConfigurer<O, M, I>> add = new LinkedList<>();

		/**
		 * Refactor {@link ItemConfigurer} instances.
		 */
		private final List<ItemConfigurer<O, M, I>> refactor = new LinkedList<>();

		/**
		 * Add item immediately (as requires no configuration).
		 */
		private ItemActioner<O, M> addImmediately = null;

		/**
		 * Delete item.
		 */
		private ItemActioner<O, M> delete = null;

		/**
		 * Ensure not add immediately.
		 */
		private void ensureNotAddImmediately() {
			if (this.addImmediately != null) {
				throw new IllegalStateException("Configured to add immediately, so can not configure add");
			}
		}

		/**
		 * Ensure not add configured.
		 */
		private void ensureNotAddConfigured() {
			if (this.add.size() > 0) {
				throw new IllegalStateException(
						"Configured to add with configuration, so can not configure add immediately");
			}
		}

		/**
		 * Convenience method to provide add and refactor configuration.
		 * 
		 * @param configuration {@link ItemConfigurer}.
		 * @return <code>this</code>.
		 */
		public IdeConfigurer addAndRefactor(ItemConfigurer<O, M, I> configuration) {
			this.ensureNotAddImmediately();
			this.add.add(configuration);
			this.refactor.add(configuration);
			return this;
		}

		/**
		 * Provides add configuration.
		 * 
		 * @param configuration {@link ItemConfigurer}.
		 * @return <code>this</code>.
		 */
		public IdeConfigurer add(ItemConfigurer<O, M, I> configuration) {
			this.ensureNotAddImmediately();
			this.add.add(configuration);
			return this;
		}

		/**
		 * Provides refactor configuration.
		 * 
		 * @param configuration {@link ItemConfigurer}.
		 * @return <code>this</code>.
		 */
		public IdeConfigurer refactor(ItemConfigurer<O, M, I> configuration) {
			this.refactor.add(configuration);
			return this;
		}

		/**
		 * Provides add immediate (without configuration).
		 * 
		 * @param add {@link ItemActioner} to add item.
		 * @return <code>this</code>.
		 */
		public IdeConfigurer add(ItemActioner<O, M> add) {
			this.ensureNotAddConfigured();
			this.addImmediately = add;
			return this;
		}

		/**
		 * Provides delete configuration.
		 * 
		 * @param deletion {@link ItemActioner} to delete item.
		 * @return <code>this</code>.
		 */
		public IdeConfigurer delete(ItemActioner<O, M> deletion) {
			this.delete = deletion;
			return this;
		}
	}

	/**
	 * Configuration for the IdeConfigurer.
	 */
	public static class IdeConfiguration<O, M extends Model, I> {

		/**
		 * Add configuration.
		 */
		public final ItemConfigurer<O, M, I>[] add;

		/**
		 * Refactor configuration.
		 */
		public final ItemConfigurer<O, M, I>[] refactor;

		/**
		 * Add not requiring configuration.
		 */
		public final ItemActioner<O, M> addImmediately;

		/**
		 * Delete.
		 */
		public final ItemActioner<O, M> delete;

		/**
		 * Instantiate.
		 * 
		 * @param configurer {@link AbstractConfigurableItem.IdeConfigurer}.
		 */
		@SuppressWarnings("unchecked")
		public IdeConfiguration(AbstractConfigurableItem<?, ?, O, M, ?, I>.IdeConfigurer configurer) {
			this.add = configurer.add.toArray(new ItemConfigurer[configurer.add.size()]);
			this.refactor = configurer.refactor.toArray(new ItemConfigurer[configurer.refactor.size()]);
			this.addImmediately = configurer.addImmediately;
			this.delete = configurer.delete;
		}
	}

	/**
	 * Extracts the {@link IdeConfiguration}.
	 * 
	 * @param <O>        Operations type.
	 * @param <M>        Item {@link Model} type.
	 * @param <I>        Item type.
	 * @param configurer {@link AbstractConfigurableItem.IdeConfigurer}.
	 */
	public static <O, M extends Model, I> IdeConfiguration<O, M, I> extractIdeConfiguration(
			AbstractConfigurableItem<?, ?, O, M, ?, I>.IdeConfigurer configurer) {
		return new IdeConfiguration<>(configurer);
	}

	/**
	 * Configures an item.
	 */
	@FunctionalInterface
	public static interface ItemConfigurer<O, M, I> {

		/**
		 * Builds the item configuration.
		 * 
		 * @param builder {@link ConfigurationBuilder}.
		 * @param context {@link ConfigurableModelContext}.
		 */
		void configure(ConfigurationBuilder<I> builder, ConfigurableModelContext<O, M> context);
	}

	/**
	 * Immediate action for an item.
	 */
	@FunctionalInterface
	public static interface ItemActioner<O, M> {

		/**
		 * Undertakes action for the item.
		 * 
		 * @param context {@link ConfigurableModelContext}.
		 * @throws Throwable If failure in actioning.
		 */
		void action(ConfigurableModelContext<O, M> context) throws Throwable;
	}

	/**
	 * Context for {@link Model} of {@link ConfigurableItem}.
	 */
	public static interface ConfigurableModelContext<O, M> {

		/**
		 * Obtains the operations.
		 * 
		 * @return Operations.
		 */
		O getOperations();

		/**
		 * Obtains the {@link Model}.
		 * 
		 * @return {@link Model}. Will be <code>null</code> if create the {@link Model}.
		 */
		M getModel();

		/**
		 * Executes the {@link Change}.
		 * 
		 * @param change {@link Change}.
		 * @throws Throwable If unable to execute the {@link Change}.
		 */
		void execute(Change<M> change) throws Throwable;
	}

	/**
	 * Obtains the item configuration.
	 * 
	 * @return {@link IdeConfigurer} for the item configuration.
	 */
	public abstract IdeConfigurer configure();

	/**
	 * Further adapt the {@link AdaptedParentBuilder}.
	 * 
	 * @param builder {@link AdaptedParentBuilder}.
	 */
	protected void furtherAdapt(AdaptedParentBuilder<R, O, M, E> builder) {
		super.furtherAdapt(builder);
	}

	/**
	 * Creates the {@link AdaptedParentBuilder}.
	 * 
	 * @return {@link AdaptedParentBuilder}.
	 */
	public final AdaptedParentBuilder<R, O, M, E> createAdaptedParent() {

		// Create the prototype
		M prototype = this.prototype();

		// Configure the parent
		IdeExtractor extractor = this.extract();
		AdaptedParentBuilder<R, O, M, E> parent = this.getConfigurableContext().getRootBuilder().parent(prototype,
				(root) -> extractor.extract(root), (model, ctx) -> this.visual(model, ctx),
				extractor.getExtractChangeEvents());

		// Capture the builder
		this.builder = parent;

		// Determine if configured with label
		IdeLabeller labeller = this.label();
		if (labeller != null) {
			parent.label((model) -> labeller.getLabel(model), labeller.getLabelChangeEvents());
		}

		// Obtain the item configuration
		IdeConfigurer ideConfigurer = this.configure();

		// Determine if can create
		if (ideConfigurer != null) {

			// Determine if add via configuration
			if (ideConfigurer.add.size() > 0) {
				parent.create((ctx) -> {

					// Obtain details for dialog
					EnvironmentBridge bridge = this.getConfigurableContext().getEnvironmentBridge();

					// Obtain details for executing change
					O operations = ctx.getOperations();
					ChangeExecutor executor = ctx.getChangeExecutor();

					// Create the overlay to add external flow
					ctx.overlay((visual) -> {

						// Prepare the parent
						Pane overlay = visual.getOverlayParent();
						overlay.setPrefWidth(500);
						overlay.setPrefHeight(400);
						overlay.applyCss();

						// Create the configurer
						Configurer<I> configurer = new Configurer<>(bridge);
						ConfigurationBuilder<I> builder = configurer;

						// Create the configurable context
						ConfigurableModelContext<O, M> modelContext = new ConfigurableModelContext<O, M>() {

							@Override
							public O getOperations() {
								return operations;
							}

							@Override
							public M getModel() {
								return null; // no model on create
							}

							@Override
							public void execute(Change<M> change) throws Throwable {

								// Position the added model
								ctx.position(change.getTarget());

								// Execute the change
								executeChange(executor, change);
							}
						};

						// Build the configuration
						for (ItemConfigurer<O, M, I> itemConfigurer : ideConfigurer.add) {
							itemConfigurer.configure(builder, modelContext);
						}

						// Configure close
						builder.close(new CloseListener() {

							@Override
							public void cancelled() {
								visual.close();
							}

							@Override
							public void applied() {
								visual.close();
							}
						});

						// Show the configuration
						I item = this.item(null);
						configurer.loadConfiguration(item, overlay);
					});
				});
			}

			// Determine if add immediately
			if (ideConfigurer.addImmediately != null) {
				parent.create((ctx) -> ideConfigurer.addImmediately.action((new ConfigurableModelContext<O, M>() {

					@Override
					public O getOperations() {
						return ctx.getOperations();
					}

					@Override
					public M getModel() {
						return ctx.getModel();
					}

					@Override
					public void execute(Change<M> change) throws Throwable {

						// Position the added model
						ctx.position(change.getTarget());

						try {
							executeChange(ctx.getChangeExecutor(), change);
						} catch (MessageOnlyApplyException ex) {
							// Translate to message only exception
							throw new MessageOnlyException(ex.getMessage());
						}
					}
				})));
			}
		}

		// Determine if can refactor
		if ((ideConfigurer != null) && (ideConfigurer.refactor.size() > 0)) {
			parent.action((ctx) -> {

				// Obtain details for dialog
				EnvironmentBridge bridge = this.getConfigurableContext().getEnvironmentBridge();

				// Obtain details for executing change
				O operations = ctx.getOperations();
				ChangeExecutor executor = ctx.getChangeExecutor();

				// Create the overlay to add external flow
				ctx.overlay((visual) -> {

					// Prepare the parent
					Pane overlay = visual.getOverlayParent();
					overlay.setMinHeight(200);
					overlay.applyCss();

					// Create the configurer
					Configurer<I> configurer = new Configurer<>(bridge);
					ConfigurationBuilder<I> builder = configurer;

					// Create the configurable context
					ConfigurableModelContext<O, M> modelContext = new ConfigurableModelContext<O, M>() {

						@Override
						public O getOperations() {
							return operations;
						}

						@Override
						public M getModel() {
							return ctx.getModel();
						}

						@Override
						public void execute(Change<M> change) throws Throwable {
							executeChange(executor, change);
						}
					};

					// Build the configuration
					for (ItemConfigurer<O, M, I> itemConfigurer : ideConfigurer.refactor) {
						itemConfigurer.configure(builder, modelContext);
					}

					// Configure close
					builder.close(new CloseListener() {

						@Override
						public void cancelled() {
							visual.close();
						}

						@Override
						public void applied() {
							visual.close();
						}
					});

					// Show the configuration
					I item = this.item(ctx.getModel());
					configurer.loadConfiguration(item, overlay);
				});

			}, DefaultImages.EDIT);
		}

		// Further adapt (so delete action is last action)
		this.furtherAdapt(parent);

		// Determine if can delete
		if ((ideConfigurer != null) && (ideConfigurer.delete != null)) {
			parent.action((ctx) -> ideConfigurer.delete.action((new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return ctx.getOperations();
				}

				@Override
				public M getModel() {
					return ctx.getModel();
				}

				@Override
				public void execute(Change<M> change) throws Throwable {
					try {
						executeChange(ctx.getChangeExecutor(), change);
					} catch (MessageOnlyApplyException ex) {
						// Translate to message only exception
						throw new MessageOnlyException(ex.getMessage());
					}
				}
			})), DefaultImages.DELETE);
		}

		// Return the parent
		return parent;
	}

	/**
	 * Executes the {@link Change}.
	 * 
	 * @param changeExecutor {@link ChangeExecutor}.
	 * @param change         {@link Change} to be executed.
	 * @throws MessageOnlyApplyException If not able to execute the {@link Change}.
	 * @throws Throwable                 If failure in {@link Conflict}.
	 */
	private static void executeChange(ChangeExecutor changeExecutor, Change<?> change)
			throws MessageOnlyApplyException, Throwable {

		// Determine if able to apply
		if (!change.canApply()) {

			// Unable to apply, so throw appropriate exception
			StringBuilder message = new StringBuilder();
			boolean isFirst = true;
			for (Conflict conflict : change.getConflicts()) {

				// Determine if cause
				Throwable cause = conflict.getConflictCause();
				if (cause != null) {
					throw cause;
				}

				// Construct the message
				if (!isFirst) {
					message.append(System.lineSeparator());
				}
				isFirst = false;
				String description = conflict.getConflictDescription();
				if ((description == null) || (description.trim().length() == 0)) {
					description = "Conflict in making change";
				}
				message.append(description);
			}
			throw new MessageOnlyApplyException(message.toString());
		}

		// Apply the change
		changeExecutor.execute(change);
	}

}
