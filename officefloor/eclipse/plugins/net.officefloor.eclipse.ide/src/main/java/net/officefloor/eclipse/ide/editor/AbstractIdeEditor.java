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
package net.officefloor.eclipse.ide.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.gef.fx.swt.canvas.IFXCanvasFactory;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.dialogs.SaveAsDialog;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import javafx.beans.property.Property;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.memory.MemoryConfigurationContext;
import net.officefloor.eclipse.common.javafx.structure.StructureLogger;
import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.editor.AdaptedBuilder;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.AdaptedEditorModule;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ChangeAdapter;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.ChildrenGroupBuilder;
import net.officefloor.eclipse.editor.SelectOnly;
import net.officefloor.eclipse.ide.OfficeFloorIdePlugin;
import net.officefloor.eclipse.ide.editor.AbstractItem.ConfigurableContext;
import net.officefloor.eclipse.ide.editor.AbstractItem.IdeChildrenGroup;
import net.officefloor.eclipse.ide.editor.AbstractItem.PreferenceListener;
import net.officefloor.eclipse.ide.preferences.PreferencesEditorInput;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.eclipse.osgi.ProjectConfigurationContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * {@link OfficeFloorModel} editor.
 * 
 * @param <R>
 *            Root {@link Model} type.
 * @param <O>
 *            {@link Change} operations type.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractIdeEditor<R extends Model, RE extends Enum<RE>, O> extends AbstractFXEditor {

	/**
	 * Translates the style with the details of the item being rendered.
	 * 
	 * @param rawStyle
	 *            Raw style to be translated.
	 * @param item
	 *            {@link AbstractItem}.
	 * @return Ready to use translate.
	 */
	public static <M extends Model> String translateStyle(String rawStyle, AbstractItem<?, ?, ?, ?, ?, ?> item) {

		// Ensure have a style
		if (rawStyle == null) {
			return null;
		}

		// Translate the style
		return rawStyle.replace("${model}", item.prototype().getClass().getSimpleName());
	}

	/**
	 * Indicates if running outside the {@link IWorkbench}.
	 */
	private static boolean isOutsideWorkbench = false;

	/**
	 * Logic to launch outside the {@link IWorkbench}.
	 */
	public static interface OutsideWorkbenchLauncher {

		/**
		 * Launches outside the {@link IWorkbench}.
		 * 
		 * @throws Throwable
		 *             If failure in launching.
		 */
		void launch() throws Throwable;
	}

	/**
	 * Enables running the {@link AbstractIdeEditor} outside of the
	 * {@link IWorkbench}.
	 * 
	 * @param launcher
	 *            {@link OutsideWorkbenchLauncher}.
	 */
	public synchronized static void launchOutsideWorkbench(OutsideWorkbenchLauncher launcher) {
		isOutsideWorkbench = true;
		try {
			launcher.launch();
		} catch (Throwable ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			} else {
				throw new RuntimeException(ex);
			}
		} finally {
			isOutsideWorkbench = false;
		}
	}

	/**
	 * {@link Module} for running outside the {@link IWorkbench}.
	 */
	private static class OutsideWorkbenchModule extends AbstractModule implements IFXCanvasFactory {

		@Override
		protected void configure() {
			this.bind(IFXCanvasFactory.class).toInstance(this);
		}

		@Override
		public FXCanvas createCanvas(Composite parent, int style) {
			return new FXCanvasEx(parent, style);
		}
	}

	/**
	 * Convenience method to launch the {@link AbstractIdeEditor} implementation
	 * outside the {@link IWorkbench}.
	 * 
	 * @param configurationFile
	 *            {@link File} containing the configuration.
	 */
	public static void launch(File configurationFile) {

		// Read in the file contents
		StringWriter configuration = new StringWriter();
		try (Reader reader = new FileReader(configurationFile)) {
			for (int character = reader.read(); character != -1; character = reader.read()) {
				configuration.write(character);
			}
		} catch (IOException ex) {
			System.err.println("Failed to load configuration from file " + configurationFile.getAbsolutePath());
			ex.printStackTrace();
		}

		// Launch the editor
		launch(configuration.toString());
	}

	/**
	 * Convenience method to launch the {@link AbstractIdeEditor} implementation
	 * outside the {@link IWorkbench}.
	 * 
	 * @param configuration
	 *            Configuration of {@link ConfigurationItem} to launch the
	 *            {@link AbstractIdeEditor}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void launch(String configuration) {

		// Determine the launch class
		StackTraceElement[] cause = Thread.currentThread().getStackTrace();
		boolean isFoundThisMethod = false;
		String launchClassName = null;
		FOUND_LAUNCHER: for (StackTraceElement se : cause) {
			String className = se.getClassName();
			String methodName = se.getMethodName();
			if ((isFoundThisMethod) && (!className.equals(AbstractIdeEditor.class.getName()))) {
				launchClassName = className;
				break FOUND_LAUNCHER;
			} else if (AbstractIdeEditor.class.getName().equals(className) && "launch".equals(methodName)) {
				isFoundThisMethod = true;
			}
		}

		// Ensure have the launch class
		if (launchClassName == null) {
			throw new RuntimeException("Unable to determine " + AbstractIdeEditor.class.getName() + " implementation");
		}

		// Attempt to launch IDE editor outside workbench
		final String finalLaunchClassName = launchClassName;
		launchOutsideWorkbench(() -> {

			// Instantiate the editor
			Class launchClass = Class.forName(finalLaunchClassName, false,
					Thread.currentThread().getContextClassLoader());
			Object instance = launchClass.newInstance();
			AbstractIdeEditor editor = (AbstractIdeEditor) instance;

			// Load configuration to run outside workbench
			editor.osgiBridge = OfficeFloorOsgiBridge.getClassLoaderInstance();
			editor.configurationItem = MemoryConfigurationContext.createWritableConfigurationItem("TEST");
			editor.configurationItem.setConfiguration(new ByteArrayInputStream(configuration.getBytes()));

			// Display the editor
			AbstractConfigurerRunnable runnable = new AbstractConfigurerRunnable() {

				@Override
				protected void loadConfiguration(Shell shell) {
					editor.parentShell = shell;
					editor.createPartControl(shell);

					// Indicate editor and provide details of structure for CSS
					editor.rootBuilder.overlay(10, 10, (ctx) -> {
						ctx.getOverlayParent().getChildren().add(new Label(editor.getClass().getSimpleName()));
					});
					editor.rootBuilder.overlay(10, 50, (ctx) -> {
						Button log = new Button("log");
						log.setOnAction((event) -> editor.rootBuilder.getErrorHandler()
								.isError(() -> StructureLogger.logFull(log, System.out)));
						ctx.getOverlayParent().getChildren().add(log);
					});

					// Register logging of changes to the model
					editor.rootBuilder.getChangeExecutor().addChangeListener(new ChangeAdapter() {

						@Override
						public void postApply(Change<?> change) {

							// Save model (with changes applied)
							editor.doSave(null);

							// Log changed configuration for tracking
							try {
								System.out.println();
								System.out.println();
								System.out.println(
										"============== Change '" + change.getChangeDescription() + "' ==============");
								Reader reader = editor.configurationItem.getReader();
								for (int character = reader.read(); character != -1; character = reader.read()) {
									System.out.write(character);
								}
								System.out.println();
								System.out.println();
							} catch (Exception ex) {
								System.err.println("Failed to log configuration: ");
								ex.printStackTrace();
							}
						}
					});
				}
			};
			runnable.run();
		});
	}

	/**
	 * {@link Injector}.
	 */
	private final Injector injector;

	/**
	 * {@link AdaptedEditorModule}.
	 */
	private AdaptedEditorModule module;

	/**
	 * {@link AdaptedBuilder}.
	 */
	private AdaptedBuilder adaptedBuilder;

	/**
	 * {@link Function} to create the operations from the root {@link Model}.
	 */
	private Function<R, O> createOperations;

	/**
	 * Current {@link IFile} being edited.
	 */
	private IFile configurationFile;

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private WritableConfigurationItem configurationItem;

	/**
	 * Parent {@link Shell}.
	 */
	private Shell parentShell;

	/**
	 * {@link AdaptedRootBuilder}.
	 */
	private AdaptedRootBuilder<R, O> rootBuilder;

	/**
	 * {@link Model} being edited.
	 */
	private R model;

	/**
	 * Operations.
	 */
	private O operations;

	/**
	 * {@link OfficeFloorOsgiBridge}.
	 */
	private OfficeFloorOsgiBridge osgiBridge;

	/**
	 * {@link AbstractConfigurableItem} instances for this
	 * {@link AbstractIdeEditor}.
	 */
	private List<AbstractConfigurableItem<R, RE, O, ?, ?, ?>> parents = null;

	/**
	 * {@link IPreferenceStore}. May be <code>null</code>.
	 */
	private IPreferenceStore preferenceStore;

	/**
	 * Registered {@link IPropertyChangeListener} instances for this
	 * {@link AbstractIdeEditor}. Need to be unregistered on close of this
	 * {@link AbstractIdeEditor}.
	 */
	private List<IPropertyChangeListener> preferenceChangeListeners = new LinkedList<>();

	/**
	 * {@link ConfigurableContext}.
	 */
	private ConfigurableContext<R, O> configurableContext;

	/**
	 * {@link SelectOnly}.
	 */
	private SelectOnly selectOnly = null;

	/**
	 * Instantiate to capture {@link Injector}.
	 * 
	 * @param injector
	 *            {@link Injector}.
	 */
	private AbstractIdeEditor(Injector injector) {
		super(injector);
		this.injector = injector;
	}

	/**
	 * <p>
	 * Instantiate to capture {@link AdaptedEditorModule}.
	 * <p>
	 * Allows for alternate {@link AdaptedEditorModule} implementation.
	 * 
	 * @param module
	 *            {@link AdaptedEditorModule}.
	 * @param rootModelType
	 *            Root {@link Model} type.
	 * @param createOperations
	 *            {@link Function} to create the operations from the root
	 *            {@link Model}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected AbstractIdeEditor(AdaptedEditorModule module, Class<R> rootModelType, Function<R, O> createOperations) {
		this(Guice.createInjector(isOutsideWorkbench ? Modules.override(module).with(new OutsideWorkbenchModule())
				: Modules.override(module).with(new MvcFxUiModule())));
		this.module = module;
		this.createOperations = createOperations;

		// Initialise
		this.module.initialise(this.getDomain(), this.injector);

		// Create the adapted builder
		this.adaptedBuilder = (context) -> {

			// Create the root model
			this.rootBuilder = context.root(rootModelType, (model) -> {
				// Keep track of operations (for configurable context)
				this.operations = this.createOperations(model);
				return this.operations;
			});

			// Obtain the preferences (may not be running in OSGi environment)
			this.preferenceStore = OfficeFloorIdePlugin.getDefault() != null
					? OfficeFloorIdePlugin.getDefault().getPreferenceStore()
					: null;

			// Create the configurable context
			this.configurableContext = new ConfigurableContext<R, O>() {

				@Override
				public AdaptedRootBuilder<R, O> getRootBuilder() {
					return AbstractIdeEditor.this.rootBuilder;
				}

				@Override
				public OfficeFloorOsgiBridge getOsgiBridge() throws Exception {
					return AbstractIdeEditor.this.getOsgiBridge();
				}

				@Override
				public Shell getParentShell() {
					return AbstractIdeEditor.this.parentShell;
				}

				@Override
				public O getOperations() {
					return AbstractIdeEditor.this.operations;
				}

				@Override
				public ChangeExecutor getChangeExecutor() {
					return AbstractIdeEditor.this.rootBuilder.getChangeExecutor();
				}

				@Override
				public String getPreference(String preferenceId) {
					IPreferenceStore preferences = AbstractIdeEditor.this.preferenceStore;
					return preferences == null ? null : preferences.getString(preferenceId);
				}

				@Override
				public void addPreferenceListener(String preferenceId, PreferenceListener preferenceListener) {
					IPreferenceStore preferences = AbstractIdeEditor.this.preferenceStore;
					if (preferences != null) {

						// Create and register listening to preference change
						IPropertyChangeListener changeListener = (event) -> {
							if (preferenceId.equals(event.getProperty())) {

								// Obtain the value
								Object value = event.getNewValue();
								if (!(value instanceof String)) {
									value = value.toString();
								}

								// Notify of value change
								preferenceListener.preferenceValueChanged((String) value);
							}
						};
						preferences.addPropertyChangeListener(changeListener);

						// Register for removing on editor close
						AbstractIdeEditor.this.preferenceChangeListeners.add(changeListener);
					}
				}
			};

			// Allow initialising of editor
			this.init(configurableContext);

			// Configure the editor
			for (AbstractConfigurableItem parent : this.getParents()) {

				// Initialise the parent
				parent.init(configurableContext);

				// Create the adapted parent
				AdaptedParentBuilder parentBuilder = parent.createAdaptedParent();

				// Load the children
				loadChildren(parent, parentBuilder);
			}
		};
	}

	/**
	 * Loads the children and their children recursively.
	 * 
	 * @param parent
	 *            Parent {@link AbstractItem}.
	 * @param parentBuilder
	 *            Parent {@link AdaptedChildBuilder}.
	 * @param configurableContext
	 *            {@link ConfigurableContext}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadChildren(AbstractItem parent, AdaptedChildBuilder parentBuilder) {

		// Load the connections
		for (AbstractItem<?, ?, ?, ?, ?, ?>.IdeConnectionTarget<? extends ConnectionModel, ?, ?> connection : parent
				.getConnections()) {
			connection.loadConnection(parentBuilder);
		}

		// Load styling
		this.loadPreferredStyling(parent.getPreferenceStyleId(), parentBuilder.style(), parent.style(),
				(rawStyle) -> translateStyle(rawStyle, parent));

		// Create the children groups
		for (IdeChildrenGroup ideChildrenGroup : parent.getChildrenGroups()) {

			// Add the children group
			ChildrenGroupBuilder childrenGroup = parentBuilder.children(ideChildrenGroup.getChildrenGroupName(),
					ideChildrenGroup, ideChildrenGroup.changeEvents());

			// Create the children
			for (AbstractItem child : ideChildrenGroup.getChildren()) {

				// Initialise the child
				child.init(this.configurableContext);

				// Build the child
				AdaptedChildBuilder childBuilder = child.createChild(childrenGroup);

				// Load the grand children (and connections)
				loadChildren(child, childBuilder);
			}
		}
	}

	/**
	 * Loads the preferred styling.
	 * 
	 * @param preferenceId
	 *            {@link IPreferenceStore} identifier for the preferred styling.
	 * @param style
	 *            {@link Property} to be loaded with the styling.
	 * @param defaultStyle
	 *            Default style. May be <code>null</code>.
	 * @param translator
	 *            Optional translator to translate the raw styling. May be
	 *            <code>null</code>.
	 */
	private void loadPreferredStyling(String preferenceId, Property<String> style, String defaultStyle,
			Function<String, String> translator) {

		// Function to apply the style
		Consumer<String> applyStyle = (rawStyle) -> {

			// Use appropriate style
			if ((rawStyle == null) || (rawStyle.trim().length() == 0)) {
				rawStyle = defaultStyle;
			}

			// Translate the style ready for use
			String translatedStyle = translator == null ? rawStyle : translator.apply(rawStyle);

			// Load the style
			style.setValue(translatedStyle);
		};

		// Obtain the override style
		String overrideStyle = this.configurableContext.getPreference(preferenceId);

		// Set initial style (and listen to changes)
		applyStyle.accept(overrideStyle);
		this.configurableContext.addPreferenceListener(preferenceId, (value) -> applyStyle.accept(value));
	}

	/**
	 * Instantiate with default {@link AdaptedEditorModule}.
	 * 
	 * @param rootModelType
	 *            Root {@link Model} type.
	 * @param createOperations
	 *            {@link Function} to create the operations from the root
	 *            {@link Model}.
	 */
	public AbstractIdeEditor(Class<R> rootModelType, Function<R, O> createOperations) {
		this(new AdaptedEditorModule(), rootModelType, createOperations);
	}

	/**
	 * Obtains the {@link IPreferenceStore} identifier for the palette indicator
	 * styling.
	 * 
	 * @return {@link IPreferenceStore} identifier for the palette indicator
	 *         styling.
	 */
	public String getPaletteIndicatorStyleId() {
		return this.prototype().getClass().getSimpleName() + ".palette.indicator.style";
	}

	/**
	 * Obtains the {@link IPreferenceStore} identifier for the palette styling.
	 * 
	 * @return {@link IPreferenceStore} identifier for the palette styling.
	 */
	public String getPaletteStyleId() {
		return this.prototype().getClass().getSimpleName() + ".palette.style";
	}

	/**
	 * Obtains the {@link IPreferenceStore} identifier for the content styling.
	 * 
	 * @return {@link IPreferenceStore} identifier for the content styling.
	 */
	public String getContentStyleId() {
		return this.prototype().getClass().getSimpleName() + ".content.style";
	}

	/**
	 * Instantiate.
	 * 
	 * @param selectOnly
	 *            {@link SelectOnly}.
	 */
	public void setSelectOnly(SelectOnly selectOnly) {
		this.selectOnly = selectOnly;
	}

	/**
	 * Allows overriding to initialise the {@link AbstractIdeEditor}.
	 * 
	 * @param context
	 *            {@link ConfigurableContext}.
	 */
	protected void init(ConfigurableContext<R, O> context) {
	}

	/**
	 * Obtains the {@link AbstractConfigurableItem} instances.
	 * 
	 * @return {@link AbstractConfigurableItem} instances.
	 */
	@SuppressWarnings("unchecked")
	public final AbstractConfigurableItem<R, RE, O, ?, ?, ?>[] getParents() {

		// Lazy load the parents
		if (this.parents == null) {
			this.parents = new LinkedList<>();
			this.loadParents(this.parents);
		}

		// Return the parents
		return parents.toArray(new AbstractConfigurableItem[parents.size()]);
	}

	/**
	 * Obtains root prototype.
	 * 
	 * @return Root prototype.
	 */
	public abstract R prototype();

	/**
	 * Loads the {@link AbstractConfigurableItem} instances.
	 * 
	 * @param parents
	 *            {@link List} to be populated with the
	 *            {@link AbstractConfigurableItem} instances.
	 */
	protected abstract void loadParents(List<AbstractConfigurableItem<R, RE, O, ?, ?, ?>> parents);

	/**
	 * Creates the root {@link Model} from the {@link ConfigurationItem}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} containing the configuration of the
	 *            {@link Model}.
	 * @return Root {@link Model} within the {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to load the root {@link Model} from the
	 *             {@link ConfigurationItem}.
	 */
	protected abstract R loadRootModel(ConfigurationItem configurationItem) throws Exception;

	/**
	 * Writes the root {@link Model} to the {@link WritableConfigurationItem}.
	 * 
	 * @param model
	 *            Root {@link Model} to be saved.
	 * @param configurationItem
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to save the root {@link Model} into the
	 *             {@link WritableConfigurationItem}.
	 */
	protected abstract void saveRootModel(R model, WritableConfigurationItem configurationItem) throws Exception;

	/**
	 * Allows overriding the palette styling.
	 * 
	 * @return Palette styling. May be <code>null</code> for default styling.
	 */
	public String paletteStyle() {
		return null;
	}

	/**
	 * Allows overriding the palette indicator styling.
	 * 
	 * @return Palette indicator styling. May be <code>null</code> for default
	 *         styling.
	 */
	public String paletteIndicatorStyle() {
		return null;
	}

	/**
	 * Allows overriding the content styling.
	 * 
	 * @return Content styling. May be <code>null</code> for defaulting styling.
	 */
	public String contentStyle() {
		return null;
	}

	/**
	 * Creates the operations for the root {@link Model}.
	 * 
	 * @param model
	 *            Root {@link Model}.
	 * @return Operations.
	 */
	public O createOperations(R model) {
		return this.createOperations.apply(model);
	}

	/**
	 * Obtains the {@link OfficeFloorOsgiBridge}.
	 * 
	 * @return {@link OfficeFloorOsgiBridge}.
	 * @throws Exception
	 *             If fails to obtain the {@link OfficeFloorOsgiBridge}.
	 */
	protected final OfficeFloorOsgiBridge getOsgiBridge() throws Exception {

		// Determine if cached access to compiler
		if (this.osgiBridge != null) {
			return this.osgiBridge;
		}

		// Obtain the file input
		IEditorInput input = this.getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			throw new Exception(
					"Invalid IEditorInput as expecting a file (" + (input == null ? null : input.getClass().getName()));
		}
		IFileEditorInput fileInput = (IFileEditorInput) input;

		// Obtain the file (and subsequently it's project)
		IFile file = fileInput.getFile();
		IProject project = file.getProject();

		// Obtain the java project
		IJavaProject javaProject = JavaCore.create(project);

		// Bridge java project to OfficeFloor compiler
		this.osgiBridge = new OfficeFloorOsgiBridge(javaProject);

		// Obtain the OfficeFloor compiler
		return this.osgiBridge;
	}

	/*
	 * ============== AbstractFXEditor ==================
	 */

	@Override
	public FXCanvas getCanvas() {
		return super.getCanvas();
	}

	@Override
	public final IViewer getContentViewer() {
		return this.module.getContentViewer();
	}

	@Override
	protected final void hookViewers() {

		// Provide possible select only
		if (this.selectOnly != null) {
			this.module.setSelectOnly(this.selectOnly);
		}

		// Create the view
		Pane view = this.module.createParent(this.adaptedBuilder);

		// Create scene and populate canvas with view
		this.getCanvas().setScene(new Scene(view));

		// Configure styling of palette indicator
		this.loadPreferredStyling(this.getPaletteIndicatorStyleId(), this.rootBuilder.paletteIndicatorStyle(),
				this.paletteIndicatorStyle(), null);

		// Configure styling of palette
		this.loadPreferredStyling(this.getPaletteStyleId(), this.rootBuilder.paletteStyle(), this.paletteStyle(), null);

		// Configure styling of content
		this.loadPreferredStyling(this.getContentStyleId(), this.rootBuilder.contentStyle(), this.contentStyle(), null);
	}

	@Override
	protected final void activate() {

		// Load the model
		if (this.model == null) {
			this.rootBuilder.getErrorHandler().isError(() -> {
				this.model = this.loadRootModel(this.configurationItem);
			});
		}

		// Load the module with root model
		this.module.loadRootModel(this.model);

		// Activate
		super.activate();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected final void setInput(IEditorInput input) {
		super.setInput(input);

		// Input changed, so reset for new input
		this.osgiBridge = null;

		// Obtain the input configuration
		if (input instanceof IFileEditorInput) {
			// Load the configuration item from the file
			IFileEditorInput fileInput = (IFileEditorInput) input;
			this.configurationFile = fileInput.getFile();
			this.configurationItem = ProjectConfigurationContext.getWritableConfigurationItem(this.configurationFile,
					null);

		} else if (input instanceof PreferencesEditorInput) {
			// Provided the model
			PreferencesEditorInput preferencesInput = (PreferencesEditorInput) input;
			this.model = (R) preferencesInput.getRootModel();

		} else {
			// Unknown editor input
			throw new IllegalStateException("Unable to edit input " + input.getClass().getName());
		}
	}

	@Override
	protected final void setSite(IWorkbenchPartSite site) {
		super.setSite(site);

		// Keep track of the parent shell
		this.parentShell = site.getShell();
	}

	@Override
	public final void doSave(IProgressMonitor monitor) {
		this.rootBuilder.getErrorHandler().isError(() -> {

			// Save the model
			this.saveRootModel(this.model, this.configurationItem);

			// Flag saved (no longer dirty)
			this.markNonDirty();
		});
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSaveAs() {
		// Allow saving a copy of the configuration
		final SaveAsDialog saveAsDialog = new SaveAsDialog(this.getEditorSite().getShell());
		saveAsDialog.setOriginalFile(this.configurationFile);
		if (saveAsDialog.open() == IStatus.OK) {

			// Save to the specified file
			IPath file = saveAsDialog.getResult();

			// TODO handle saving to the path
			throw new UnsupportedOperationException("TODO implement SaveAs to " + file.toString());
		}
	}

	@Override
	public void dispose() {

		// Remove the preference change listeners
		if (this.preferenceStore != null) {
			for (IPropertyChangeListener listener : this.preferenceChangeListeners) {
				this.preferenceStore.removePropertyChangeListener(listener);
			}
		}

		// Further clean up
		super.dispose();
	}

}