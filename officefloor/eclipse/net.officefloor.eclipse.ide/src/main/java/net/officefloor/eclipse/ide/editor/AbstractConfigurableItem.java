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
package net.officefloor.eclipse.ide.editor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import javafx.scene.layout.Pane;
import net.officefloor.eclipse.configurer.CloseListener;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder.MessageOnlyApplyException;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.editor.AdaptedErrorHandler.MessageOnlyException;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.ChangeListener;
import net.officefloor.eclipse.editor.DefaultImages;
import net.officefloor.eclipse.ide.ConfigurableItem;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
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
	protected abstract I item(M model);

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
					OfficeFloorOsgiBridge bridge = this.getConfigurableContext().getOsgiBridge();
					Shell shell = this.getConfigurableContext().getParentShell();

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
						Configurer<I> configurer = new Configurer<>(bridge, shell);
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
				OfficeFloorOsgiBridge bridge = this.getConfigurableContext().getOsgiBridge();
				Shell shell = this.getConfigurableContext().getParentShell();

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
					Configurer<I> configurer = new Configurer<>(bridge, shell);
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

	/*
	 * =============== AbstractConfigurerRunnable =============
	 */

	/**
	 * Invoke to run in main method for external testing.
	 * 
	 * @param rootModel         Root {@link Model}.
	 * @param ideEditorClass    {@link AbstractIdeEditor} {@link Class} for this
	 *                          {@link AbstractConfigurableItem}.
	 * @param decoratePrototype Optional decorator of the prototype {@link Model}
	 *                          for refactor testing. May be <code>null</code> to
	 *                          use prototype as is.
	 */
	public void main(R rootModel, Class<? extends AbstractIdeEditor<R, RE, O>> ideEditorClass,
			Consumer<M> decoratePrototype) {
		AbstractIdeEditor.launchOutsideWorkbench(() -> {
			AbstractIdeEditor<R, RE, O> ideEditor = ideEditorClass.getDeclaredConstructor().newInstance();
			this.init(new MainConfigurableContext(ideEditor.createOperations(rootModel), decoratePrototype));
			this.run();
		});
	}

	@Override
	protected void loadConfiguration(Shell shell) {

		// Ensure have context
		ConfigurableContext<R, O> context = this.getConfigurableContext();
		if ((context == null) || (!(context instanceof AbstractConfigurableItem.MainConfigurableContext))) {
			throw new IllegalStateException("Must start main through instance main method");
		}
		MainConfigurableContext mainContext = (MainConfigurableContext) context;

		// Associate the parent shell
		mainContext.parentShell = shell;

		// Obtain the IDE configurer
		IdeConfigurer ideConfigurer = this.configure();
		if (ideConfigurer == null) {
			new Label(shell, SWT.NONE).setText("No configuration for " + this.getClass().getSimpleName());
			return;
		}

		// Provide separate tabs for add and refactor
		TabFolder folder = new TabFolder(shell, SWT.NONE);

		// Load the add configuration
		if (ideConfigurer.add.size() > 0) {
			TabItem addTab = new TabItem(folder, SWT.NONE);
			addTab.setText("Add");
			Group addGroup = new Group(folder, SWT.NONE);
			addGroup.setLayout(new FillLayout());
			addTab.setControl(addGroup);
			I addItem = this.item(null);
			Configurer<I> addConfigurer = new Configurer<>(OfficeFloorOsgiBridge.getClassLoaderInstance(), shell);
			ConfigurationBuilder<I> addBuilder = addConfigurer;
			ConfigurableModelContext<O, M> addContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return mainContext.getOperations();
				}

				@Override
				public M getModel() {
					return null;
				}

				@Override
				public void execute(Change<M> change) {
					mainContext.getChangeExecutor().execute(change);
				}
			};
			for (ItemConfigurer<O, M, I> itemConfigurer : ideConfigurer.add) {
				itemConfigurer.configure(addBuilder, addContext);
			}
			addConfigurer.loadConfiguration(addItem, addGroup);
		}

		// Load the add immediately
		if (ideConfigurer.addImmediately != null) {
			TabItem addTab = new TabItem(folder, SWT.NONE);
			addTab.setText("Add");
			Group addGroup = new Group(folder, SWT.NONE);
			addGroup.setLayout(new RowLayout());
			addTab.setControl(addGroup);
			ConfigurableModelContext<O, M> addContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return mainContext.getOperations();
				}

				@Override
				public M getModel() {
					return null; // no modal on add
				}

				@Override
				public void execute(Change<M> change) {
					mainContext.getChangeExecutor().execute(change);
				}
			};
			Button addButton = new Button(addGroup, SWT.NONE);
			addButton.setText("Add");
			addButton.addListener(SWT.Selection, (event) -> {
				try {
					ideConfigurer.addImmediately.action(addContext);
				} catch (Throwable ex) {
					System.out.println("Failed to add " + ex.getMessage());
				}
			});
		}

		// Load the refactor configuration
		if (ideConfigurer.refactor.size() > 0) {
			TabItem refactorTab = new TabItem(folder, SWT.NONE);
			refactorTab.setText("Refactor");
			Group refactorGroup = new Group(folder, SWT.NONE);
			refactorGroup.setLayout(new FillLayout());
			refactorTab.setControl(refactorGroup);
			M prototype = this.prototype();
			if (mainContext.decoratePrototype != null) {
				mainContext.decoratePrototype.accept(prototype);
			}
			I refactorItem = this.item(prototype);
			Configurer<I> refactorConfigurer = new Configurer<>(OfficeFloorOsgiBridge.getClassLoaderInstance(), shell);
			ConfigurationBuilder<I> refactorBuilder = refactorConfigurer;
			ConfigurableModelContext<O, M> refactorContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return mainContext.getOperations();
				}

				@Override
				public M getModel() {
					return prototype;
				}

				@Override
				public void execute(Change<M> change) {
					mainContext.getChangeExecutor().execute(change);
				}
			};
			for (ItemConfigurer<O, M, I> itemConfigurer : ideConfigurer.refactor) {
				itemConfigurer.configure(refactorBuilder, refactorContext);
			}
			refactorConfigurer.loadConfiguration(refactorItem, refactorGroup);
		}

		// Load the delete configuration
		if (ideConfigurer.delete != null) {
			TabItem deleteTab = new TabItem(folder, SWT.NONE);
			deleteTab.setText("Delete");
			Group deleteGroup = new Group(folder, SWT.NONE);
			deleteGroup.setLayout(new RowLayout());
			deleteTab.setControl(deleteGroup);
			M prototype = this.prototype();
			if (mainContext.decoratePrototype != null) {
				mainContext.decoratePrototype.accept(prototype);
			}
			ConfigurableModelContext<O, M> deleteContext = new ConfigurableModelContext<O, M>() {

				@Override
				public O getOperations() {
					return mainContext.getOperations();
				}

				@Override
				public M getModel() {
					return prototype;
				}

				@Override
				public void execute(Change<M> change) {
					mainContext.getChangeExecutor().execute(change);
				}
			};
			Button deleteButton = new Button(deleteGroup, SWT.NONE);
			deleteButton.setText("Delete");
			deleteButton.addListener(SWT.Selection, (event) -> {
				try {
					ideConfigurer.delete.action(deleteContext);
				} catch (Throwable ex) {
					System.out.println("Failed to delete " + ex.getMessage());
				}
			});
		}
	}

	/**
	 * Main {@link ConfigurableContext} for testing configuration outside Eclipse
	 * IDE.
	 */
	private class MainConfigurableContext implements ConfigurableContext<R, O>, ChangeExecutor {

		/**
		 * Operations.
		 */
		private final O operations;

		/**
		 * {@link Consumer} to decorate the prototype.
		 */
		private final Consumer<M> decoratePrototype;

		/**
		 * Parent {@link Shell}.
		 */
		private Shell parentShell;

		/**
		 * Instantiate.
		 * 
		 * @param operations        Operations.
		 * @param decoratePrototype {@link Consumer} to decorate the prototype.
		 */
		private MainConfigurableContext(O operations, Consumer<M> decoratePrototype) {
			this.operations = operations;
			this.decoratePrototype = decoratePrototype;
		}

		/**
		 * =============== ConfigurableContext ===================
		 */

		@Override
		public AdaptedRootBuilder<R, O> getRootBuilder() {
			throw new IllegalStateException(
					"Should not require " + AdaptedRootBuilder.class.getSimpleName() + " for testing configuration");
		}

		@Override
		public Shell getParentShell() {
			return this.parentShell;
		}

		@Override
		public OfficeFloorOsgiBridge getOsgiBridge() throws Exception {
			return OfficeFloorOsgiBridge.getClassLoaderInstance();
		}

		@Override
		public O getOperations() {
			return operations;
		}

		@Override
		public ChangeExecutor getChangeExecutor() {
			return this;
		}

		/*
		 * ================ ChangeExecutor =======================
		 */

		@Override
		public void execute(Change<?> change) {
			// Log running the change
			StringBuilder message = new StringBuilder();
			message.append("Executing change '" + change.getChangeDescription() + "' for target "
					+ change.getTarget().getClass().getName());
			if (!change.canApply()) {
				message.append(" (can not apply)");
				for (Conflict conflict : change.getConflicts()) {
					message.append(System.lineSeparator() + "\t" + conflict.getConflictDescription());
				}
			}
			System.out.println(message.toString());
		}

		@Override
		public void execute(ITransactionalOperation operation) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support executing "
					+ ITransactionalOperation.class.getSimpleName());
		}

		@Override
		public void addChangeListener(ChangeListener changeListener) {
			throw new UnsupportedOperationException(
					MainConfigurableContext.class.getName() + " does not support " + ChangeListener.class.getName());
		}

		@Override
		public void removeChangeListener(ChangeListener changeListener) {
			throw new UnsupportedOperationException(
					MainConfigurableContext.class.getName() + " does not support " + ChangeListener.class.getName());
		}

		@Override
		public String getPreference(String preferenceId) {
			return null; // always use defaults
		}

		@Override
		public void addPreferenceListener(String preferenceId, PreferenceListener preferenceListener) {
			// never changes
		}
	}

}